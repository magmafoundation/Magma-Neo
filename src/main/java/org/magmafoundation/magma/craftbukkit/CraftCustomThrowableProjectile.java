package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftThrowableProjectile;

public class CraftCustomThrowableProjectile extends CraftThrowableProjectile {
    public CraftCustomThrowableProjectile(CraftServer server, ThrowableItemProjectile entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftCustomThrowableProjectile{" + this.getType() + "}";
    }
}
