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

import com.google.common.collect.Maps;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.md_5.specialsource.provider.InheritanceProvider;
import net.md_5.specialsource.repo.ClassRepo;
import org.magmafoundation.magma.Magma;
import org.magmafoundation.magma.remapping.repos.GlobalClassRepo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * MagmaInterfaceInvokerGen
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName ArclightInterfaceInvokerGen
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ArclightInterfaceInvokerGen.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class MagmaInterfaceInvokerGen implements PluginTransformer {
    public static final MagmaInterfaceInvokerGen INSTANCE = new MagmaInterfaceInvokerGen();
    private static final String PREFIX = "net/minecraft/";

    @Override
    public void handleClass(ClassNode node, ClassLoaderRemapper remapper) {
        generate(node, remapper, GlobalClassRepo.inheritanceProvider());
    }

    private static void generate(ClassNode classNode, ClassLoaderRemapper remapper, InheritanceProvider inheritanceProvider) {
        if (shouldGenerate(classNode.name, inheritanceProvider)) {
            HashSet<Map.Entry<String, String>> set = new HashSet<>();
            interfaceMethods(classNode.name, set, GlobalClassRepo.INSTANCE);
            for (Map.Entry<String, String> entry : set) {
                String name = entry.getKey();
                String desc = entry.getValue();
                MethodInsnNode node = remapper.mapMethod(classNode.name, name, desc);
                desc = remapper.mapMethodDesc(desc);
                if (node != null && !node.name.equals(name)) {
                    boolean extend = false;
                    for (MethodNode methodNode : classNode.methods) {
                        if (methodNode.name.equals(name) && methodNode.desc.equals(desc)) {
                            extend = true;
                            break;
                        }
                    }
                    if (!extend) {
                        MethodNode methodNode = generateSynthetic(name, desc, node, remapper);
                        classNode.methods.add(methodNode);
                        Magma.LOGGER.debug("Generated {} redirecting to {}", classNode.name + "/" + name + " " + desc, node.owner + "/" + node.name + " " + node.desc);
                    }
                }
            }
        }
    }

    private static MethodNode generateSynthetic(String name, String desc, MethodInsnNode node, ClassLoaderRemapper remapper) {
        name = remapper.mapType(name);
        MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, desc, null, null);
        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        Type[] types = Type.getArgumentTypes(desc);
        int offset = 1;
        for (Type type : types) {
            methodNode.instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), offset));
            offset += type.getSize();
        }
        methodNode.instructions.add(node);
        Type returnType = Type.getReturnType(desc);
        methodNode.instructions.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));
        return methodNode;
    }

    private static boolean shouldGenerate(String internalName, InheritanceProvider provider) {
        if (internalName == null) return false;
        if (internalName.startsWith(PREFIX)) return true;
        for (String parent : provider.getParents(internalName)) {
            if (shouldGenerate(parent, provider)) return true;
        }
        return false;
    }

    private static void interfaceMethods(String internalName, Set<Map.Entry<String, String>> set, ClassRepo classRepo) {
        if (internalName == null || internalName.equals("java/lang/Object") || internalName.startsWith(PREFIX)) return;
        ClassNode classNode = classRepo.findClass(internalName);
        if (classNode == null) return;
        interfaceMethods(classNode.superName, set, classRepo);
        if (classNode.interfaces != null && classNode.interfaces.size() > 0) {
            for (String intf : classNode.interfaces) {
                interfaceMethods(intf, set, classRepo);
            }
        }
        for (MethodNode methodNode : classNode.methods) {
            set.add(Maps.immutableEntry(methodNode.name, methodNode.desc));
        }
    }
}
