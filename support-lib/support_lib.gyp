{
  "targets": [
    {
      "target_name": "djinni_jni",
      "type": "static_library",
      "sources": [
        "djinni_common.hpp",
        "jni/djinni_support.cpp",
        "jni/djinni_support.hpp",
        "jni/Marshal.hpp",
      ],
      "include_dirs": [
        "jni",
      ],
      "direct_dependent_settings": {
        "include_dirs": [
          "jni",
        ],
      },
    },
    {
      "target_name": "djinni_jni_main",
      "type": "static_library",
      "sources": [
        "jni/djinni_main.cpp",
      ],
      "link_settings": {
        "aosp_build_settings": {
          "LOCAL_WHOLE_STATIC_LIBRARIES": [ 'djinni_jni_main' ], # Ensure JNI symbols are exposed
        },
      },
    },
    {
      "target_name": "djinni_objc",
      "type": "static_library",
      "xcode_settings": {
        "CLANG_ENABLE_OBJC_ARC": "YES",
      },
      "sources": [
        "objc/DJICppWrapperCache+Private.h",
        "objc/DJIError.h",
        "objc/DJIError.mm",
        "objc/DJIMarshal+Private.h",
        "objc/DJIObjcWrapperCache+Private.h",
        "objc/DJIProxyCaches.mm",
        "proxy_cache_impl.hpp",
        "proxy_cache_interface.hpp",
      ],
      "include_dirs": [
        "objc",
      ],
      "direct_dependent_settings": {
        "include_dirs": [
          "objc",
        ],
      },
    },
    {
      "target_name": "djinni_cwrapper",
      "type": "static_library",
      "sources": [
        "cwrapper/djinni_common.hpp",
        "cwrapper/thread_local.cpp",
        "cwrapper/thread_local.hpp",
        "cwrapper/wrapper_marshal.cpp",
        "cwrapper/wrapper_marshal.hpp",
        "cwrapper/wrapper_marshal.h",
      ],
      "include_dirs": [
        "cwrapper",
      ],
      "direct_dependent_settings": {
        "include_dirs": [
          "cwrapper",
        ],
      },
      # Use C++1y for Python libraries, which depend on it.
      'cflags_cc': [ '-std=c++1y', '-frtti', '-fexceptions' ],
      'xcode_settings': {
        'CLANG_CXX_LANGUAGE_STANDARD': 'c++1y',
      },
    },
  ],
}
