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

package org.magmafoundation.magma.launcher

import org.magmafoundation.magma.launcher.dep.DependenciesDownloader
import org.magmafoundation.magma.launcher.installer.MagmaInstaller
import java.util.jar.Manifest


var ui: UI? = null

fun main(args: Array<String>) {

    ui = UI(!isArgumentPresent(args, "--no-ui"))

    ui?.display(version = getVersion(), neoForgeVersion = getNeoForgeVersion())

    var dependenciesDownloader: DependenciesDownloader = DependenciesDownloader()
    var libsToLoad = dependenciesDownloader.start()

    val magmaInstaller = MagmaInstaller(version = getVersion(), neoForgeVersion = getNeoForgeVersion())

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
