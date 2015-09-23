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

//
// Note: This is a sample implementation which interacts with Djinni-generated code.
// You may need to adjust names or include paths to match the settings of code
// generation in your project.
//

#include "DBPlatformThreads.h"


typedef void (^ __nullable DJIObjCPlatformThreadsConfigBlock)(NSThread * __nonnull thread);
typedef void (^ __nonnull DJIThreadFuncBlock)();


/**
 * Cross-language platform thread implementation using NSThread.
 */
@interface DJIObjCPlatformThreads : NSObject<DBPlatformThreads>

- (nullable instancetype)init __attribute__((unavailable("Please use factory method instead.")));

/** Creates an object which produces threads with default configuration. */
+ (nullable DJIObjCPlatformThreads *)platformThreads;

/**
 * Creates an object which calls the given block on each thread after it's created,
 * but before it's started.  A nil block will be ignored.
 */
+ (nullable DJIObjCPlatformThreads *)
    platformThreadsWithConfigBlock:(DJIObjCPlatformThreadsConfigBlock)block;

/** Creates and starts a new thread which will call the given function. */
- (void)createThread:(nonnull NSString *)name
                func:(nonnull DBThreadFunc *)func;

/** Creates and starts a new thread which will call the given block. */
- (void)createThread:(nonnull NSString *)name
               block:(DJIThreadFuncBlock)block;

/**
 * Determines whether the calling thread is the main UI thread of the
 * app.  Some platforms do not have a notion of a main thread, in which
 * case this method returns null.
 *
 * This implementation returns YES or NO depending on NSThread's notion
 * of the main thread.
 */
- (nonnull NSNumber *)isMainThread;

@end
