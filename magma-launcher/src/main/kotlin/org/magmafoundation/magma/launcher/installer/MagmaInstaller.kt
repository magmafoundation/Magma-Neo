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
import dev.vankka.dependencydownload.common.util.HashUtil
import dev.vankka.dependencydownload.dependency.Dependency
import dev.vankka.dependencydownload.dependency.StandardDependency
import dev.vankka.dependencydownload.path.CleanupPathProvider
import dev.vankka.dependencydownload.path.DependencyPathProvider
import dev.vankka.dependencydownload.repository.StandardRepository
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.magmafoundation.magma.launcher.utils.DownloadUtil
import org.magmafoundation.magma.launcher.utils.JarTool
import java.io.*
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.jar.JarFile
import kotlin.system.exitProcess

/**
 * Handles the installation process for Magma Server
 */
class MagmaInstaller(version: String, neoForgeVersion: String) {

    // Paths
    private val baseDirPath: Path
        get() = JarTool.getJarDir()?.toPath()
            ?: throw IllegalStateException("Could not determine the base directory path.")

    private val librariesPath: Path
        get() = baseDirPath.resolve("libraries")

    // Version information
    private val magmaVersion: String = version
    private val neoForgeVersion: String = neoForgeVersion
    private val neoformVersion: String = "1.21.1-20240808.144430"
    private val minecraftVersion: String = "1.21.1"

    // Installer URLs and logging
    private val installerUrls: MutableList<URL?> = ArrayList()
    private val installerLog: PrintStream
    private val originalOut: PrintStream = System.out

    // Files and paths
    private val installInfo: File
    private val forgeStart: String
    private val universalJar: File
    private val serverJar: File
    private val lzma: File
    private val mcStart: File
    private val mcUnpacked: File
    private val mojmap: File
    private val mcSlim: File
    private val mcSRG: File
    private val mcExtra: File
    private val minecraftServerJar: File
    private val neoFormStart: File
    private val neoFormZip: File
    private val mappings: File
    private val mergedMappings: File

    init {
        // Setup logging
        val logFile = File("logs/installer.log").apply {
            parentFile.mkdirs()
            if (exists()) delete()
            createNewFile()
        }
        installerLog = PrintStream(BufferedOutputStream(FileOutputStream(logFile)), true)

        // Initialize file paths
        installInfo = File("${librariesPath}/org/magmafoundation/installer/install_info.txt")
        forgeStart = "$librariesPath/net/neoforged/neoforge/$version/neoforge-$version"
        universalJar = File("$forgeStart-universal.jar")
        serverJar = File("$forgeStart-server.jar")
        lzma = File(librariesPath.toFile(), "org/magmafoundation/installer/data/server.lzma")

        mcStart = File("$librariesPath/net/minecraft/server/$neoformVersion/server-$neoformVersion")
        mcUnpacked = File("$mcStart-unpacked.jar")
        mojmap = File("$mcStart-mappings.txt")
        mcSlim = File("$mcStart-slim.jar")
        mcSRG = File("$mcStart-srg.jar")
        mcExtra = File("$mcStart-extra.jar")

        minecraftServerJar = librariesPath.resolve("net/minecraft/server/$minecraftVersion/server-$minecraftVersion.jar").toFile()

        neoFormStart = File("$librariesPath/net/neoforged/neoform/$neoformVersion/neoform-$neoformVersion")
        neoFormZip = File("$neoFormStart.zip")
        mappings = File("$neoFormStart-mappings.txt")
        mergedMappings = File("$neoFormStart-mappings-merged.txt")

        // Run installation process
        downloadDependencies()
        install()
    }

