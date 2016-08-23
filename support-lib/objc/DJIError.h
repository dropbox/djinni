//
// Copyright 2014 Dropbox, Inc.
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

namespace djinni {

// Throws an exception for an unimplemented method call.
[[noreturn]] void throwUnimplemented(const char * ctx, NSString * msg);

// Helper function for exception translation. Do not call directly!
[[noreturn]] void throwNSExceptionFromCurrent(const char * ctx);

} // namespace djinni

#define DJINNI_UNIMPLEMENTED(msg) \
    ::djinni::throwUnimplemented(__PRETTY_FUNCTION__, msg);

#define DJINNI_TRANSLATE_EXCEPTIONS() \
    catch (const std::exception & e) { \
        ::djinni::throwNSExceptionFromCurrent(__PRETTY_FUNCTION__); \
    }
