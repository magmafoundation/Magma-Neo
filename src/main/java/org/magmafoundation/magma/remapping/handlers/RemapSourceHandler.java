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

import com.google.common.io.ByteStreams;
import cpw.mods.modlauncher.ClassTransformer;
import cpw.mods.modlauncher.TransformingClassLoader;
import io.izzel.arclight.api.Unsafe;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Hashtable;
import org.magmafoundation.magma.remapping.MagmaRemapper;
import org.magmafoundation.magma.remapping.repos.GlobalClassRepo;
import org.objectweb.asm.ClassReader;

/**
 * RemapSourceHandler
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName RemapSourceHandler
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/resource/RemapSourceHandler.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */

public class RemapSourceHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new RemapSourceConnection(new URL(u.getFile()));
    }

    private static class RemapSourceConnection extends URLConnection {
        private static final MethodHandle MH_TRANSFORM;

        static {
            try {
                ClassLoader classLoader = RemapSourceConnection.class.getClassLoader();
                Field classTransformer = TransformingClassLoader.class.getDeclaredField("classTransformer");
                classTransformer.setAccessible(true);
                ClassTransformer tranformer = (ClassTransformer) classTransformer.get(classLoader);
                Method transform = tranformer.getClass().getDeclaredMethod("transform", byte[].class, String.class, String.class);
                MH_TRANSFORM = Unsafe.lookup().unreflect(transform).bindTo(tranformer);
            } catch (Throwable t) {
                throw new IllegalStateException("Unknown modlauncher version", t);
            }
        }

        private byte[] array;

        protected RemapSourceConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            byte[] bytes = ByteStreams.toByteArray(url.openStream());
            String className = new ClassReader(bytes).getClassName();
            if (className.startsWith("net/minecraft/") || className.equals("com/mojang/brigadier/tree/CommandNode")) {
                try {
                    bytes = (byte[]) MH_TRANSFORM.invokeExact(bytes, className.replace('/', '.'), "source");
                } catch (Throwable e) {
                    throw new IOException(e);
                }
            }
            this.array = MagmaRemapper.getResourceMapper().remapClassFile(bytes, GlobalClassRepo.INSTANCE);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            if (this.array == null) {
                throw new FileNotFoundException(this.url.getFile());
            } else {
                return new ByteArrayInputStream(this.array);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void register() {
        try {
            Unsafe.ensureClassInitialized(URL.class);
            MethodHandle getter = Unsafe.lookup().findStaticGetter(URL.class, "handlers", Hashtable.class);
            Hashtable<String, URLStreamHandler> handlers = (Hashtable<String, URLStreamHandler>) getter.invokeExact();
            handlers.put("remap", new RemapSourceHandler());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
