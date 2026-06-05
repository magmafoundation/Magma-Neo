package org.bukkit.craftbukkit.entity;

import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class CraftItem extends CraftEntity implements Item {

    // Paper start
    private final static int NO_AGE_TIME = (int) Short.MIN_VALUE;
    private final static int NO_PICKUP_TIME = (int) Short.MAX_VALUE;
    // Paper end

    public CraftItem(CraftServer server, ItemEntity entity) {
        super(server, entity);
    }

    @Override
    public ItemEntity getHandle() {
        return (ItemEntity) entity;
    }

    @Override
    public ItemStack getItemStack() {
        return CraftItemStack.asCraftMirror(getHandle().getItem());
    }

    @Override
    public void setItemStack(ItemStack stack) {
        getHandle().setItem(CraftItemStack.asNMSCopy(stack));
    }

    @Override
    public int getPickupDelay() {
        return getHandle().pickupDelay;
    }

    @Override
    public void setPickupDelay(int delay) {
        getHandle().pickupDelay = Math.min(delay, Short.MAX_VALUE);
    }

    @Override
    public void setUnlimitedLifetime(boolean unlimited) {
        if (unlimited) {
            // See EntityItem#INFINITE_LIFETIME
            getHandle().age = Short.MIN_VALUE;
        } else {
            getHandle().age = getTicksLived();
        }
    }

    @Override
    public boolean isUnlimitedLifetime() {
        return getHandle().age == Short.MIN_VALUE;
    }

    @Override
    public void setTicksLived(int value) {
        super.setTicksLived(value);

        // Second field for EntityItem (don't set if lifetime is unlimited)
        if (!isUnlimitedLifetime()) {
            getHandle().age = value;
        }
    }

    // Paper start
    @Override
    public boolean canMobPickup() {
        return this.getHandle().canMobPickup;
    }

    @Override
    public void setCanMobPickup(boolean canMobPickup) {
        this.getHandle().canMobPickup = canMobPickup;
    }

    @Override
    public boolean canPlayerPickup() {
        return this.getHandle().pickupDelay != NO_PICKUP_TIME;
    }

    @Override
    public void setCanPlayerPickup(boolean canPlayerPickup) {
        this.getHandle().pickupDelay = canPlayerPickup ? 0 : NO_PICKUP_TIME;
    }

    @Override
    public boolean willAge() {
        return this.getHandle().age != NO_AGE_TIME;
    }

    @Override
    public void setWillAge(boolean willAge) {
        this.getHandle().age = willAge ? 0 : NO_AGE_TIME;
    }

    @Override
    public int getHealth() {
        return this.getHandle().health;
    }

    @Override
    public void setHealth(int health) {
        if (health <= 0) {
            this.getHandle().getItem().onDestroyed(this.getHandle());
            this.getHandle().setRemovedReason(org.bukkit.event.entity.EntityRemoveEvent.Cause.PLUGIN);
            this.getHandle().discard();
        } else {
            this.getHandle().health = health;
        }
    }
    // Paper end

    @Override
    public void setOwner(UUID uuid) {
        getHandle().setTarget(uuid);
    }

    @Override
    public UUID getOwner() {
        return getHandle().target;
    }

    @Override
    public void setThrower(UUID uuid) {
        getHandle().thrower = uuid;
    }

    @Override
    public UUID getThrower() {
        return getHandle().thrower;
    }

    @Override
    public String toString() {
        return "CraftItem";
    }
}
