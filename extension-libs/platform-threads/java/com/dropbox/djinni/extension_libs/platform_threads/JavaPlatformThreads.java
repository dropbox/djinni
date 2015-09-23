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
// You may need to adjust names and packages to match the settings of code
// generation in your project.  You may also need to add a catch clause if you
// configure Djinni to declare checked exceptions on generated methods.
//

package com.dropbox.djinni.extension_libs.platform_threads;

import com.dropbox.djinni.generated.PlatformThreads;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Cross-language platform thread implementation using java.thread.Thread.
 * Create a subclass and override configureThread() to customize the created threads.
 */
public class JavaPlatformThreads extends PlatformThreads {
    /**
     * Creates an instance.
     */
    public JavaPlatformThreads() {}

    /** Creates and starts a new thread which will call the given function. */
    @Override
    public void createThread(@Nonnull String name, @Nonnull ThreadFunc func) {
        final ThreadFunc passFunc = func;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                passFunc.run();
            }
        }, name);
        thread.setDaemon(true);
        configureThread(thread);
        thread.start();
    }

    /**
     * Determines whether the calling thread is the main UI thread of the
     * app.  Some platforms do not have a notion of a main thread, in which
     * case this method returns null.
     *
     * This implementation returns null since Java doesn't have any default
     * notion of a main/UI thread.  Platform-specific subclasses may override.
     */
    @Override
    @CheckForNull
    public Boolean isMainThread() {
        return null;
    }

    /**
     * Called after each thread is created, but before it is started.
     * The default implementation does nothing, resulting in a default thread configuration.
     */
    protected void configureThread(Thread thread) {}
}
