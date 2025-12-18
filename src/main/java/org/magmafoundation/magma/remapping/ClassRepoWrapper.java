package org.magmafoundation.magma.remapping;

import net.md_5.specialsource.repo.ClassRepo;
import org.objectweb.asm.tree.ClassNode;

/**
 * ClassRepoWrapper
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName ClassRepoWrapper
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ClassRepoWrapper.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public record ClassRepoWrapper(ClassRepo inner) implements ClassRepo {
    @Override
    public ClassNode findClass(String s) {
        return inner.findClass(s);
    }
}
