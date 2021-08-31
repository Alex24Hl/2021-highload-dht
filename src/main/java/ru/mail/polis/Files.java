/*
 * Copyright 2020 (c) Odnoklassniki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.mail.polis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility methods for handling files.
 *
 * @author Vadim Tsesko
 */
public final class Files {
    private static final String TEMP_PREFIX = "highload-dht";

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Files() {
        // Don't instantiate
    }

    /**
     * Creates and returns a new unused yet temporary directory.
     */
    public static Path createTempDirectory() throws IOException {
        final Path data = java.nio.file.Files.createTempDirectory(TEMP_PREFIX);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (java.nio.file.Files.exists(data)) {
                    recursiveDelete(data);
                }
            } catch (IOException e) {
                log.error("Can't delete temporary directory: {}", data, e);
            }
        }));
        return data;
    }

    /**
     * Recursively removes path.
     */
    public static void recursiveDelete(final Path path) throws IOException {
        java.nio.file.Files.walkFileTree(
                path,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(
                            final Path file,
                            final BasicFileAttributes attrs) throws IOException {
                        java.nio.file.Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(
                            final Path dir,
                            final IOException exc) throws IOException {
                        java.nio.file.Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    /**
     * Recursively calculates directory size.
     */
    public static long directorySize(final Path path) throws IOException {
        final AtomicLong result = new AtomicLong(0L);
        java.nio.file.Files.walkFileTree(
                path,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(
                            final Path file,
                            final BasicFileAttributes attrs) {
                        result.addAndGet(attrs.size());
                        return FileVisitResult.CONTINUE;
                    }
                });
        return result.get();
    }
}