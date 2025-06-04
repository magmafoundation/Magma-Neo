package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;

public class CraftCustomEntity extends CraftEntity {
    public CraftCustomEntity(CraftServer server, Entity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftCustomEntity{" + this.getType() + "}";
    }
}
