package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMinecartContainer;

public class CraftCustomMinecartContainer extends CraftMinecartContainer {
    public CraftCustomMinecartContainer(CraftServer server, AbstractMinecart entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftCustomMinecartContainer{" + this.getType() + "}";
    }
}
