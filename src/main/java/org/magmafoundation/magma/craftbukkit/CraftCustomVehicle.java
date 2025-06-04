package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftVehicle;

public class CraftCustomVehicle extends CraftVehicle {
    public CraftCustomVehicle(CraftServer server, Entity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftCustomVehicle{" + this.getType() + "}";
    }
}
