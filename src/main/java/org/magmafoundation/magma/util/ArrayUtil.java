package org.magmafoundation.magma.util;

import java.util.function.IntFunction;

/**
 * ArrayUtil
 *
 * @author Mainly by IzzelAliz and modified by Hex
 * @originalClassName ArrayUtil
 * @classFrom <a href="https://github.com/IzzelAliz/Arclight/blob/FeudalKings/arclight-common/src/main/java/io/izzel/arclight/common/util/ArrayUtil.java">Click here to get to github</a>
 *            <p>
 *            This classes is modified by Magma to support the Magma software.
 */
public class ArrayUtil {
    public static Object[] append(Object[] array, Object obj) {
        Object[] newArray = new Object[array.length + 1];
        newArray[array.length] = obj;
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static Object[] prepend(Object[] array, Object obj) {
        Object[] newArray = new Object[array.length + 1];
        newArray[0] = obj;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }

    public static <T> T[] prepend(T[] array, T obj, IntFunction<T[]> factory) {
        T[] newArray = factory.apply(array.length + 1);
        newArray[0] = obj;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }
}
