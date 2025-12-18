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

package org.magmafoundation.magma.remapping;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.izzel.arclight.api.Unsafe;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.JointProvider;
import org.magmafoundation.magma.remapping.adapters.ClassLoaderAdapter;
import org.magmafoundation.magma.remapping.adapters.MagmaRedirectAdapter;
import org.magmafoundation.magma.remapping.handlers.RemapSourceHandler;
import org.magmafoundation.magma.remapping.repos.GlobalClassRepo;

/**
 * MagmaRemapper
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName ArclightRemapper
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/1.18/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ArclightRemapper.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
@SuppressWarnings("unchecked")
public class MagmaRemapper {
    public static final MagmaRemapper INSTANCE;
    public static final File DUMP;

    static {
        try {
            INSTANCE = new MagmaRemapper();
            DUMP = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final JarMapping toNmsMapping;
    private final JarMapping toBukkitMapping;
    private final JarMapping fromMojMapping;
    public final InheritanceMap inheritanceMap;
    private final List<PluginTransformer> transformerList = new ArrayList<>();
    private final JarRemapper toBukkitRemapper;
    private final JarRemapper toNmsRemapper;
    private final JarRemapper fromMojRemapper;

    public MagmaRemapper() throws Exception {
        this.toNmsMapping = new JarMapping();
        this.toBukkitMapping = new JarMapping();
        this.fromMojMapping = new JarMapping();
        this.inheritanceMap = new InheritanceMap();
        this.toNmsMapping.loadMappings(
                new BufferedReader(new InputStreamReader(MagmaRemapper.class.getClassLoader().getResourceAsStream("mappings/nms.srg"))),
                null, null, false);
        this.toBukkitMapping.loadMappings(
                new BufferedReader(new InputStreamReader(MagmaRemapper.class.getClassLoader().getResourceAsStream("mappings/nms.srg"))),
                null, null, true);
        this.fromMojMapping.loadMappings(
                new BufferedReader(new InputStreamReader(MagmaRemapper.class.getClassLoader().getResourceAsStream("mappings/moj.srg"))),
                null, null, true);
        BiMap<String, String> inverseClassMap = HashBiMap.create(toNmsMapping.classes).inverse();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(MagmaRemapper.class.getClassLoader().getResourceAsStream("mappings/inheritanceMap.txt")))) {
            inheritanceMap.load(reader, inverseClassMap);
        }

        this.toNmsMapping.packages.put("org/bukkit/craftbukkit/v1_21_R1/", "org/bukkit/craftbukkit/");
        this.toNmsMapping.packages.put("org/yaml/snakeyaml/", "org/magmafoundation/magma/deps/snakeyaml/");
        this.toNmsMapping.packages.put("org/apache/commons/lang/", "org/magmafoundation/magma/deps/commonslang/");
        JointProvider inheritanceProvider = new JointProvider();
        inheritanceProvider.add(inheritanceMap);
        inheritanceProvider.add(new ClassLoaderProvider(ClassLoader.getSystemClassLoader()));
        this.toNmsMapping.setFallbackInheritanceProvider(inheritanceProvider);
        this.toBukkitMapping.setFallbackInheritanceProvider(inheritanceProvider);
        this.fromMojMapping.setFallbackInheritanceProvider(inheritanceProvider);
        this.transformerList.add(MagmaInterfaceInvokerGen.INSTANCE);
        this.transformerList.add(MagmaRedirectAdapter.INSTANCE);
        this.transformerList.add(ClassLoaderAdapter.INSTANCE);
        toBukkitMapping.setFallbackInheritanceProvider(GlobalClassRepo.inheritanceProvider());
        this.toBukkitRemapper = new LenientJarRemapper(toBukkitMapping);
        this.toNmsRemapper = new LenientJarRemapper(toNmsMapping);
        this.fromMojRemapper = new LenientJarRemapper(fromMojMapping);
        RemapSourceHandler.register();
    }

    public static ClassLoaderRemapper createClassLoaderRemapper(ClassLoader classLoader) {
        return new ClassLoaderRemapper(INSTANCE.copyOf(INSTANCE.toNmsMapping), INSTANCE.copyOf(INSTANCE.toBukkitMapping), classLoader);
    }

    public static JarRemapper getResourceMapper() {
        return INSTANCE.toBukkitRemapper;
    }

    public static JarRemapper getNmsMapper() {
        return INSTANCE.toNmsRemapper;
    }

    public static JarRemapper getMojMapper() {
        return INSTANCE.fromMojRemapper;
    }

    public List<PluginTransformer> getTransformerList() {
        return transformerList;
    }

    private static long pkgOffset, clOffset, mdOffset, fdOffset, mapOffset;

    static {
        try {
            pkgOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("packages"));
            clOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("classes"));
            mdOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("methods"));
            fdOffset = Unsafe.objectFieldOffset(JarMapping.class.getField("fields"));
            mapOffset = Unsafe.objectFieldOffset(JarMapping.class.getDeclaredField("inheritanceMap"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private JarMapping copyOf(JarMapping mapping) {
        JarMapping jarMapping = new JarMapping();
        Unsafe.putObject(jarMapping, pkgOffset, Unsafe.getObject(mapping, pkgOffset));
        Unsafe.putObject(jarMapping, clOffset, Unsafe.getObject(mapping, clOffset));
        Unsafe.putObject(jarMapping, mdOffset, Unsafe.getObject(mapping, mdOffset));
        Unsafe.putObject(jarMapping, fdOffset, Unsafe.getObject(mapping, fdOffset));
        Unsafe.putObject(jarMapping, mapOffset, Unsafe.getObject(mapping, mapOffset));
        return jarMapping;
    }
}
