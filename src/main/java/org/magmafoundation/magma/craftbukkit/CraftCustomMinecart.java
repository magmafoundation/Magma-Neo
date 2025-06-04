package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMinecart;

public class CraftCustomMinecart extends CraftMinecart {
    public CraftCustomMinecart(CraftServer server, AbstractMinecart entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftCustomMinecart{" + this.getType() + "}";
    }
}
