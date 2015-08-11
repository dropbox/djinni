{
  "target_defaults": {
    "default_configuration": "Debug",
    # enable android short names (not full paths) for linking libraries
    "android_unmangled_name": 1,
    'cflags':    [ '-gdwarf-2', '-Werror', '-Wall', '-Wextra', '-Wno-missing-field-initializers' ],
    'cflags_cc': [ '-std=c++11', '-frtti', '-fexceptions', '-Wno-literal-suffix' ],
    'xcode_settings': {
      'OTHER_CFLAGS' : ['-Wall'],
      'OTHER_CPLUSPLUSFLAGS' : ['-Wall'],
      'CLANG_CXX_LANGUAGE_STANDARD': 'c++11',
      'CLANG_CXX_LIBRARY': 'libc++',
      'DEAD_CODE_STRIPPING': 'YES',
      'SKIP_INSTALL': 'YES',
      'CLANG_ENABLE_OBJC_ARC': 'YES',
    },
    "conditions": [
        ['OS=="ios"', {
          "xcode_settings" : {
            'SDKROOT': 'iphoneos',
            'SUPPORTED_PLATFORMS': 'iphonesimulator iphoneos',
          }
        }]
    ],
    'configurations': {
      'Debug': {
        # do _not_ put defines here, unless you add them in cflags as well
        # ios/clang doesn't respect them :(
        'defines': [ 'DEBUG=1' ],
        'cflags' : [ '-g', '-O0', '-DDEBUG=1' ],
        'xcode_settings': {
          'ONLY_ACTIVE_ARCH': 'YES',
        },
      },
      'Release': {
        'defines': [
          'NDEBUG=1',
        ],
        'cflags': [
          '-Os',
          '-fomit-frame-pointer',
          '-fdata-sections',
          '-ffunction-sections',
          '-DNDEBUG=1',
        ],
        'xcode_settings': {
          'DEAD_CODE_STRIPPING': 'YES',
        },
      },
    },
  },
}
