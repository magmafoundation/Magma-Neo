package org.magmafoundation.magma.neoforge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import io.izzel.arclight.api.EnumHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.LevelStem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftSpawnCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SpawnCategory;
import org.magmafoundation.magma.util.ResourceLocationUtil;

public class NeoInject {
    private static Logger LOGGER = LogManager.getLogger(NeoInject.class);

    public static final Map<net.minecraft.world.entity.EntityType<?>, String> ENTITY_TYPES = new ConcurrentHashMap<>();
    public static final Map<net.minecraft.world.entity.EntityType<?>, org.bukkit.entity.EntityType> ENTITY_TYPES0 = new ConcurrentHashMap<>();
    public static final Map<SpawnCategory, MobCategory> SPAWN_CATEGORY_MOB_CATEGORY = new HashMap<>();

    public static BiMap<ResourceKey<LevelStem>, World.Environment> environments = HashBiMap
            .create(ImmutableMap.<ResourceKey<LevelStem>, World.Environment>builder()
                    .put(LevelStem.OVERWORLD, World.Environment.NORMAL)
                    .put(LevelStem.NETHER, World.Environment.NETHER)
                    .put(LevelStem.END, World.Environment.THE_END)
                    .build());

    public static BiMap<World.Environment, ResourceKey<LevelStem>> environments0 = HashBiMap.create(ImmutableMap.<World.Environment, ResourceKey<LevelStem>>builder()
            .put(World.Environment.NORMAL, LevelStem.OVERWORLD)
            .put(World.Environment.NETHER, LevelStem.NETHER)
            .put(World.Environment.THE_END, LevelStem.END)
            .build());

    public static void init() {
        log("Injecting NeoForge Block Materials into Bukkit");
        addNeoForgeBlockMaterials();
        log("Injecting NeoForge Item Materials into Bukkit");
        addNeoForgeItemMaterials();
        log("Injecting NeoForge Entities into Bukkit");
        addNeoForgeEntities();
        log("Injecting NeoForge Mob Categorys into Bukkit");
        addNeoForgeMobCategorys();
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

    public static void addNeoForgeEnvironment(Registry<LevelStem> registry) {
        var i = World.Environment.values().length;
        for (var entry : registry.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            World.Environment environment = environments.get(key);
            if (environment == null) {
                String envName = ResourceLocationUtil.standardize(key.location());
                var id = i - 1;
                environment = EnumHelper.addEnum(World.Environment.class, envName, List.of(Integer.TYPE), List.of(id));
                environments.put(key, environment);
                environments0.put(environment, key);
                log("Added NeoForge Environment: " + environment.name() + " - " + key.location());
                i++;
            }
        }
    }

    private static void addNeoForgeEnchantments() {}

    private static void addNeoForgeMobCategorys() {
        for (MobCategory category : MobCategory.values()) {
            try {
                CraftSpawnCategory.toBukkit(category);
            } catch (Exception e) {
                String name = category.name();
                SpawnCategory bukkitCategory = EnumHelper.addEnum(SpawnCategory.class, name, List.of(), List.of());
                SPAWN_CATEGORY_MOB_CATEGORY.put(bukkitCategory, category);
                bukkitCategory.isModded = true;
                log("Added NeoForge Mob Category: " + bukkitCategory.name());
            }
        }
    }

    private static boolean isModded(ResourceLocation resourceLocation) {
        return !resourceLocation.getNamespace().equals(NamespacedKey.MINECRAFT);
    }
}
