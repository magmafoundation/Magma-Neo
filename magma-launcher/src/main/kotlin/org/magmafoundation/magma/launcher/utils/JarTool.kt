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

import java.io.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

object JarTool {

    fun getJarPath(): String? {
        val file = getFile()
        if (file == null) {
            return null
        }
        return file.getAbsolutePath()
    }

    fun getJarDir(): File? {
        val file = getFile()
        if (file == null) {
            return null
        }
        return getFile().getParentFile()
    }

    fun getJarName(): String? {
        val file = getFile()
        if (file == null) {
            return null
        }
        return getFile().getName()
    }

    fun getFile(): File {
        var path = JarTool::class.java.getProtectionDomain().getCodeSource()
            .getLocation().getFile()
        path = URLDecoder.decode(path, StandardCharsets.UTF_8)
        return File(path)
    }

    fun inputStreamFile(inputStream: InputStream, targetFilePath: String) {
        val file = File(targetFilePath)
        try {
            val os: OutputStream = FileOutputStream(file)
            var bytesRead = 0
            val buffer = ByteArray(8192)
            while ((inputStream.read(buffer, 0, 8192).also { bytesRead = it }) != -1) {
                os.write(buffer, 0, bytesRead)
            }
            os.flush()
            os.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readFileLinesFromJar(path: String?): MutableList<String?> {
        try {
            BufferedReader(
                InputStreamReader(
                    Objects.requireNonNull<InputStream?>(
                        JarTool::class.java.getClassLoader().getResourceAsStream(path)
                    )
                )
            ).use { br ->
                val lines: MutableList<String?> = ArrayList<String?>()
                var line: String?
                while ((br.readLine().also { line = it }) != null) lines.add(line)
                return lines
            }
        } catch (e: Exception) {
            return mutableListOf<String?>()
        }
    }
}
