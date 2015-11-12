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

#include "DJIObjCPlatformThreads.h"
#include "DBThreadFunc.h"


@interface DJIObjCPlatformThreadsInternalFunc : DBThreadFunc
- (instancetype)initWithBlock:(DJIThreadFuncBlock)block;
- (void)run;
@end


@implementation DJIObjCPlatformThreadsInternalFunc {
    DJIThreadFuncBlock _runBlock;
}

- (instancetype)initWithBlock:(DJIThreadFuncBlock)block {
    if (self = [super init]) {
        _runBlock = [block copy];
    }
    return self;
}

- (void)run {
    _runBlock();
}

@end


@implementation DJIObjCPlatformThreads {
    DJIObjCPlatformThreadsConfigBlock _cfgBlock;
}

- (id) init __attribute__((unavailable)) {
    return nil;
}

- (nullable instancetype)initWithConfigBlock:(DJIObjCPlatformThreadsConfigBlock)block {
    if (self = [super init]) {
        _cfgBlock = [block copy];
    }
    return self;
}

+ (nullable DJIObjCPlatformThreads *)platformThreads {
    return [DJIObjCPlatformThreads platformThreadsWithConfigBlock:nil];
}

+ (nullable DJIObjCPlatformThreads *)
    platformThreadsWithConfigBlock:(DJIObjCPlatformThreadsConfigBlock)block {
    return [[DJIObjCPlatformThreads alloc] initWithConfigBlock:block];
}

- (void)createThread:(nonnull NSString *)name
                func:(nonnull DBThreadFunc *)func {
    NSThread * thread = [[NSThread alloc] initWithTarget:func selector:@selector(run) object:nil];
    thread.name = name;
    if (_cfgBlock) {
        _cfgBlock(thread);
    }
    [thread start];
}

- (void)createThread:(nonnull NSString *)name
               block:(DJIThreadFuncBlock)block {
    [self createThread:name func:[[DJIObjCPlatformThreadsInternalFunc alloc] initWithBlock:block]];
}

- (nonnull NSNumber *)isMainThread {
    return @(NSThread.isMainThread);
}

@end
