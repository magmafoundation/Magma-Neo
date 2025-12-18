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

package org.magmafoundation.magma.remapping.handlers;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.md_5.specialsource.provider.InheritanceProvider;
import net.md_5.specialsource.repo.ClassRepo;
import org.magmafoundation.magma.remapping.MagmaRemapper;
import org.objectweb.asm.tree.ClassNode;

/**
 * PluginInheritanceProvider
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName PluginInheritanceProvider
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/PluginInheritanceProvider.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class PluginInheritanceProvider implements InheritanceProvider {
    private static final Map<String, Collection<String>> SHARED_INHERITANCE_MAP = new ConcurrentHashMap<>();

    private final ClassRepo classRepo;

    public PluginInheritanceProvider(ClassRepo classRepo) {
        this.classRepo = classRepo;
    }

    @Override
    public Collection<String> getParents(String className) {
        ClassNode node = classRepo.findClass(className);
        if (node == null) return Collections.emptyList();

        Collection<String> parents = new HashSet<>(node.interfaces);
        if (node.superName != null) {
            parents.add(node.superName);
        }

        return parents;
    }

    public Collection<String> getAll(String className) {
        Collection<String> collection = SHARED_INHERITANCE_MAP.get(className);
        if (collection != null) return collection;

        ClassNode node = classRepo.findClass(className);
        if (node == null) return ImmutableSet.of("java/lang/Object");
        Collection<String> parents = new HashSet<>(node.interfaces);
        parents.add(node.name);
        if (node.superName != null) {
            parents.add(node.superName);
            parents.addAll(getAll(node.superName));
        } else {
            parents.add("java/lang/Object");
        }

        SHARED_INHERITANCE_MAP.put(className, parents);
        return parents;
    }

    public static class Remapping extends PluginInheritanceProvider {
        private final PluginInheritanceProvider provider;

        public Remapping(ClassRepo classRepo, PluginInheritanceProvider provider) {
            super(classRepo);
            this.provider = provider;
        }

        @Override
        public Collection<String> getAll(String className) {
            return provider.getAll(className).stream().map(MagmaRemapper.getNmsMapper()::map).collect(Collectors.toSet());
        }

        @Override
        public Collection<String> getParents(String className) {
            return provider.getParents(className).stream().map(MagmaRemapper.getNmsMapper()::map).collect(Collectors.toSet());
        }
    }
}
