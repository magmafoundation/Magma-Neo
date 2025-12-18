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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import net.md_5.specialsource.repo.ClassRepo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * ClassLoaderRepo
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName ClassLoaderRepo
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ClassLoaderRepo.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class ClassLoaderRepo implements ClassRepo {
    private final ClassLoader classLoader;

    public ClassLoaderRepo(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassNode findClass(String internalName) {
        return findClass(internalName, ClassReader.SKIP_CODE);
    }

    public ClassNode findClass(String internalName, int parsingOptions) {
        URL url = classLoader instanceof URLClassLoader
                ? ((URLClassLoader) classLoader).findResource(internalName + ".class") // search local
                : classLoader.getResource(internalName + ".class");
        if (url == null) return null;
        try {
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                ClassReader reader = new ClassReader(inputStream);
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, parsingOptions);
                return classNode;
            }
        } catch (IOException ignored) {}
        return null;
    }
}
