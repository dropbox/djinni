//
// Copyright 2015 Dropbox, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.dropbox.djinni;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for loading native libraries containing djinni interfaces
 * and records. To load libraries in your application at startup,
 * simply place them:
 *  * inside the jar containing this code at the (jar-relative)
 *      path `djinniNativeLibsJarPath`
 *  * somewhere on the host filesystem and use the `djinniNativeLibsSysProp`
 *      system property to provide path(s)
 *
 * Then execute:
 * <code>
 *     NativeLibLoader.loadLibs()
 * </code>
 * to load the libraries (e.g. in your main() or a class static initializer).
 */
public class NativeLibLoader {

    /**
     * Canonical directory in a jar containing (djinni-adapted) native libraries
     */
    public static final String djinniNativeLibsJarPath =
            "resources/djinni_native_libs";

    /**
     * Load native libraries with djinni support from the (comma-separated)
     * path(s) provided in this system property
     */
    public static final String djinniNativeLibsSysProp =
            "djinni.native_libs_dirs";

    private static final Logger log =
            Logger.getLogger(NativeLibLoader.class.getName());

    private NativeLibLoader() { }

    // Load native libs from canonical locations
    public static void loadLibs() throws URISyntaxException, IOException {
        // Try to load from Jar
        loadLibsFromJarPath(djinniNativeLibsJarPath);

        // Try to load from system
        String localPaths = System.getProperty(djinniNativeLibsSysProp);
        if (localPaths != null) {
            log.log(Level.FINE, "Loading local native libs");
            for (String localPath : localPaths.split(",")) {
                loadLibsFromLocalPath(Paths.get(localPath));
            }
        }
    }

    // Load native lib(s) from the given `localPath` - a file or directory
    public static void loadLibsFromLocalPath(Path localPath) throws IOException {
        File localFile = localPath.toFile();
        if (!localFile.exists()) { return; }
        if (localFile.isDirectory()) {
            log.log(Level.FINE, "Loading all libs in " + localFile.getAbsolutePath());
            for (File f : localFile.listFiles()) {
                if (f.isFile()) {
                    loadLibrary(f.getAbsolutePath());
                }
            }
        } else {
            loadLibrary(localFile.getAbsolutePath());
        }
    }

    // Load a directory of libs from a jar resource path
    public static void loadLibsFromJarPath(String jarPath)
            throws URISyntaxException, IOException {

        // Do we have a valid path?
        URL libsURL =
                NativeLibLoader.class
                    .getClassLoader()
                    .getResource(djinniNativeLibsJarPath);
        if (libsURL == null) { return; }

        // Are we actually referencing a jar path?
        if (!libsURL.toURI().getScheme().equals("jar")) { return; }

        log.log(Level.FINE, "Loading libs from jar path " + jarPath);

        // Walk the directory and load libs
        FileSystem fs =
                FileSystems.newFileSystem(libsURL.toURI(), Collections.<String, String>emptyMap());
        Path myPath = fs.getPath(jarPath);

        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(myPath);
        try {
            for (Path p : directoryStream) {
                loadLibFromJarPath(p);
            }
        } finally {
            directoryStream.close();
        }

        fs.close();
    }

    // Load a single native lib from a jar resource with path `libPath`
    public static void loadLibFromJarPath(Path libPath) throws IOException {

        // System libraries *must* be loaded from the filesystem,
        // so we must copy the lib's data from the jar to a tempfile

        InputStream libIn =
            NativeLibLoader.class.getResourceAsStream(libPath.toString());
        if (libIn == null) { return; } // Invalid `libPath`

        // Name the tempfile
        String libName = libPath.getName(libPath.getNameCount() - 1).toString();
            // Name the tempfile after the lib to ease debugging
        String suffix = null;
        int extPos = libName.lastIndexOf('.');
        if (extPos > 0) { suffix = libName.substring(extPos + 1); }
            // Try to suffix the tempfile with the lib's suffix so that other
            // tools (e.g. profilers) identify the file correctly

        File tempLib = File.createTempFile(libName, suffix);
        tempLib.deleteOnExit();

        log.log(
            Level.FINE,
            "Copying jar lib " + libPath + " to " + tempLib.getAbsolutePath());
        try {
            Files.copy(libIn, tempLib.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (SecurityException e) {
            throw new RuntimeException(
                    "SecurityException while trying to create tempfile: " +
                        e.getMessage() + "\n\n If you cannot grant this process " +
                        "permissions to create temporary files, you need to install " +
                        "the native libraries manually and provide the installation " +
                        "path(s) using the system property " + djinniNativeLibsSysProp);
        }

        loadLibrary(tempLib.getAbsolutePath());
    }

    private static void loadLibrary(String abspath) {
        System.load(abspath);
        log.log(Level.INFO, "Loaded " + abspath);
    }
}
