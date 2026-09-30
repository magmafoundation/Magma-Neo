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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class StartupDiagnosticsTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `startup failure is saved with its cause and rethrown unchanged`() {
        val report = directory.resolve("startup-error.log").toFile()
        val failure = IllegalStateException("startup failed", IllegalArgumentException("root diagnostic cause"))

        val thrown = assertThrows(IllegalStateException::class.java) {
            withStartupErrorReport(report) { throw failure }
        }

        assertSame(failure, thrown)
        val text = report.readText(Charsets.UTF_8)
        assertTrue(text.contains("java.lang.IllegalStateException: startup failed"))
        assertTrue(text.contains("Caused by: java.lang.IllegalArgumentException: root diagnostic cause"))
        assertTrue(text.contains("StartupDiagnosticsTest"))
    }

    @Test
    fun `report write failure does not replace the startup failure`() {
        val failure = LinkageError("startup linkage failed")

        val thrown = assertThrows(LinkageError::class.java) {
            withStartupErrorReport(directory.toFile()) { throw failure }
        }

        assertSame(failure, thrown)
        assertTrue(thrown.suppressed.isNotEmpty())
    }

    @Test
    fun `successful startup does not create an error report`() {
        val report = directory.resolve("startup-error.log").toFile()
        var started = false

        withStartupErrorReport(report) { started = true }

        assertTrue(started)
        assertFalse(report.exists())
    }
}
