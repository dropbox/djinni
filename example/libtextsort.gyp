{
    "targets": [
        {
            "target_name": "libtextsort_jni",
            "type": "shared_library",
            "dependencies": [
              "../support-lib/support_lib.gyp:djinni_jni",
            ],
            "ldflags": [ "-llog", "-Wl,--build-id,--gc-sections,--exclude-libs,ALL" ],
            "sources": [
              "../support-lib/jni/djinni_main.cpp",
              "<!@(python glob.py generated-src/jni   '*.cpp' '*.hpp')",
              "<!@(python glob.py generated-src/cpp   '*.cpp' '*.hpp')",
              "<!@(python glob.py handwritten-src/cpp '*.cpp' '*.hpp')",
            ],
            "include_dirs": [
              "generated-src/jni",
              "generated-src/cpp",
              "handwritten-src/jni",
              "handwritten-src/cpp",
            ],
        },
        {
            "target_name": "libtextsort_objc",
            "type": 'static_library',
            "dependencies": [
              "../support-lib/support_lib.gyp:djinni_objc",
            ],
            "sources": [
              "<!@(python glob.py generated-src/objc  '*.cpp' '*.mm' '*.m' '*.h' '*.hpp')",
              "<!@(python glob.py generated-src/cpp   '*.cpp' '*.hpp')",
              "<!@(python glob.py handwritten-src/cpp '*.cpp' '*.hpp')",
            ],
            "include_dirs": [
              "generated-src/objc",
              "generated-src/cpp",
              "handwritten-src/objc",
              "handwritten-src/cpp",
            ],
        },
    ],
}
