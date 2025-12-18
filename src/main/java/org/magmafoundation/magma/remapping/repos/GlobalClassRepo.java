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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.md_5.specialsource.repo.ClassRepo;
import org.magmafoundation.magma.remapping.handlers.PluginInheritanceProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.MixinService;

/**
 * GlobalClassRepo
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName GlobalClassRepo
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/GlobalClassRepo.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class GlobalClassRepo implements ClassRepo {
    public static final GlobalClassRepo INSTANCE = new GlobalClassRepo();
    private static final PluginInheritanceProvider PROVIDER = new PluginInheritanceProvider(INSTANCE);
    private static final PluginInheritanceProvider REMAPPING = new PluginInheritanceProvider.Remapping(INSTANCE, PROVIDER);

    private final LoadingCache<String, ClassNode> cache = CacheBuilder.newBuilder().maximumSize(256)
            .expireAfterAccess(1, TimeUnit.MINUTES).build(CacheLoader.from(this::findParallel));
    private final Set<ClassRepo> repos = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final RuntimeRepo runtimeRepo = new RuntimeRepo();

    private GlobalClassRepo() {
        repos.add(this.runtimeRepo);
    }

    @Override
    public ClassNode findClass(String internalName) {
        try {
            return cache.get(internalName);
        } catch (Throwable e) {
            return null;
        }
    }

    public ClassNode findClass(String internalName, int parsingOptions) {
        if (parsingOptions == ClassReader.SKIP_CODE) {
            return findClass(internalName);
        }
        return null;
    }

    private ClassNode findParallel(String internalName) {
        return this.repos.parallelStream()
                .map(it -> it.findClass(internalName))
                .filter(Objects::nonNull)
                .findAny()
                .orElseGet(() -> this.findMinecraft(internalName));
    }

    private ClassNode findMinecraft(String internalName) {
        try {
            return MixinService.getService().getBytecodeProvider().getClassNode(internalName);
        } catch (Exception e) {
            throw new RuntimeException(internalName, e);
        }
    }

    public void addRepo(ClassRepo repo) {
        this.repos.add(repo);
    }

    public void removeRepo(ClassRepo repo) {
        this.repos.remove(repo);
    }

    public static PluginInheritanceProvider inheritanceProvider() {
        return PROVIDER;
    }

    public static PluginInheritanceProvider remappingProvider() {
        return REMAPPING;
    }

    public static RuntimeRepo runtimeRepo() {
        return INSTANCE.runtimeRepo;
    }
}
