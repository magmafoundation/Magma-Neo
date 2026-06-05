package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Bat;

public class CraftBat extends CraftAmbient implements Bat {
    public CraftBat(CraftServer server, net.minecraft.world.entity.ambient.Bat entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.ambient.Bat getHandle() {
        return (net.minecraft.world.entity.ambient.Bat) entity;
    }

    @Override
    public String toString() {
        return "CraftBat";
    }

    @Override
    public boolean isAwake() {
        return !getHandle().isResting();
    }

    @Override
    public void setAwake(boolean state) {
        getHandle().setResting(!state);
    }

    // Paper start
    @Override
    public org.bukkit.Location getTargetLocation() {
        net.minecraft.core.BlockPos pos = this.getHandle().targetPosition;
        if (pos == null) {
            return null;
        }
        return io.papermc.paper.util.MCUtil.toLocation(this.getHandle().level(), pos);
    }

    @Override
    public void setTargetLocation(org.bukkit.Location location) {
        net.minecraft.core.BlockPos pos = null;
        if (location != null) {
            pos = io.papermc.paper.util.MCUtil.toBlockPosition(location);
        }
        this.getHandle().targetPosition = pos;
    }
    // Paper end
}
