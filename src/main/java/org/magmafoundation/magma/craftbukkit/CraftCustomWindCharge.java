package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftWindCharge;

public class CraftCustomWindCharge extends CraftWindCharge {
    public CraftCustomWindCharge(CraftServer server, WindCharge entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftCustomWindCharge{" + this.getType() + "}";
    }
}
