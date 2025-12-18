package org.magmafoundation.magma.asm;

import java.lang.reflect.Modifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Implementer
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName Implementer
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/bootstrap/src/main/java/io/izzel/arclight/boot/asm/Implementer.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public interface Implementer {
    Logger LOGGER = LogManager.getLogger("Implementer");

    boolean processClass(ClassNode node);

    static void loadArgs(InsnList list, MethodNode methodNode, Type[] types, int i) {
        if (!Modifier.isStatic(methodNode.access)) {
            list.add(new VarInsnNode(Opcodes.ALOAD, i));
            i += 1;
        }
        for (Type type : types) {
            list.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), i));
            i += type.getSize();
        }
    }
}
