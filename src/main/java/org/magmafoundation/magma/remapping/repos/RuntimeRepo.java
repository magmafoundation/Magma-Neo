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

package org.magmafoundation.magma.remapping.repos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.specialsource.repo.ClassRepo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * RuntimeRepo
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName RuntimeRepo
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/RuntimeRepo.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class RuntimeRepo implements ClassRepo {
    private final Map<String, ClassNode> map = new ConcurrentHashMap<>();

    @Override
    public ClassNode findClass(String internalName) {
        return map.get(internalName);
    }

    public void put(byte[] bytes) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, ClassReader.SKIP_CODE);
        this.map.put(reader.getClassName(), node);
    }
}
