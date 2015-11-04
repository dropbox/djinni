{
    "targets": [
        {
            "target_name": "thread_cpp",
            "type": "static_library",
            "sources": [
              "cpp/thread_impl.hpp",
            ],
            "include_dirs": [
              "cpp",
            ],
            "direct_dependent_settings": {
                "include_dirs": [
                  "cpp",
                ],
            },
        },
        {
            "target_name": "thread_objc",
            "type": "static_library",
            "xcode_settings": {
              "CLANG_ENABLE_OBJC_ARC": "YES",
            },
            "sources": [
              "objc/DJIThreadFactoryImpl.h",
              "objc/DJIThreadFactoryImpl.m",
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

        # Java sources are in java/ and android/
        # Gyp doesn't include native support for Java.
    ],
}
