{
    "targets": [
        {
            "target_name": "djinni_jni",
            "type": "static_library",
            "sources": [
                "<!@(ls jni/*)" ,
                #"jni/djinni_support.cpp",
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
            "target_name": "djinni_objc",
            "type": "static_library",
            "xcode_settings": {
              "CLANG_ENABLE_OBJC_ARC": "YES",
            },
            "sources": [
              "<!@(dir=('objc'); \
              pattern=('*.h' '*.hpp' '*.cpp' '*.mm' '*.m'); \
              echo ${pattern[@]} | xargs -n 1 find ${dir[@]} -iname \
              )", 
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
    ],
}
