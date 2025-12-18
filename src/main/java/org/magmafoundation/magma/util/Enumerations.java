package org.magmafoundation.magma.util;

import io.izzel.arclight.api.Unsafe;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.function.Function;

/**
 * Enumerations
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName Enumerations
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/util/Enumerations.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class Enumerations {
    public static <A, B> Enumeration<B> transform(Enumeration<A> enumeration, Function<A, B> mapper) {
        return new Enumeration<B>() {
            @Override
            public boolean hasMoreElements() {
                return enumeration.hasMoreElements();
            }

            @Override
            public B nextElement() {
                return mapper.apply(enumeration.nextElement());
            }
        };
    }

    public static Enumeration<URL> remapped(Enumeration<URL> enumeration) {
        return transform(enumeration, url -> {
            try {
                return new URL("remap:" + url);
            } catch (MalformedURLException e) {
                Unsafe.throwException(e);
                return null;
            }
        });
    }
}
