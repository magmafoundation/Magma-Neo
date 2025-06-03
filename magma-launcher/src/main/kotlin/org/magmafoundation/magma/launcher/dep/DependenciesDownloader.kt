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

package org.magmafoundation.magma.launcher.dep

import dev.vankka.dependencydownload.DependencyManager
import dev.vankka.dependencydownload.common.util.HashUtil
import dev.vankka.dependencydownload.dependency.Dependency
import dev.vankka.dependencydownload.dependency.StandardDependency
import dev.vankka.dependencydownload.path.CleanupPathProvider
import dev.vankka.dependencydownload.path.DependencyPathProvider
import dev.vankka.dependencydownload.repository.Repository
import dev.vankka.dependencydownload.repository.StandardRepository
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.magmafoundation.magma.launcher.utils.DownloadUtil
import org.magmafoundation.magma.launcher.utils.JarTool
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

class DependenciesDownloader {
    private val baseDirPath: Path
        get() = JarTool.getJarDir()?.toPath()
            ?: throw IllegalStateException("Could not determine the base directory path.")

    private val librariesPath: Path
        get() = baseDirPath.resolve("libraries")

    fun start(): MutableList<String> {
        val dependencyPathProvider: DependencyPathProvider = object : CleanupPathProvider {
            override fun getCleanupPath(): Path = baseDirPath

            override fun getDependencyPath(dependency: Dependency, relocated: Boolean): Path {
                return librariesPath.resolve(dependency.getGroupId().replace(".", "/"))
                    .resolve(dependency.getArtifactId()).resolve(dependency.getVersion())
                    .resolve(dependency.getFileName())
            }
        }

        val manager = DependencyManager(dependencyPathProvider)
        manager.loadFromResource(URL("jar:file:${JarTool.getJarPath()}!/data/magma_libraries.txt"))

        val repositories = mutableListOf<Repository>(
            StandardRepository("https://libraries.minecraft.net/"),
            StandardRepository("https://repo.magmafoundation.org/releases")
        )

        val dependencies = manager.dependencies
        val loadedLibsPaths = mutableListOf<String>()

        val progressBar = ProgressBarBuilder().setTaskName("Loading libraries...").setStyle(ProgressBarStyle.ASCII)
            .setUpdateIntervalMillis(100).setInitialMax(dependencies.size.toLong()).continuousUpdate()

        ProgressBar.wrap(dependencies.stream(), progressBar).forEach { dep ->
            dep?.let {
                println("Downloading ${it.getFileName()}...")
                try {
                    DownloadUtil.downloadDependency(manager, it, repositories)
                    DownloadUtil.loadDependency(manager, it) { path ->
                        loadedLibsPaths.add(path.toFile().absolutePath)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

        downloadNeoForm(manager, repositories)
        downloadServer()

        return loadedLibsPaths
    }

    fun downloadNeoForm(manager: DependencyManager, repositories: List<Repository>) {
        val neoForgeDependency = object : StandardDependency(
            "net.neoforged",
            "neoform",
            "1.21.1-20240808.144430",
            "",
            "3fcdf0f0d115774cfceaf206a05a6dc964e2189bfd00f5b5f863e37f4b7733c7",
            "sha256"
        ) {
            override fun getFileName(): String = "${getArtifactId()}-${getVersion()}.zip"
        }

        try {
            println("Downloading NeoForm mappings...")
            DownloadUtil.downloadDependency(manager, neoForgeDependency, repositories)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun downloadServer() {
        val serverPath = librariesPath.resolve("net/minecraft/server/1.21.1/server-1.21.1.jar")

        if (Files.exists(serverPath)) {
            val fileHash = HashUtil.getFileHash(serverPath.toFile(), "sha256")
            if (fileHash == "c96394da86f9d9f9ef7ca2d2ee1f2f0980c29b7aa5c94b43c02c50435dbcf53f") {
                println("Server is already downloaded and verified.")
                return
            } else {
                Files.deleteIfExists(serverPath)
                println("Server hash mismatch, redownloading...")
            }
        }

        try {
            println("Downloading server...")
            DownloadUtil.downloadFile(
                "https://piston-data.mojang.com/v1/objects/450698d1863ab5180c25d7c804ef0fe6369dd1ba/server.jar",
                "c96394da86f9d9f9ef7ca2d2ee1f2f0980c29b7aa5c94b43c02c50435dbcf53f",
                librariesPath.resolve("net/minecraft/server/1.21.1/server-1.21.1.jar")
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
