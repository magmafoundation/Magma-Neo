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

package org.magmafoundation.magma.launcher.installer

import dev.vankka.dependencydownload.DependencyManager
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
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption


class MagmaInstaller {

    private val baseDirPath: Path
        get() = JarTool.getJarDir()?.toPath()
            ?: throw IllegalStateException("Could not determine the base directory path.")

    private val librariesPath: Path
        get() = baseDirPath.resolve("libraries")

    var installerTourls: MutableList<java.net.URL?> = ArrayList<URL?>()

    var magmaVersion: String? = null
    var neoForgeVersion: String? = null
    var mcpVersion: String? = null
    var minecraftVersion: String? = null

    var forgeStart: String? = null
    var universalJar: File? = null

    var lzma: File? = null

    var minecraftServerJar: File? = null

    var installerLog: PrintStream

    constructor(version: String, neoForgeVersion: String) {
        init()

        val out = File("logs/installer.log")
        if (!out.exists()) {
            out.getParentFile().mkdirs()
            out.createNewFile()
        } else {
            out.delete()
            out.createNewFile()
        }
        this.installerLog = PrintStream(BufferedOutputStream(FileOutputStream(out)))

        this.magmaVersion = version
        this.neoForgeVersion = neoForgeVersion
        this.mcpVersion = "1.21.1-20240808.144430"
        this.minecraftVersion = "1.21.1"

        this.forgeStart = "$librariesPath/net/neoforged/neoforge/$version/neoforge-$version"
        this.universalJar = File("$forgeStart-universal.jar")

        this.lzma = File(librariesPath.toFile(), "org/magmafoundation/installer/data/server.lzma")

        this.minecraftServerJar =
            librariesPath.resolve("net/minecraft/server/$minecraftVersion/server-$minecraftVersion.jar").toFile()

        install()
    }

