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

#include <Foundation/Foundation.h>
#include "DJIError.h"
#include <exception>
static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

namespace djinni {

[[noreturn]] __attribute__((weak)) void throwUnimplemented(const char * /*ctx*/, NSString * message) {
    [NSException raise:NSInternalInconsistencyException format:@"Unimplemented: %@", message];
    __builtin_unreachable();
}

[[noreturn]] __attribute__((weak)) void throwNSExceptionFromCurrent(const char * /*ctx*/) {
    try {
        throw;
    } catch (const std::exception & e) {
        NSString *message = [NSString stringWithCString:e.what() encoding:NSUTF8StringEncoding];
        [NSException raise:message format:@"%@", message];
        __builtin_unreachable();
    }
}

} // namespace djinni
