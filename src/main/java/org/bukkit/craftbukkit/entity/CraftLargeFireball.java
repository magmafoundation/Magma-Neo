package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.LargeFireball;

public class CraftLargeFireball extends CraftSizedFireball implements LargeFireball {
    public CraftLargeFireball(CraftServer server, net.minecraft.world.entity.projectile.LargeFireball entity) {
        super(server, entity);
    }

    @Override
    public void setYield(float yield) {
        super.setYield(yield);
        getHandle().explosionPower = (int) yield;
    }

    @Override
    public net.minecraft.world.entity.projectile.LargeFireball getHandle() {
        return (net.minecraft.world.entity.projectile.LargeFireball) entity;
    }

    @Override
    public String toString() {
        return "CraftLargeFireball";
    }
}