    private fun init() {
        // Download installer dependencies
        val dependencyPathProvider: DependencyPathProvider = object : CleanupPathProvider {
            override fun getCleanupPath(): Path = baseDirPath

            override fun getDependencyPath(dependency: Dependency, relocated: Boolean): Path {
                return librariesPath.resolve(dependency.getGroupId().replace(".", "/"))
                    .resolve(dependency.getArtifactId()).resolve(dependency.getVersion())
                    .resolve(dependency.getFileName())
            }
        }

        val manager = DependencyManager(dependencyPathProvider)

        manager.addDependency(
            StandardDependency(
                "net.neoforged.installertools",
                "binarypatcher",
                "2.1.2",
                null,
                "4fae71426631ce9972358cea5d40f9f3ed1805bb212f3be6c05efd071203a5a7",
                "sha256"
            )
        )
        manager.addDependency(
            StandardDependency(
                "net.neoforged.installertools",
                "cli-utils",
                "2.1.2",
                null,
                "023a5a0cc7579fbf10a0bdd2a47a8ac1a10046a6432fef36ec850ab1ccf5504d",
                "sha256"
            )
        )
        manager.addDependency(
            StandardDependency(
                "net.neoforged.installertools",
                "installertools",
                "2.1.2",
                null,
                "fc695a63f7cdc7f673f7a94d94c9f35518850594bd8fa5c933b0fff38352d8bb",
                "sha256"
            )
        )
        manager.addDependency(
            StandardDependency(
                "net.neoforged.installertools",
                "jarsplitter",
                "2.1.2",
                null,
                "fe98ac94c75afcf77b1b6156effcffe72140d861f7e165303c35a5abe1a4f834",
                "sha256"
            )
        )
        manager.addDependency(
            StandardDependency(
                "net.neoforged",
                "srgutils",
                "1.0.0",
                null,
                "16663ae2504a0522cca386af6c11281d3fde61d25b6bd2690d7200e900b26c36",
                "sha256"
            )
        )
        manager.addDependency(
            StandardDependency(
                "net.neoforged",
                "AutoRenamingTool",
                "2.0.3",
                "all",
                "43fcb978664ba04fc0b3b1b1a14eccabb4bc87b7a415e0eb8bdd1c2f12324321",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "net.neoforged",
                "AutoRenamingTool",
                "2.0.3",
                "all",
                "43fcb978664ba04fc0b3b1b1a14eccabb4bc87b7a415e0eb8bdd1c2f12324321",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "net.sf.jopt-simple",
                "jopt-simple",
                "5.0.4",
                null,
                "df26cc58f235f477db07f753ba5a3ab243ebe5789d9f89ecf68dd62ea9a66c28",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "com.google.code.gson",
                "gson",
                "2.10.1",
                null,
                "4241c14a7727c34feea6507ec801318a3d4a90f070e4525681079fb94ee4c593",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "de.siegmar",
                "fastcsv",
                "2.0.0",
                null,
                "665d52cb6f35ca236919be9ac81653c208628507f91d6bf13479bc11496db616",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "org.ow2.asm",
                "asm",
                "9.7",
                null,
                "adf46d5e34940bdf148ecdd26a9ee8eea94496a72034ff7141066b3eea5c4e9d",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "org.ow2.asm",
                "asm-analysis",
                "9.7",
                null,
                "7bc6bcbc21379948a0c8c467fb0f864206e5b818f6bc0b546872f5c9f941556f",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "org.ow2.asm",
                "asm-commons",
                "9.7",
                null,
                "389bc247958e049fc9a0408d398c92c6d370c18035120395d4cba1d9d9304b7a",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "org.ow2.asm",
                "asm-tree",
                "9.7",
                null,
                "62f4b3bc436045c1acb5c3ba2d8ec556ec3369093d7f5d06c747eb04b56d52b1",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "org.ow2.asm",
                "asm-util",
                "9.7",
                null,
                "37a6414d36641973f1af104937c95d6d921b2ddb4d612c66c5a9f2b13fc14211",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "org.apache.commons",
                "commons-text",
                "1.3",
                null,
                "8185b3a5311092d83ed1f184c2d093b3105d726bbd76867c32b3511542bb99a8",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "org.apache.commons",
                "commons-lang3",
                "3.14.0",
                null,
                "7b96bf3ee68949abb5bc465559ac270e0551596fa34523fddf890ec418dde13c",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "commons-beanutils",
                "commons-beanutils",
                "1.9.3",
                null,
                "c058e39c7c64203d3a448f3adb588cb03d6378ed808485618f26e137f29dae73",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "org.apache.commons",
                "commons-collections4",
                "4.2",
                null,
                "6a594721d51444fd97b3eaefc998a77f606dedb03def494f74755aead3c9df3e",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "commons-logging",
                "commons-logging",
                "1.2",
                null,
                "daddea1ea0be0f56978ab3006b8ac92834afeefbd9b7e4e6316fca57df0fa636",
                "sha256"
            )
        )

        manager.addDependency(
            StandardDependency(
                "net.md-5",
                "SpecialSource",
                "1.11.2",
                null,
                "cf5f1542daef5b332eaa01796eb0fc7d25a2289ee9efbea330317da819840825",
                "sha256"
            )
        )

        val repositories = mutableListOf<Repository>(
            StandardRepository("https://libraries.minecraft.net/"),
            StandardRepository("https://repo.magmafoundation.org/releases")
        )

        val dependencies = manager.dependencies

        val progressBar =
            ProgressBarBuilder().setTaskName("Downloading installer dependencies...").setStyle(ProgressBarStyle.ASCII)
                .setUpdateIntervalMillis(100).setInitialMax(dependencies.size.toLong()).continuousUpdate()

        ProgressBar.wrap(dependencies.stream(), progressBar).forEach { dep ->
            dep?.let {
                try {
                    DownloadUtil.downloadDependency(manager, it, repositories)
                    DownloadUtil.loadDependency(manager, it) { path ->
                        installerTourls.add(path.toUri().toURL())
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun install() {

        val builder = ProgressBarBuilder()
            .setTaskName("Patching server...")
            .setStyle(ProgressBarStyle.ASCII)
            .setUpdateIntervalMillis(100)
            .setInitialMax(8)

        builder.build().use { pb ->
            copyFileFromJar(this.lzma!!, "data/server.lzma")
            copyFileFromJar(this.universalJar!!, "${forgeStart}/neoforge-$magmaVersion-universal.jar")

            if (this.minecraftServerJar!!.exists()) {
                mute()
                println("[STEP ONE] Extracting bundled resources...")
                launchService(
                    "net.neoforged.installertools.ConsoleTool",
                    "--task",
                    "BUNDLER_EXTRACT",
                    "--input", minecraftServerJar!!.absolutePath,
                    "--output", librariesPath.toAbsolutePath().toString(),
                    "--libraries"
                )
                unmute()
                pb.step()
            }
        }

    }

    @Throws(java.lang.Exception::class)
    protected fun launchService(mainClass: String?, vararg args: String) {
        var classPath = installerTourls
        val loader = URLClassLoader.newInstance(classPath.toTypedArray<URL?>())
        Class.forName(mainClass, true, loader).getDeclaredMethod("main", Array<String>::class.java)
            .invoke(null, *arrayOf<Any?>(args))
        loader.clearAssertionStatus()
        loader.close()
    }

    @Throws(Exception::class)
    protected fun copyFileFromJar(file: File, pathInJar: String?) {
        val `is` = MagmaInstaller::class.java.getClassLoader().getResourceAsStream(pathInJar)
        //todo: check if hash matches
        if (!file.exists()) {
            file.getParentFile().mkdirs()
            file.createNewFile()
            if (`is` != null) Files.copy(`is`, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            else {
                println("The file " + file.getName() + " was not found in the jar.")
                System.exit(0)
            }
        }
    }

    protected fun mute() {
        System.setOut(installerLog)
    }

    protected fun unmute() {
        System.setOut(System.out)
    }
}


