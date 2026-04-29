package io.papermc.paper.tag;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;

/**
 * All tags in this class are unmodifiable, attempting to modify them will throw an
 * {@link UnsupportedOperationException}.
 */
public class EntityTags {
    private static NamespacedKey keyFor(String key) {
        //noinspection deprecation
        return new NamespacedKey("paper", key + "_settag");
    }

    /**
     * Covers undead mobs
     * 
     * @see <a href="https://minecraft.wiki/wiki/Mob#Undead_mobs">https://minecraft.wiki/wiki/Mob#Undead_mobs</a>
     */
    public static final EntitySetTag UNDEADS = new EntitySetTag(keyFor("undeads"))
            .add(EntityType.DROWNED, EntityType.HUSK, EntityType.PHANTOM, EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.STRAY, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOGLIN, EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN, EntityType.BOGGED)
            .ensureSize("UNDEADS", 14).lock();

    /**
     * Covers all horses
     */
    public static final EntitySetTag HORSES = new EntitySetTag(keyFor("horses"))
            .contains("HORSE")
            .ensureSize("HORSES", 3).lock();

    /**
     * Covers all minecarts
     */
    public static final EntitySetTag MINECARTS = new EntitySetTag(keyFor("minecarts"))
            .contains("MINECART")
            .ensureSize("MINECARTS", 7).lock();

    /**
     * Covers mobs that split into smaller mobs
     */
    public static final EntitySetTag SPLITTING_MOBS = new EntitySetTag(keyFor("splitting_mobs"))
            .add(EntityType.SLIME, EntityType.MAGMA_CUBE)
            .ensureSize("SLIMES", 2).lock();

    /**
     * Covers all water based mobs
     * 
     * @see <a href="https://minecraft.wiki/wiki/Mob#Aquatic_mobs">https://minecraft.wiki/wiki/Mob#Aquatic_mobs</a>
     * @deprecated in favour of {@link org.bukkit.Tag#ENTITY_TYPES_AQUATIC}
     */
    @Deprecated
    public static final EntitySetTag WATER_BASED = new EntitySetTag(keyFor("water_based"))
            .add(EntityType.AXOLOTL, EntityType.DOLPHIN, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.TURTLE, EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH, EntityType.TADPOLE)
            .ensureSize("WATER_BASED", 12).lock();
}
