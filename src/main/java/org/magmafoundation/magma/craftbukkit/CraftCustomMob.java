package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMob;

public class CraftCustomMob extends CraftMob {
    public CraftCustomMob(CraftServer server, Mob entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftCustomMob{" + this.getType() + "}";
    }
}
