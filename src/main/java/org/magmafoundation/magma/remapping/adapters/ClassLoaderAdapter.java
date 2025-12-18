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

package org.magmafoundation.magma.remapping.adapters;

import com.google.common.collect.ImmutableMap;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.magmafoundation.magma.Magma;
import org.magmafoundation.magma.remapping.ClassLoaderRemapper;
import org.magmafoundation.magma.remapping.MagmaRemapper;
import org.magmafoundation.magma.remapping.PluginTransformer;
import org.magmafoundation.magma.remapping.loaders.RemappingClassLoader;
import org.magmafoundation.magma.remapping.loaders.RemappingURLClassLoader;
import org.magmafoundation.magma.remapping.repos.GlobalClassRepo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * ClassLoaderAdapter
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName ClassLoaderAdapter
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ClassLoaderAdapter.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class ClassLoaderAdapter implements PluginTransformer {
    public static final ClassLoaderAdapter INSTANCE = new ClassLoaderAdapter();
    private static final Marker MARKER = MarkerManager.getMarker("CLADAPTER");
    private static final String CLASSLOADER = "java/lang/ClassLoader";

    private final Map<String, String> classLoaderTypes = ImmutableMap.<String, String>builder()
            .put(Type.getInternalName(URLClassLoader.class), Type.getInternalName(RemappingURLClassLoader.class))
            .build();

    @Override
    public void handleClass(ClassNode node, ClassLoaderRemapper remapper) {
        // Must handle all class loaders, or else we'll lose track of classloading
        for (MethodNode methodNode : node.methods) {
            for (AbstractInsnNode insnNode : methodNode.instructions) {
                if (insnNode.getOpcode() == Opcodes.NEW) {
                    TypeInsnNode typeInsnNode = (TypeInsnNode) insnNode;
                    String replace = classLoaderTypes.get(typeInsnNode.desc);
                    if (replace != null) {
                        AbstractInsnNode next = typeInsnNode.getNext();
                        while (next != null && (next.getOpcode() != Opcodes.INVOKESPECIAL || !((MethodInsnNode) next).name.equals("<init>") || !((MethodInsnNode) next).owner.equals(typeInsnNode.desc))) {
                            next = next.getNext();
                        }
                        if (next == null) continue;
                        Magma.LOGGER.debug(MARKER, "Found new {}/{} call in {} {}", typeInsnNode.desc, ((MethodInsnNode) next).name + ((MethodInsnNode) next).desc, node.name, methodNode.name + methodNode.desc);
                        ((MethodInsnNode) next).owner = replace;
                        typeInsnNode.desc = replace;
                    }
                }
            }
        }
        ClassInfo info = classInfo(node);
        if (info == null) return;
        Magma.LOGGER.debug(MARKER, "Transforming classloader class {}", node.name);
        if (!info.remapping) {
            implementIntf(node);
        }
        for (MethodNode methodNode : node.methods) {
            for (AbstractInsnNode insnNode : methodNode.instructions) {
                if (insnNode instanceof MethodInsnNode methodInsnNode) {
                    if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL && methodNode.name.equals("<init>") && methodInsnNode.name.equals("<init>") && methodInsnNode.owner.equals(node.superName)) {
                        methodInsnNode.owner = info.superName;
                    }
                }
            }
        }
        node.superName = info.superName;
    }

    private void implementIntf(ClassNode node) {
        Magma.LOGGER.debug(MARKER, "Implementing RemappingClassLoader for class {}", node.name);
        FieldNode remapper = new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC, "remapper", Type.getDescriptor(ClassLoaderRemapper.class), null, null);
        MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, "getRemapper", Type.getMethodDescriptor(Type.getType(ClassLoaderRemapper.class)), null, null);
        InsnList list = new InsnList();
        LabelNode labelNode = new LabelNode();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, remapper.name, remapper.desc));
        list.add(new JumpInsnNode(Opcodes.IFNONNULL, labelNode));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new InsnNode(Opcodes.DUP));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MagmaRemapper.class), "createClassLoaderRemapper", Type.getMethodDescriptor(Type.getType(ClassLoaderRemapper.class), Type.getType(ClassLoader.class)), false));
        list.add(new FieldInsnNode(Opcodes.PUTFIELD, node.name, remapper.name, remapper.desc));
        list.add(labelNode);
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, remapper.name, remapper.desc));
        list.add(new InsnNode(Opcodes.ARETURN));
        methodNode.instructions = list;
        node.fields.add(remapper);
        node.methods.add(methodNode);
        node.interfaces.add(Type.getInternalName(RemappingClassLoader.class));
    }

    private ClassInfo classInfo(ClassNode node) {
        ClassInfo info = new ClassInfo();
        Collection<String> parents = GlobalClassRepo.inheritanceProvider().getAll(node.superName);
        if (!parents.contains(CLASSLOADER)) return null;
        for (String s : classLoaderTypes.keySet()) {
            if (parents.contains(s)) {
                info.remapping = true;
                break;
            }
        }
        String s = classLoaderTypes.get(node.superName);
        if (s != null) {
            info.superName = s;
        } else {
            info.superName = node.superName;
        }
        return info;
    }

    private static class ClassInfo {
        private String superName;
        private boolean remapping;
    }
}
