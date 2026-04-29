package com.destroystokyo.paper.event.player;

import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Called when the player themselves change their armor items
 * <p>
 * Not currently called for environmental factors though it <strong>MAY BE IN THE FUTURE</strong>
 */
@NullMarked
public class PlayerArmorChangeEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final SlotType slotType;
    private final ItemStack oldItem;
    private final ItemStack newItem;

    @ApiStatus.Internal
    public PlayerArmorChangeEvent(final Player player, final SlotType slotType, final ItemStack oldItem, final ItemStack newItem) {
        super(player);
        this.slotType = slotType;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    /**
     * Gets the type of slot being altered.
     *
     * @return type of slot being altered
     */
    public SlotType getSlotType() {
        return this.slotType;
    }

    /**
     * Gets the existing item that's being replaced
     *
     * @return old item
     */
    public ItemStack getOldItem() {
        return this.oldItem;
    }

    /**
     * Gets the new item that's replacing the old
     *
     * @return new item
     */
    public ItemStack getNewItem() {
        return this.newItem;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public enum SlotType {
        HEAD(Material.NETHERITE_HELMET, Material.DIAMOND_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.CHAINMAIL_HELMET, Material.LEATHER_HELMET, Material.CARVED_PUMPKIN, Material.PLAYER_HEAD, Material.SKELETON_SKULL, Material.ZOMBIE_HEAD, Material.CREEPER_HEAD, Material.WITHER_SKELETON_SKULL, Material.TURTLE_HELMET, Material.DRAGON_HEAD, Material.PIGLIN_HEAD),
        CHEST(Material.NETHERITE_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.ELYTRA),
        LEGS(Material.NETHERITE_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.LEATHER_LEGGINGS),
        FEET(Material.NETHERITE_BOOTS, Material.DIAMOND_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.CHAINMAIL_BOOTS, Material.LEATHER_BOOTS);

        private final Set<Material> types;

        SlotType(final Material... types) {
            this.types = Set.of(types);
        }

        /**
         * Gets an immutable set of all allowed material types that can be placed in an
         * armor slot.
         *
         * @return immutable set of material types
         */
        public Set<Material> getTypes() {
            return this.types;
        }

        /**
         * Gets the type of slot via the specified material
         *
         * @param material material to get slot by
         * @return slot type the material will go in, or {@code null} if it won't
         */
        public static @Nullable SlotType getByMaterial(final Material material) {
            for (final SlotType slotType : values()) {
                if (slotType.getTypes().contains(material)) {
                    return slotType;
                }
            }
            return null;
        }

        /**
         * Gets whether this material can be equipped to a slot
         *
         * @param material material to check
         * @return whether this material can be equipped
         */
        public static boolean isEquipable(final Material material) {
            return getByMaterial(material) != null;
        }
    }
}
