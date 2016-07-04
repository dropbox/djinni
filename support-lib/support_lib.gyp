{
    "targets": [
        {
            "target_name": "djinni_jni",
            "type": "static_library",
            "sources": [
              "../include/djinni/support-lib/djinni_common.hpp",
              "../include/djinni/support-lib/jni/djinni_support.hpp",
              "../include/djinni/support-lib/jni/Marshal.hpp",
              "jni/djinni_support.cpp",
            ],
            "include_dirs": [
              "jni",
              "../include"
            ],
            "direct_dependent_settings": {
                "include_dirs": [
                  "jni",
                  "../include"
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
              "../include/djinni/support-lib/objc/DJICppWrapperCache+Private.h",
              "../include/djinni/support-lib/objc/DJIError.h",
              "../include/djinni/support-lib/objc/DJIMarshal+Private.h",
              "../include/djinni/support-lib/objc/DJIObjcWrapperCache+Private.h",
              "../include/djinni/support-lib/proxy_cache_interface.hpp",
              "objc/DJIError.mm",
              "objc/DJIProxyCaches.mm",
              "proxy_cache_impl.hpp",
            ],
            "include_dirs": [
              "objc",
              "../include"
            ],
            "direct_dependent_settings": {
                "include_dirs": [
                  "objc",
                  "../include"
                ],
            },
        },
    ],
}
