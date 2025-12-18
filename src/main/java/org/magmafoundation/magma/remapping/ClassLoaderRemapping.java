package org.magmafoundation.magma.remapping;

import org.magmafoundation.magma.remapping.loaders.RemappingClassLoader;

/**
 * ClassLoaderRemapping
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName ClassLoaderRemapping
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ClassLoaderRemapping.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class ClassLoaderRemapping {
    /*
     * Redirect is applied for every cl;
     * To preserve proper delegation rule, we don't apply predicate recursively
     */
    public static ClassLoader tryRedirect(ClassLoader parent) {
        return parent == ClassLoader.getSystemClassLoader() ? RemappingClassLoader.class.getClassLoader() : parent;
    }

    /*
     * Indicates whether ClassLoader(s) loaded by this ClassLoader can be transformed
     * to continue delegate transformation.
     */
    public static boolean canRemap(ClassLoader cl) {
        // Removing redirect for PlatformClassLoader since only classes
        // in the standard library are loaded by PlatformClassLoader.
        // They are always related only to JDK we're using.
        for (; cl != null; cl = cl.getParent()) {
            // They may know our class loader; in that case we also need to remap.
            // If they use system class loader then we also need to remap.
            if (cl == ClassLoader.getSystemClassLoader() || cl == RemappingClassLoader.class.getClassLoader()) {
                return true;
            }
        }
        return false;
    }
}
