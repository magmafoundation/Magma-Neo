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

import java.text.SimpleDateFormat
import java.util.Date

class UI(enabled: Boolean) {

    private val enabled: Boolean = enabled

    var title: String = "______  ___                                     _____   __           \n" +
            "___   |/  /_____ _______ _______ _________ _    ___  | / /__________ \n" +
            "__  /|_/ /_  __ `/_  __ `/_  __ `__ \\  __ `/    __   |/ /_  _ \\  __ \\\n" +
            "_  /  / / / /_/ /_  /_/ /_  / / / / / /_/ /     _  /|  / /  __/ /_/ /\n" +
            "/_/  /_/  \\__,_/ _\\__, / /_/ /_/ /_/\\__,_/      /_/ |_/  \\___/\\____/ \n" +
            "                 /____/                                              \n"

    fun display(version: String, neoForgeVersion: String) {
        if (!enabled) {
            return
        }

        println(title)
        println("Copyright (C) 2019-" + SimpleDateFormat("yyyy").format(Date()) + " Magma Foundation")
        println("-----------------------------------------")
        println("Running on Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")")
        println("Magma version $version")
        println("NeoForge version $neoForgeVersion")
        println("-----------------------------------------")
    }
}
