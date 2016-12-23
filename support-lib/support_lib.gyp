{
    "targets": [
        {
            "target_name": "djinni_jni",
            "type": "static_library",
            "sources": [
              "include/djinni/djinni_common.hpp",
              "include/djinni/jni/djinni_support.hpp",
              "include/djinni/jni/Marshal.hpp",
              "src/jni/djinni_support.cpp",
            ],
            "include_dirs": [
              "include",
              "src",
            ],
            "direct_dependent_settings": {
                "include_dirs": [
                  "include",
                  "src",
                ],
            },
        },
        {
            "target_name": "djinni_objc",
            "type": "static_library",
            "xcode_settings": {
              "CLANG_ENABLE_OBJC_ARC": "YES",
            },
            "sources": [
              "include/djinni/objc/DJICppWrapperCache+Private.h",
              "include/djinni/objc/DJIError.h",
              "include/djinni/objc/DJIMarshal+Private.h",
              "include/djinni/objc/DJIObjcWrapperCache+Private.h",
              "include/djinni/proxy_cache_interface.hpp",
              "src/objc/DJIProxyCaches.mm",
              "src/objc/DJIError.mm",
              "src/proxy_cache_impl.hpp",
            ],
            "include_dirs": [
              "include",
              "src",
            ],
            "direct_dependent_settings": {
                "include_dirs": [
                  "include",
                  "src",
                ],
            },
        },
    ],
}
