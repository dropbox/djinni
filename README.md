# Djinni

Djinni is a tool for generating cross-language type declarations and interface bindings. It's
designed to connect C++ with either Java or Objective-C. Python support is available in an experimental version on the `python` branch.

We at Dropbox use Djinni to interface cross-platform C++ library code with platform-specific
Java and Objective-C on Android and iOS.

We announced Djinni at CppCon 2014. You can see the [slides](https://bit.ly/djinnitalk) and [video](https://bit.ly/djinnivideo).  For more info about Djinni and how others are using it, check out the community links at the end of this document.

## Main Features
- Generates parallel C++, Java and Objective-C type definitions from a single interface
  description file.
- Supports the intersection of the three core languages' primitive types, and user-defined
  enums, records, and interfaces.
- Generates interface code allowing bidirectional calls between C++ and Java (with JNI) or
  Objective-C (with Objective-C++).
- Can autogenerate comparator functions (equality, ordering) on data types.

## Getting Started

### Types
Djinni generates code based on interface definitions in an IDL file. An IDL file can contain
three kinds of declarations: enums, records, and interfaces.

* Enums become C++ enum classes, Java enums, or ObjC `NS_ENUM`s.
* Records are pure-data value objects.
* Interfaces are objects with defined methods to call (in C++, passed by `shared_ptr`). Djinni
  produces code allowing an interface implemented in C++ to be transparently used from ObjC or
  Java, and vice versa.

### IDL Files
Djinni's input is an interface description file. Here's an example:

    # Multi-line comments can be added here. This comment will be propagated
    # to each generated definition.
    my_enum = enum {
        option1;
        option2;
        option3;
    }

    my_record = record {
        id: i32;
        info: string;
        store: set<string>;
        hash: map<string, i32>;

        values: list<another_record>;

        # Comments can also be put here

        # Constants can be included
        const string_const: string = "Constants can be put here";
        const min_value: another_record = {
            key1 = 0,
            key2 = ""
        };
    }

    another_record = record {
        key1: i32;
        key2: string;
    } deriving (eq, ord)

    # This interface will be implemented in C++ and can be called from any language.
    my_cpp_interface = interface +c {
        method_returning_nothing(value: i32);
        method_returning_some_type(key: string): another_record;
        static get_version(): i32;

        # Interfaces can also have constants
        const version: i32 = 1;
    }

    # This interface will be implemented in Java and ObjC and can be called from C++.
    my_client_interface = interface +j +o {
        log_string(str: string): bool;
    }

Djinni files can also include each other. Adding the line:

    @import "relative/path/to/filename.djinni"

at the beginning of a file will simply include another file. Child file paths are
relative to the location of the file that contains the @import. Two different djinni files
cannot define the same type. `@import` behaves like `#include` with `#pragma once` in C++, or
like ObjC's `#import`: if a file is included multiple times through different paths, then it
will only be processed once.

### Generate Code
When the Djinni file(s) are ready, from the command line or a bash script you can run:

    src/run \
       --java-out JAVA_OUTPUT_FOLDER \
       --java-package com.example.jnigenpackage \
       --java-cpp-exception DbxException \ # Choose between a customized C++ exception in Java and java.lang.RuntimeException (the default).
       --ident-java-field mFooBar \ # Optional, this adds an "m" in front of Java field names
       \
       --cpp-out CPP_OUTPUT_FOLDER \
       \
       --jni-out JNI_OUTPUT_FOLDER \
       --ident-jni-class NativeFooBar \ # This adds a "Native" prefix to JNI class
       \
       --objc-out OBJC_OUTPUT_FOLDER \
       --objc-type-prefix DB \ # Apple suggests Objective-C classes have a prefix for each defined type.
       \
       --objcpp-out OBJC_OUTPUT_FOLDER \
       \
       --idl MY_PROJECT.djinni

Some other options are also available, such as `--cpp-namespace` that put generated C++ code into the namespace specified. For a list of all options, run
`src/run --help`

Sample generated code is in the `example/generated-src/` and `test-suite/generated-src/`
directories of this distribution.

Note that if a language's output folder is not specified, that language will not be generated.
For more information, run `run --help` to see all command line arguments available.

### Use Generated Code in Your Project

#### Java / JNI / C++ Project

##### Includes & Build target
The following headers / code will be generated for each defined type:

| Type      | C++ header             | C++ source                 | Java                | JNI header            | JNI source            |
|-----------|------------------------|----------------------------|---------------------|-----------------------|-----------------------|
| Enum      | my\_enum.hpp           |                            | MyEnum.java         | NativeMyEnum.hpp      | NativeMyEnum.cpp      |
| Record    | my\_record[\_base].hpp | my\_record[\_base].cpp (+) | MyRecord[Base].java | NativeMyRecord.hpp    | NativeMyRecord.cpp    |
| Interface | my\_interface.hpp      | my\_interface.cpp (+)      | MyInterface.java    | NativeMyInterface.hpp | NativeMyInterface.cpp |

(+) Generated only for types that contain constants.

Add all generated source files to your build target, as well as the contents of
`support-lib/java`.

##### Our JNI approach
JNI stands for Java Native Interface, an extension of the Java language to allow interop with
native (C/C++) code or libraries. Complete documentation on JNI is available at:
http://docs.oracle.com/javase/6/docs/technotes/guides/jni/spec/jniTOC.html

For each type, built-in (`list`, `string`, etc.) or user-defined, Djinni produces a translator
class with a `toJava` and `fromJava` function to translate back and forth.

Application code is responsible for the initial load of the JNI library. Add a static block
somewhere in your code:

    System.loadLibrary("YourLibraryName");
    // The name is specified in Android.mk / build.gradle / Makefile, depending on your build system.

If you package your native library in a jar, you can also use `com.dropbox.djinni.NativeLibLoader` 
to help unpack and load your lib(s).  See the [Localhost README](example/localhost/README.md)
for details.

When a native library is called, JNI calls a special function called `JNI_OnLoad`. If you use
Djinni for all JNI interface code, include `support_lib/jni/djinni_main.cpp`; if not,
you'll need to add calls to your own `JNI_OnLoad` and `JNI_OnUnload` functions. See
`support-lib/jni/djinni_main.cpp` for details.

#### Objective-C / C++ Project

##### Includes & Build Target
Generated files for Objective-C / C++ are as follows (assuming prefix is `DB`):

| Type      | C++ header             | C++ source                 | Objective-C files        | Objective-C++ files         |
|-----------|------------------------|----------------------------|--------------------------|-----------------------------|
| Enum      | my\_enum.hpp           |                            | DBMyEnum.h               |                             |
| Record    | my\_record[\_base].hpp | my\_record[\_base].cpp (+) | DBMyRecord[Base].h       | DBMyRecord[Base]+Private.h  |
|           |                        |                            | DBMyRecord[Base].mm (++) | DBMyRecord[Base]+Private.mm |
| Interface | my\_interface.hpp      | my\_interface.cpp (+)      | DBMyInterface.h          | DBMyInterface+Private.h     |
|           |                        |                            |                          | DBMyInterface+Private.mm    |

(+) Generated only for types that contain constants.
(++) Generated only for types with derived operations and/or constants. These have `.mm` extensions to allow non-trivial constants.

Add all generated files to your build target, as well as the contents of `support-lib/objc`.
Note that `+Private` files can only be used with ObjC++ source (other headers are pure ObjC) and are not required by Objective-C users of your interface.

## Details of Generated Types
### Enum
Enums are translated to C++ `enum class`es with underlying type `int`, ObjC `NS_ENUM`s with
underlying type `NSInteger`, and Java enums.

### Record
Records are data objects. In C++, records contain all their elements by value, including other
records (so a record cannot contain itself).

#### Data types
The available data types for a record, argument, or return value are:

 - Boolean (`bool`)
 - Primitives (`i8`, `i16`, `i32`, `i64`, `f32`, `f64`).
 - Strings (`string`)
 - Binary (`binary`). This is implemented as `std::vector<uint8_t>` in C++, `byte[]` in Java,
   and `NSData` in Objective-C.
 - Date (`date`).  This is `chrono::system_clock::time_point` in C++, `Date` in Java, and
   `NSDate` in Objective-C.
 - List (`list<type>`). This is `vector<T>` in C++, `ArrayList` in Java, and `NSArray`
   in Objective-C. Primitives in a list will be boxed in Java and Objective-C.
 - Set (`set<type>`). This is `unordered_set<T>` in C++, `HashSet` in Java, and `NSSet` in
   Objective-C. Primitives in a set will be boxed in Java and Objective-C.
 - Map (`map<typeA, typeB>`). This is `unordered_map<K, V>` in C++, `HashMap` in Java, and
   `NSDictionary` in Objective-C. Primitives in a map will be boxed in Java and Objective-C.
 - Enumerations
 - Optionals (`optional<typeA>`). This is `std::experimental::optional<T>` in C++11, object /
   boxed primitive reference in Java (which can be `null`), and object / NSNumber strong
   reference in Objective-C (which can be `nil`).
 - Other record types. This is generated with a by-value semantic, i.e. the copy method will
   deep-copy the contents.

#### Extensions
To support extra fields and/or methods, a record can be "extended" in any language. To extend
a record in a language, you can add a `+c` (C++), `+j` (Java), or `+o` (ObjC) flag after the
record tag. The generated type will have a `Base` suffix, and you should create a derived type
without the suffix that extends the record type.

The derived type must be constructible in the same way as the `Base` type. Interfaces will
always use the derived type.

#### Derived methods
For record types, Haskell-style "deriving" declarations are supported to generate some common
methods. Djinni is capable of generating equality and order comparators, implemented
as operator overloading in C++ and standard comparison functions in Java / Objective-C.

Things to note:

 - All fields in the record are compared in the order they appear in the record declaration.
   If you need to add a field later, make sure the order is correct.
 - Ordering comparison is not supported for collection types, optionals, and booleans.
 - To compare records containing other records, the inner record must derive at least the same
   types of comparators as the outer record.

### Interface

#### Special Methods for C++ Only
`+c` interfaces (implementable only in C++) can have methods flagged with the special keywords const and static which have special effects in C++:

   special_methods = interface +c {
       const accessor_method();
       static factory_method();
   }
   
- `const` methods will be declared as const in C++, though this cannot be enforced on callers in other languages, which lack this feature.
- `static` methods will become a static method of the C++ class, which can be called from other languages without an object.  This is often useful for factory methods to act as a cross-language constructor.

#### Exception Handling
When an interface implemented in C++ throws a `std::exception`, it will be translated to a
`java.lang.RuntimeException` in Java or an `NSException` in Objective-C. The `what()` message
will be translated as well.

### Constants
Constants can be defined within interfaces and records. In Java and C++ they are part of the
generated class; and in Objective-C, constant names are globals with the name of the
interface/record prefixed. Example:

   record_with_const = record +c +j +o {
       const const_value: i32 = 8;
   }

will be `RecordWithConst::CONST_VALUE` in C++, `RecordWithConst.CONST_VALUE` in Java, and
`RecordWithConstConstValue` in Objective-C.

## Modularization and Library Support
When generating the interface for your project and wish to make it available to other users
in all of C++/Objective-C/Java you can tell Djinni to generate a special YAML file as part
of the code generation process. This file then contains all the information Djinni requires
to include your types in a different project. Instructing Djinni to create these YAML files
is controlled by the follwoing arguments:
- `--yaml-out`: The output folder for YAML files (Generator disabled if unspecified).
- `--yaml-out-file`: If specified all types are merged into a single YAML file instead of generating one file per type (relative to `--yaml-out`).
- `--yaml-prefix`: The prefix to add to type names stored in YAML files (default: `""`).

Such a YAML file looks as follows:
```yml
---
name: mylib_record1
typedef: 'record +c deriving(eq, ord)'
params: []
prefix: 'mylib'
cpp:
    typename: '::mylib::Record1'
    header: '"MyLib/Record1.hpp"'
    byValue: false
objc:
    typename: 'MLBRecord1'
    header: '"MLB/MLBRecord1.h"'
    boxed: 'MLBRecord1'
    pointer: true
    hash: '%s.hash'
objcpp:
    translator: '::mylib::djinni::objc::Record1'
    header: '"mylib/djinni/objc/Record1.hpp"'
java:
    typename: 'com.example.mylib.Record1'
    boxed: 'com.example.mylib.Record1'
    reference: true
    generic: true
    hash: '%s.hashCode()'
jni:
    translator: '::mylib::djinni::jni::Record1'
    header: '"Duration-jni.hpp"'
    typename: jobject
    typeSignature: 'Lcom/example/mylib/Record1;'
---
name: mylib_interface1
typedef: 'interface +j +o'
    (...)
---
name: mylib_enum1
typedef: 'enum'
    (...)

```
Each document in the YAML file describes one extern type.
A full documentation of all fields is available in `example/example.yaml`. You can also check
the files `test-suite/djinni/date.yaml` and `test-suite/djinni/duration.yaml` for some
real working examples of what you can do with it.

To use a library type in your project simply include it in your IDL file and refer to it using
its name identifier:
```
@extern "mylib.yaml"

client_interface = interface +c {
  foo(): mylib_record1;
}
```

These files can be created by hand as long as you follow the required format. This allows you
to support types not generated by Djinni. See `test-suite/djinni/duration.yaml` and the
accompanying translators in `test-suite/handwritten-src/cpp/Duration-objc.hpp` and 
`test-suite/handwritten-src/cpp/Duration-jni.hpp` for an advanced example. Handwritten
translators implement the following concept:
```cpp
// For C++ <-> Objective-C
struct Record1
{
    using CppType = ::mylib::Record1;
    using ObjcType = MLBRecord1*;

    static CppType toCpp(ObjcType o) { return /* your magic here */; }
    static ObjcType fromCpp(CppType c) { return /* your magic here */; }

    // Option 1: use this if no boxing is required
    using Boxed = Record1;
    // Option 2: or this if you do need dedicated boxing behavior
    struct Boxed
    {
        using ObjcType = MLBRecord1Special*;
        static CppType toCpp(ObjcType o) { return /* your magic here */; }
        static ObjcType fromCpp(CppType c) { return /* your magic here */; }
    }
};
```
```cpp
// For C++ <-> JNI
#include "djinni_support.hpp"
struct Record1
{
    using CppType = ::mylib::Record1;
    using JniType = jobject;

    static CppType toCpp(JniType j) { return /* your magic here */; }
    // The return type *must* be LocalRef<T> if T is not a primitive!
    static ::djinni::LocalRef<jobject> JniType fromCpp(CppType c) { return /* your magic here */; }

    using Boxed = Record1;
};
```
For `interface` classes the `CppType` alias is expected to be a `std::shared_ptr<T>`.

Be sure to put the translators into representative and distinct namespaces.

If your type is generic the translator takes the same number of template parameters.
At usage each is instantiated with the translators of the respective type argument.
```cpp
template<class A, class B>
struct Record1
{
    using CppType = ::mylib::Record1<typename A::CppType, typename B::CppType>;
    using ObjcType = MLBRecord1*;

    static CppType toCpp(ObjcType o)
    {
        // Use A::toCpp() and B::toCpp() if necessary
        return /* your magic here */;
    }
    static ObjcType fromCpp(CppType c)
    {
        // Use A::fromCpp() and B::fromCpp() if necessary
        return /* your magic here */;
    }

    using Boxed = Record1;
};
```

## Miscellaneous
### Record constructors / initializers
Djinni does not permit custom constructors for records or interfaces, since there would be
no way to implement them in Java except by manually editing the autogenerated file. Instead,
use extended records or static functions.

### Identifier Format
Djinni supports overridable formats for most generated filenames and identifiers. The complete
list can found by invoking Djinni with `--help`. The format is specified by formatting the
word FooBar in the desired style:
- `FOO_BAR` -> `GENERATED_IDENT`
- `mFooBar` -> `mGeneratedIdent`
- `FooBar` -> `GeneratedIdent`

### Integer types
In Djinni, i8 through i64 are all used with fixed length. The C++ builtin `int`, `long`, etc
and Objective-C `NSInteger` are not used because their length varies by architecture. Unsigned
integers are not included because they are not available in Java.

## Test Suite
Run `make test` to invoke the test suite, found in the test-suite subdirectory. It will build and run Java code on a local JVMy, plus Objective-C on an iOS simulator.  The latter will only work on a Mac with Xcode.

## Community Links

* Join the discussion with other developers at the [Mobile C++ Slack Community](https://mobilecpp.herokuapp.com/)
* There are a set of [tutorials](http://mobilecpptutorials.com/) for building a cross-platform app using Djinni.
* [mx3](https://github.com/libmx3/mx3) is an example project demonstrating use of Djinni and other tools.
* [Slides](https://bit.ly/djinnitalk) and [video](https://bit.ly/djinnivideo) from the CppCon 2014 talk where we introduced Djinni.
* [Slides](https://bit.ly/djinnitalk2) and [video](https://bit.ly/djinnivideo2) from the CppCon 2015 about Djinni implementatino techniques, and the addition of Python.
* You can see a [CppCon 2014 talk](https://www.youtube.com/watch?v=5AZMEm3rZ2Y) by app developers at Dropbox about their cross-platform experiences.

## Authors
- Kannan Goundan
- Tony Grue
- Derek He
- Steven Kabbes
- Jacob Potter
- Iulia Tamas
- Andrew Twyman

## Contacts
- Andrew Twyman - `atwyman@dropbox.com`
- Jacob Potter - `djinni@j4cbo.com`
