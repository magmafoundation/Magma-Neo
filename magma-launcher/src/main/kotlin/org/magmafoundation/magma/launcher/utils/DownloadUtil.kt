/*
 * Magma Server
 * Copyright (C) 2019-2025.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.magmafoundation.magma.launcher.utils

import dev.vankka.dependencydownload.DependencyManager
import dev.vankka.dependencydownload.common.util.HashUtil
import dev.vankka.dependencydownload.dependency.Dependency
import dev.vankka.dependencydownload.repository.Repository
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object DownloadUtil {

    /**
     * Downloads a file from the specified URL, verifies its hash, and saves it to the target path.
     */
    fun downloadFile(url: String, expectedHash: String, targetPath: Path) {
        if (!Files.exists(targetPath.parent)) {
            Files.createDirectories(targetPath.parent)
        }

        val tempFile = Files.createTempFile("download", ".tmp")
        try {
            val input = URL(url).openStream()
            try {
                val output = Files.newOutputStream(tempFile)
                try {
                    input.copyTo(output)
                } finally {
                    output.close()
                }
            } finally {
                input.close()
            }

            val fileHash = HashUtil.getFileHash(tempFile.toFile(), "sha256")
            if (fileHash != expectedHash) {
                throw RuntimeException("Hash mismatch for $url: $fileHash != $expectedHash")
            }

            Files.move(tempFile, targetPath)
        } catch (e: Exception) {
            Files.deleteIfExists(tempFile)
            throw e
        }
    }

    /**
     * Downloads a dependency from the provided repositories.
     * from: https://github.com/Vankka/DependencyDownload/blob/8be258ee186fd188e30ea79b2047dfc78e86bcf3/runtime/src/main/java/dev/vankka/dependencydownload/DependencyManager.java#L503
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class)
    fun downloadDependency(
        manager: DependencyManager, 
        dependency: Dependency, 
        repositories: List<Repository>
    ) {
        if (repositories.isEmpty()) {
            throw RuntimeException("No repositories provided")
        }

        val dependencyPath = manager.getPathForDependency(dependency, false)

        // Create parent directories if they don't exist
        if (!Files.exists(dependencyPath.parent)) {
            Files.createDirectories(dependencyPath.parent)
        }

        // Check if the dependency is already downloaded with the correct hash
        if (Files.exists(dependencyPath)) {
            val fileHash = HashUtil.getFileHash(dependencyPath.toFile(), dependency.getHashingAlgorithm())
            if (fileHash == dependency.getHash()) {
                // This dependency is already downloaded & the hash matches
                return
            } else {
                Files.delete(dependencyPath)
            }
        }

        Files.createFile(dependencyPath)

        val failure = RuntimeException("All provided repositories failed to download dependency")
        var anyFailures = false

        for (repository in repositories) {
            try {
                val digest = MessageDigest.getInstance(dependency.getHashingAlgorithm())
                downloadFromRepository(dependency, repository, dependencyPath, digest)

                val hash = HashUtil.getHash(digest)
                val dependencyHash = dependency.getHash()
                if (hash != dependencyHash) {
                    throw RuntimeException("Failed to verify file hash: $hash should've been: $dependencyHash")
                }

                // Success
                return
            } catch (e: Throwable) {
                Files.deleteIfExists(dependencyPath)
                failure.addSuppressed(e)
                anyFailures = true
            }
        }

        if (!anyFailures) {
            throw RuntimeException("Nothing failed yet nothing passed")
        }

        throw failure
    }

    /**
     * Downloads a dependency from a specific repository.
     * from: https://github.com/Vankka/DependencyDownload/blob/8be258ee186fd188e30ea79b2047dfc78e86bcf3/runtime/src/main/java/dev/vankka/dependencydownload/DependencyManager.java#L552
     */
    @Throws(Throwable::class)
    private fun downloadFromRepository(
        dependency: Dependency?, 
        repository: Repository, 
        dependencyPath: Path, 
        digest: MessageDigest
    ) {
        val connection = repository.openConnection(dependency)
        val buffer = ByteArray(repository.getBufferSize())

        val inputStream = BufferedInputStream(connection.getInputStream())
        try {
            val outputStream = BufferedOutputStream(Files.newOutputStream(dependencyPath))
            try {
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    digest.update(buffer, 0, bytesRead)
                }
            } finally {
                outputStream.close()
            }
        } finally {
            inputStream.close()
        }
    }

    /**
     * Loads a dependency into the classpath.
     * from: https://github.com/Vankka/DependencyDownload/blob/8be258ee186fd188e30ea79b2047dfc78e86bcf3/runtime/src/main/java/dev/vankka/dependencydownload/DependencyManager.java#L586
     */
    fun loadDependency(manager: DependencyManager, dependency: Dependency, classpathAppender: (Path) -> Unit) {
        classpathAppender(manager.getPathForDependency(dependency, false))
    }
}
