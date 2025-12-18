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

package org.magmafoundation.magma.remapping.loaders;

import com.google.common.io.ByteStreams;
import io.izzel.tools.product.Product2;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.security.CodeSource;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;
import org.magmafoundation.magma.remapping.ClassLoaderRemapper;
import org.magmafoundation.magma.remapping.ClassLoaderRemapping;
import org.magmafoundation.magma.remapping.MagmaRemapper;

/**
 * MagmaReflectionHandler
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName ArclightReflectionHandler
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/generated/RemappingURLClassLoader.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class RemappingURLClassLoader extends URLClassLoader implements RemappingClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public RemappingURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, ClassLoaderRemapping.tryRedirect(parent));
    }

    public RemappingURLClassLoader(URL[] urls) {
        super(urls, ClassLoaderRemapping.tryRedirect(null));
    }

    public RemappingURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, ClassLoaderRemapping.tryRedirect(parent), factory);
    }

    public RemappingURLClassLoader(String name, URL[] urls, ClassLoader parent) {
        super(name, urls, ClassLoaderRemapping.tryRedirect(parent));
    }

    public RemappingURLClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(name, urls, ClassLoaderRemapping.tryRedirect(parent), factory);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = null;
        String path = name.replace('.', '/').concat(".class");
        URL resource = this.getResource(path);
        if (resource != null) {
            URLConnection connection;
            Callable<byte[]> byteSource;
            Manifest manifest;
            try {
                connection = resource.openConnection();
                connection.connect();
                if (connection instanceof JarURLConnection && ((JarURLConnection) connection).getManifest() != null) {
                    manifest = ((JarURLConnection) connection).getManifest();
                } else {
                    manifest = null;
                }
                byteSource = () -> {
                    try (InputStream is = connection.getInputStream()) {
                        return ByteStreams.toByteArray(is);
                    }
                };
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }

            Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection);

            int i = name.lastIndexOf('.');
            if (i != -1) {
                String pkgName = name.substring(0, i);
                if (getPackage(pkgName) == null) {
                    if (manifest != null) {
                        this.definePackage(pkgName, manifest, ((JarURLConnection) connection).getJarFileURL());
                    } else {
                        this.definePackage(pkgName, null, null, null, null, null, null, null);
                    }
                }
            }
            result = this.defineClass(name, classBytes._1, 0, classBytes._1.length, classBytes._2);
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        return result;
    }

    private ClassLoaderRemapper remapper;

    @Override
    public ClassLoaderRemapper getRemapper() {
        if (remapper == null) {
            remapper = MagmaRemapper.createClassLoaderRemapper(this);
        }
        return remapper;
    }
}
