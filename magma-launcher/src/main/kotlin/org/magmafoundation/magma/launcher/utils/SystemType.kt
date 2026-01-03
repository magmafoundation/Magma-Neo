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

package org.magmafoundation.magma.launcher.utils

object SystemType {
    enum class OS {
        WINDOWS, LINUX, MAC, SOLARIS
    }

    private var os: OS? = null

    fun getOS(): OS {
        if (os == null) {
            val operSys = System.getProperty("os.name").lowercase()
            os = when {
                "win" in operSys -> OS.WINDOWS
                "nix" in operSys || "nux" in operSys || "aix" in operSys -> OS.LINUX
                "mac" in operSys -> OS.MAC
                "sunos" in operSys -> OS.SOLARIS
                else -> OS.LINUX // Default to Linux for unknown systems
            }
        }
        return os!!
    }
}
