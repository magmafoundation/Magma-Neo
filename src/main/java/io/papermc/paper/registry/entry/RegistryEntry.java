package io.papermc.paper.registry.entry;

import io.papermc.paper.registry.RegistryHolder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.legacy.DelayedRegistryEntry;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface RegistryEntry<M, B extends Keyed> extends RegistryEntryInfo<M, B> { // TODO remove Keyed
    RegistryHolder<B> createRegistryHolder(Registry<M> nmsRegistry);

    default RegistryEntry<M, B> withSerializationUpdater(final BiFunction<NamespacedKey, ApiVersion, NamespacedKey> updater) {
        return this;
    }

    /**
     * This should only be used if the registry instance needs to exist early due to the need
     * to populate a field in {@link org.bukkit.Registry}. Data-driven registries shouldn't exist
     * as fields, but instead be obtained via {@link io.papermc.paper.registry.RegistryAccess#getRegistry(RegistryKey)}
     */
    @Deprecated
    default RegistryEntry<M, B> delayed() {
        return new DelayedRegistryEntry<>(this);
    }

    static <M, B extends Keyed> RegistryEntry<M, B> entry(
            final ResourceKey<? extends Registry<M>> mcKey,
            final RegistryKey<B> apiKey,
            final Class<?> classToPreload,
            final BiFunction<NamespacedKey, M, B> minecraftToBukkit) {
        return new CraftRegistryEntry<>(mcKey, apiKey, classToPreload, minecraftToBukkit);
    }

    static <M, B extends Keyed> RegistryEntry<M, B> apiOnly(
            final ResourceKey<? extends Registry<M>> mcKey,
            final RegistryKey<B> apiKey,
            final Supplier<org.bukkit.Registry<B>> apiRegistrySupplier) {
        return new ApiRegistryEntry<>(mcKey, apiKey, apiRegistrySupplier);
    }
}
