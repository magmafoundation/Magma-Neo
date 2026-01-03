/*
 * Magma Server
 * Copyright (C) 2019-2026.
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

package org.magmafoundation.magma.launcher

import cpw.mods.bootstraplauncher.BootstrapLauncher
import org.magmafoundation.magma.launcher.dep.DependenciesDownloader
import org.magmafoundation.magma.launcher.installer.MagmaInstaller
import org.magmafoundation.magma.launcher.utils.JarTool
import org.magmafoundation.magma.launcher.utils.ServerInitHelper
import org.magmafoundation.magma.launcher.utils.SystemType
import java.nio.file.spi.FileSystemProvider
import java.util.function.Consumer
import java.util.jar.Manifest


var ui: UI? = null

fun main(args: Array<String>) {

    ui = UI(!isArgumentPresent(args, "--no-ui"))

    ui?.display(version = getVersion(), neoForgeVersion = getNeoForgeVersion())

    var dependenciesDownloader: DependenciesDownloader = DependenciesDownloader()
    var libsToLoad = dependenciesDownloader.start()

    val magmaInstaller = MagmaInstaller(version = getVersion(), neoForgeVersion = getNeoForgeVersion())


    val launchArgs: MutableList<String?> = JarTool.readFileLinesFromJar(
        "data/" + (if (SystemType.getOS() == SystemType.OS.WINDOWS) "win" else "unix") + "_args.txt"
    )
    val forgeArgs: MutableList<String?> = ArrayList()
    launchArgs.stream().filter { s: String? ->
        s!!.startsWith("--launchTarget") || s.startsWith("--fml.neoForgeVersion") || s.startsWith("--fml.mcVersion") || s.startsWith("--fml.fmlVersion") || s.startsWith("--fml.neoFormVersion")
    }.toList().forEach(
        Consumer { arg: String? ->
            forgeArgs.add(arg?.split(" ")[0])
            forgeArgs.add(arg?.split(" ")[1])
        })

    ServerInitHelper.applyLaunchArgs(launchArgs as List<String>)

    BootstrapLauncher.main(*forgeArgs.filterNotNull().toTypedArray())

}

private fun isArgumentPresent(args: Array<String>, argument: String): Boolean {
    return args.any { it.equals(argument, ignoreCase = true) }
}

fun getVersion(): String {
    val version = object {}.javaClass.`package`?.implementationVersion
    return version ?: "dev-env"
}

fun getNeoForgeVersion(): String {
    val neoForgeVersion = object {}.javaClass.classLoader.getResourceAsStream("META-INF/MANIFEST.MF")?.use { inputStream ->
        val manifest = Manifest(inputStream)
        manifest.mainAttributes.getValue("NeoForge-Version")
    }
    return neoForgeVersion ?: "dev-env"
}