    /**
     * Downloads all dependencies required for the installer
     */
    private fun downloadDependencies() {
        val dependencyPathProvider: DependencyPathProvider = object : CleanupPathProvider {
            override fun getCleanupPath(): Path = baseDirPath

            override fun getDependencyPath(dependency: Dependency, relocated: Boolean): Path {
                return librariesPath.resolve(dependency.getGroupId().replace(".", "/"))
                    .resolve(dependency.getArtifactId()).resolve(dependency.getVersion())
                    .resolve(dependency.getFileName())
            }
        }

        val manager = DependencyManager(dependencyPathProvider)
        addInstallerDependencies(manager)

        val repositories = listOf(
            StandardRepository("https://libraries.minecraft.net/"),
            StandardRepository("https://repo.magmafoundation.org/releases")
        )

        val dependencies = manager.dependencies
        val progressBar = ProgressBarBuilder()
            .setTaskName("Downloading installer dependencies...")
            .setStyle(ProgressBarStyle.ASCII)
            .setUpdateIntervalMillis(100)
            .setInitialMax(dependencies.size.toLong())
            .continuousUpdate()
            .build()

        try {
            progressBar.use { pb ->
                dependencies.forEachIndexed { index, dep ->
                    dep?.let {
                        try {
                            DownloadUtil.downloadDependency(manager, it, repositories)
                            DownloadUtil.loadDependency(manager, it) { path ->
                                installerUrls.add(path.toUri().toURL())
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                    pb.step()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Adds all required dependencies to the dependency manager
     */
    private fun addInstallerDependencies(manager: DependencyManager) {
        val dependencies = listOf(
            StandardDependency(
                "net.neoforged.installertools", "binarypatcher", "2.1.2", "fatjar",
                "9d73d565b775c8ec9da83da6d2a25454c7aad95eba22f64def9261f214cc49ba", "sha256"
            ),
            StandardDependency(
                "net.neoforged.installertools", "cli-utils", "2.1.2", null,
                "023a5a0cc7579fbf10a0bdd2a47a8ac1a10046a6432fef36ec850ab1ccf5504d", "sha256"
            ),
            StandardDependency(
                "net.neoforged.installertools", "installertools", "2.1.2", null,
                "fc695a63f7cdc7f673f7a94d94c9f35518850594bd8fa5c933b0fff38352d8bb", "sha256"
            ),
            StandardDependency(
                "net.neoforged.installertools", "jarsplitter", "2.1.2", null,
                "fe98ac94c75afcf77b1b6156effcffe72140d861f7e165303c35a5abe1a4f834", "sha256"
            ),
            StandardDependency(
                "net.neoforged", "srgutils", "1.0.0", null,
                "16663ae2504a0522cca386af6c11281d3fde61d25b6bd2690d7200e900b26c36", "sha256"
            ),
            StandardDependency(
                "net.neoforged", "AutoRenamingTool", "2.0.3", "all",
                "43fcb978664ba04fc0b3b1b1a14eccabb4bc87b7a415e0eb8bdd1c2f12324321", "sha256"
            ),
            StandardDependency(
                "net.sf.jopt-simple", "jopt-simple", "5.0.4", null,
                "df26cc58f235f477db07f753ba5a3ab243ebe5789d9f89ecf68dd62ea9a66c28", "sha256"
            ),
            StandardDependency(
                "com.google.code.gson", "gson", "2.10.1", null,
                "4241c14a7727c34feea6507ec801318a3d4a90f070e4525681079fb94ee4c593", "sha256"
            ),
            StandardDependency(
                "de.siegmar", "fastcsv", "2.0.0", null,
                "665d52cb6f35ca236919be9ac81653c208628507f91d6bf13479bc11496db616", "sha256"
            ),
            StandardDependency(
                "org.ow2.asm", "asm", "9.7", null,
                "adf46d5e34940bdf148ecdd26a9ee8eea94496a72034ff7141066b3eea5c4e9d", "sha256"
            ),
            StandardDependency(
                "org.ow2.asm", "asm-analysis", "9.7", null,
                "7bc6bcbc21379948a0c8c467fb0f864206e5b818f6bc0b546872f5c9f941556f", "sha256"
            ),
            StandardDependency(
                "org.ow2.asm", "asm-commons", "9.7", null,
                "389bc247958e049fc9a0408d398c92c6d370c18035120395d4cba1d9d9304b7a", "sha256"
            ),
            StandardDependency(
                "org.ow2.asm", "asm-tree", "9.7", null,
                "62f4b3bc436045c1acb5c3ba2d8ec556ec3369093d7f5d06c747eb04b56d52b1", "sha256"
            ),
            StandardDependency(
                "org.ow2.asm", "asm-util", "9.7", null,
                "37a6414d36641973f1af104937c95d6d921b2ddb4d612c66c5a9f2b13fc14211", "sha256"
            ),
            StandardDependency(
                "org.apache.commons", "commons-text", "1.3", null,
                "8185b3a5311092d83ed1f184c2d093b3105d726bbd76867c32b3511542bb99a8", "sha256"
            ),
            StandardDependency(
                "org.apache.commons", "commons-lang3", "3.14.0", null,
                "7b96bf3ee68949abb5bc465559ac270e0551596fa34523fddf890ec418dde13c", "sha256"
            ),
            StandardDependency(
                "commons-beanutils", "commons-beanutils", "1.9.3", null,
                "c058e39c7c64203d3a448f3adb588cb03d6378ed808485618f26e137f29dae73", "sha256"
            ),
            StandardDependency(
                "org.apache.commons", "commons-collections4", "4.2", null,
                "6a594721d51444fd97b3eaefc998a77f606dedb03def494f74755aead3c9df3e", "sha256"
            ),
            StandardDependency(
                "commons-logging", "commons-logging", "1.2", null,
                "daddea1ea0be0f56978ab3006b8ac92834afeefbd9b7e4e6316fca57df0fa636", "sha256"
            ),
            StandardDependency(
                "net.md-5", "SpecialSource", "1.11.2", null,
                "cf5f1542daef5b332eaa01796eb0fc7d25a2289ee9efbea330317da819840825", "sha256"
            )
        )

        dependencies.forEach { manager.addDependency(it) }
    }

    /**
     * Runs the installation process
     */
    private fun install() {

        copyFileFromJar(lzma, "data/server.lzma", true)
        copyFileFromJar(
            universalJar,
            "maven/net/neoforged/neoforge/$magmaVersion/neoforge-$magmaVersion-universal.jar"
        )

        if(!needToPatchServer()) return;
        copyFileFromJar(
            universalJar,
            "maven/net/neoforged/neoforge/$magmaVersion/neoforge-$magmaVersion-universal.jar",
            true
        )

        if (!minecraftServerJar.exists()) {
            System.err.println("The server is missing essential files to install properly, delete your libraries folder and try again.")
            exitProcess(-1)
        }

        val progressBar = ProgressBarBuilder()
            .setTaskName("Patching server...")
            .setStyle(ProgressBarStyle.ASCII)
            .setUpdateIntervalMillis(100)
            .setInitialMax(8)
            .build()

        progressBar.use { pb ->
            // Step 1: Extract bundled resources
            mute()
            println("[STEP ONE] Extracting bundled resources...")
            pb.extraMessage = "Extracting bundled resources..."
            launchService(
                "net.neoforged.installertools.ConsoleTool",
                "--task", "BUNDLER_EXTRACT",
                "--input", minecraftServerJar.absolutePath,
                "--output", librariesPath.toAbsolutePath().toString(),
                "--libraries"
            )
            unmute()
            pb.step()

            // Step 2: Extract jars if needed
            if (!mcUnpacked.exists()) {
                mute()
                println("[STEP TWO] Extracting jars...")
                pb.extraMessage = "Extracting jars..."
                launchService(
                    "net.neoforged.installertools.ConsoleTool",
                    "--task", "BUNDLER_EXTRACT",
                    "--input", minecraftServerJar.absolutePath,
                    "--output", mcUnpacked.absolutePath,
                    "--jar-only"
                )
                unmute()
            }
            pb.step()

            // Step 3: Extract NeoForm mappings
            if (neoFormZip.exists() && !mappings.exists()) {
                mute()
                println("[STEP THREE] Extracting NeoForm mappings...")
                pb.extraMessage = "Extracting NeoForm mappings..."
                launchService(
                    "net.neoforged.installertools.ConsoleTool",
                    "--task", "MCP_DATA",
                    "--input", neoFormZip.absolutePath,
                    "--output", mappings.absolutePath,
                    "--key", "mappings"
                )
                unmute()
            }
            pb.step()

            // Clean up corrupted files
            checkAndCleanCorruptedFiles()

            // Step 4: Download Mojang mappings
            if (!mojmap.exists()) {
                mute()
                println("[STEP FOUR] Downloading mojang mappings...")
                pb.extraMessage = "Downloading Mojang mappings..."
                launchService(
                    "net.neoforged.installertools.ConsoleTool",
                    "--task", "DOWNLOAD_MOJMAPS",
                    "--version", minecraftVersion,
                    "--side", "server",
                    "--output", mojmap.absolutePath
                )
                unmute()
            }
            pb.step()

            // Step 5: Merge mappings
            if (!mergedMappings.exists()) {
                mute()
                println("[STEP FIVE] Merging mappings...")
                pb.extraMessage = "Merging mappings..."
                launchService(
                    "net.neoforged.installertools.ConsoleTool",
                    "--task", "MERGE_MAPPING",
                    "--left", mappings.absolutePath,
                    "--right", mojmap.absolutePath,
                    "--output", mergedMappings.absolutePath,
                    "--classes", "--fields", "--methods",
                    "--reverse-right"
                )
                unmute()
            }
            pb.step()

            // Step 6: Split server jar
            if (!mcSlim.exists() || !mcExtra.exists()) {
                mute()
                println("[STEP SIX] Splitting server jar...")
                pb.extraMessage = "Splitting server jar..."
                launchService(
                    "net.neoforged.jarsplitter.ConsoleTool",
                    "--input", mcUnpacked.absolutePath,
                    "--slim", mcSlim.absolutePath,
                    "--extra", mcExtra.absolutePath,
                    "--srg", mergedMappings.absolutePath
                )
                unmute()
            }
            pb.step()

            // Step 7: Create SRG jar
            if (!mcSRG.exists()) {
                mute()
                println("[STEP SEVEN] Creating srg jar file...")
                pb.extraMessage = "Creating srg jar file..."
                launchService(
                    "net.neoforged.art.Main",
                    "--input", mcSlim.absolutePath,
                    "--output", mcSRG.absolutePath,
                    "--names", mergedMappings.absolutePath,
                    "--ann-fix", "--ids-fix", "--src-fix", "--record-fix"
                )
                unmute()
            }
            pb.step()

            // Step 8: Patch the server if needed
            patchServerIfNeeded(pb)
            pb.step()
        }
    }

    /**
     * Checks for corrupted jar files and removes them
     */
    private fun checkAndCleanCorruptedFiles() {
        val filesToCheck = listOf(mcUnpacked, mcExtra, mcSlim, mcSRG)
        filesToCheck.forEach { file ->
            if (isCorrupted(file)) {
                file.delete()
            }
        }
    }

    /**
     * Patches the server jar if necessary
     */
    private fun patchServerIfNeeded(progressBar: ProgressBar) {
        var storedServerMD5: String? = null
        var storedMagmaMD5: String? = null
        var serverMD5: String? = HashUtil.getFileHash(JarTool.getFile(), "md5")
        val lzmaMD5: String = HashUtil.getFileHash(lzma, "md5")
        val universalMD5: String = HashUtil.getFileHash(universalJar, "md5")

        if (installInfo.exists()) {
            val infoLines = Files.readAllLines(installInfo.toPath())
            if (infoLines.isNotEmpty()) storedServerMD5 = infoLines[0]
            if (infoLines.size > 1) storedMagmaMD5 = infoLines[1]
        }

        val needsPatching = !serverJar.exists() || 
                          storedServerMD5 == null || 
                          storedMagmaMD5 == null || 
                          (storedServerMD5 != serverMD5) || 
                          (storedMagmaMD5 != lzmaMD5)

        if (needsPatching) {
            mute()
            println("[STEP EIGHT] Patching forge jar...")
            progressBar.extraMessage = "Patching forge jar..."
            launchService(
                "net.neoforged.binarypatcher.ConsoleTool",
                "--clean", mcSRG.absolutePath,
                "--output", serverJar.absolutePath,
                "--apply", lzma.absolutePath
            )
            unmute()
            serverMD5 = HashUtil.getFileHash(serverJar, "md5")
        }

        // Write installation info
        FileWriter(installInfo).use { fw ->
            fw.write("$serverMD5\n$lzmaMD5\n${universalMD5}\n")
        }
    }

    /**
     * Launches a service with the specified main class and arguments
     */
    @Throws(Exception::class)
    private fun launchService(mainClass: String?, vararg args: String) {
        var classPath = installerUrls
        val loader = URLClassLoader.newInstance(classPath.toTypedArray<URL?>())
        Class.forName(mainClass, true, loader).getDeclaredMethod("main", Array<String>::class.java)
            .invoke(null, *arrayOf<Any?>(args))
        loader.clearAssertionStatus()
        loader.close()
        installerLog.flush()
    }

    /**
     * Copies a file from the jar to the filesystem
     */
    @Throws(Exception::class)
    private fun copyFileFromJar(file: File, pathInJar: String?, removeIfExists: Boolean = false) {
        if (file.exists()) {
            if (removeIfExists) {
                file.delete()
            } else {
                return
            }
        }

        file.parentFile.mkdirs()
        file.createNewFile()
        
        MagmaInstaller::class.java.classLoader.getResourceAsStream(pathInJar)?.use { inputStream ->
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } ?: run {
            println("The file ${file.name} was not found in the jar.")
            exitProcess(0)
        }
    }

    /**
     * Redirects output to the installer log
     */
    private fun mute() {
        System.setOut(installerLog)
        println("Redirecting output to logs/installer.log")
    }

    /**
     * Restores output to the original stream
     */
    private fun unmute() {
        installerLog.println("--- End of this operation ---")
        installerLog.flush()
        System.setOut(originalOut)
    }

    /**
     * Checks if a jar file is corrupted
     */
    private fun isCorrupted(file: File): Boolean {
        if (!file.exists()) return false
        
        return try {
            JarFile(file).use { /* Just trying to open it */ }
            false
        } catch (e: IOException) {
            true
        }
    }

    private fun needToPatchServer(): Boolean {
        if(installInfo.exists()) {
            var lzmaMD5: String? = HashUtil.getFileHash(lzma, "md5")
            val lines = Files.readAllLines(installInfo.toPath())

            return lines.size < 3 || (lzmaMD5 != lines.get(1)) || !HashUtil.getFileHash(universalJar, "md5").equals(lines.get(2))
        }
        return true
    }

}
