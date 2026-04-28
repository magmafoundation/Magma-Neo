package io.papermc.paper.configuration.serializer;

import static io.leangen.geantyref.GenericTypeReflector.erase;

import com.mojang.logging.LogUtils;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.EnumLookup;

/**
 * Enum serializer that lists options if fails and accepts `-` as `_`.
 */
public class EnumValueSerializer extends ScalarSerializer<Enum<?>> {
    private static final Logger LOGGER = LogUtils.getClassLogger();

    public EnumValueSerializer() {
        super(new TypeToken<Enum<?>>() {});
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public @Nullable Enum<?> deserialize(final Type type, final Object obj) throws SerializationException {
        final String enumConstant = obj.toString();
        final Class<? extends Enum> typeClass = erase(type).asSubclass(Enum.class);
        @Nullable
        Enum<?> ret = EnumLookup.lookupEnum(typeClass, enumConstant);
        if (ret == null) {
            ret = EnumLookup.lookupEnum(typeClass, enumConstant.replace("-", "_"));
        }
        if (ret == null) {
            boolean longer = typeClass.getEnumConstants().length > 10;
            List<String> options = Arrays.stream(typeClass.getEnumConstants()).limit(10L).map(Enum::name).toList();
            LOGGER.error("Invalid enum constant provided, expected one of [" + String.join(", ", options) + (longer ? ", ..." : "") + "], but got " + enumConstant);
        }
        return ret;
    }

    @Override
    public Object serialize(final Enum<?> item, final Predicate<Class<?>> typeSupported) {
        return item.name();
    }
}
