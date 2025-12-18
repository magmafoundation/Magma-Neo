/*
 * Magma Server
 * Copyright (C) 2019-2023.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.magmafoundation.magma.remapping.loaders;

import org.magmafoundation.magma.remapping.ClassLoaderRemapper;

/**
 * RemappingClassLoader
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName RemappingClassLoader
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/RemappingClassLoader.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public interface RemappingClassLoader {
    ClassLoaderRemapper getRemapper();
}
