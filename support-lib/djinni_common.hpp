//
// Copyright 2015 Dropbox, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

#pragma once

#ifdef _MSC_VER
    #define DJINNI_WEAK_DEFINITION // weak attribute not supported by MSVC
    #define DJINNI_NORETURN_DEFINITION __declspec(noreturn)
    #if _MSC_VER < 1900 // snprintf not implemented prior to VS2015
        #define DJINNI_SNPRINTF snprintf
        #define noexcept _NOEXCEPT // work-around for missing noexcept VS2015
        #define constexpr // work-around for missing constexpr VS2015
    #else
        #define DJINNI_SNPRINTF _snprintf
    #endif
#else
    #define DJINNI_WEAK_DEFINITION __attribute__((weak))
    #define DJINNI_NORETURN_DEFINITION __attribute__((noreturn))
    #define DJINNI_SNPRINTF snprintf
#endif
