package org.magmafoundation.magma.neoforge;

import io.izzel.arclight.api.EnumHelper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.magmafoundation.magma.util.ResourceLocationUtil;

public class NeoInject {
    private static Logger LOGGER = LogManager.getLogger(NeoInject.class);

    public static final Map<net.minecraft.world.entity.EntityType<?>, String> ENTITY_TYPES = new ConcurrentHashMap<>();
    public static final Map<net.minecraft.world.entity.EntityType<?>, org.bukkit.entity.EntityType> ENTITY_TYPES0 = new ConcurrentHashMap<>();

    public static void init() {
        log("Injecting NeoForge Block Materials into Bukkit");
        addNeoForgeBlockMaterials();
        log("Injecting NeoForge Item Materials into Bukkit");
        addNeoForgeItemMaterials();
        log("Injecting NeoForge Entities into Bukkit");
        addNeoForgeEntities();
    }

    private static void log(String message) {
        LOGGER.info(message);
    }

    private static void addNeoForgeBlockMaterials() {
        var blockRegistry = BuiltInRegistries.BLOCK;
        for (var block : blockRegistry) {
            ResourceLocation blockResourceName = blockRegistry.getKey(block);
            if (!isModded(blockResourceName)) {
                continue;
            }

            var blockName = ResourceLocationUtil.standardize(blockResourceName);
            int blockId = Item.getId(block.asItem());
            Item item = Item.byId(blockId);
            Material material = Material.addMaterial(blockName, blockId, item.getMaxStackSize(new ItemStack(item)), blockResourceName, true, false);

            if (material != null) {
                CraftMagicNumbers.BLOCK_MATERIAL.put(block, material);
                CraftMagicNumbers.MATERIAL_BLOCK.put(material, block);
                log("Added NeoForge Material: " + material.name() + " - " + material.getKey());
            } else {
                log("Failed to add NeoForge Material: " + blockName);
            }
        }
    }

    private static void addNeoForgeItemMaterials() {
        var itemRegistry = BuiltInRegistries.ITEM;
        for (var item : itemRegistry) {
            ResourceLocation itemResourceName = itemRegistry.getKey(item);
            if (!isModded(itemResourceName)) {
                continue;
            }

            var itemName = ResourceLocationUtil.standardize(itemResourceName);
            int itemId = Item.getId(item);
            Material material = Material.addMaterial(itemName, itemId, item.getMaxStackSize(new ItemStack(item)), itemResourceName, false, false);

            if (material != null) {
                CraftMagicNumbers.ITEM_MATERIAL.put(item, material);
                CraftMagicNumbers.MATERIAL_ITEM.put(material, item);
                log("Added NeoForge Material: " + material.name() + " - " + material.getKey());
            } else {
                log("Failed to add NeoForge Material: " + itemName);
            }
        }
    }

    private static void addNeoForgeEntities() {
        var entityRegistry = BuiltInRegistries.ENTITY_TYPE;
        for (net.minecraft.world.entity.EntityType<?> entity : entityRegistry) {
            ResourceLocation entityResourceName = entityRegistry.getKey(entity);
            var enumName = ResourceLocationUtil.standardize(entityResourceName);
            if (!isModded(entityResourceName)) {
                NeoInject.ENTITY_TYPES.put(entity, enumName);
                continue;
            }

            var entityId = enumName.hashCode();
            EntityType bukkitType = EnumHelper.addEnum(EntityType.class, enumName, List.of(String.class, Class.class, Integer.TYPE, Boolean.TYPE), List.of(enumName.toLowerCase(), Entity.class, entityId, false));
            if (bukkitType != null) {
                bukkitType.magmaInject(entityResourceName, entity);
                log("Added NeoForge Entity: " + bukkitType.name() + " - " + bukkitType.getKey());
            } else {
                log("Failed to add NeoForge Entity: " + enumName);
            }
        }
        EntityClassLookup.init();
    }

    private static void addNeoForgeEnchantments() {}

    private static boolean isModded(ResourceLocation resourceLocation) {
        return !resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT);
    }
}
