package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.projectile.Projectile;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftProjectile;

public class CraftCustomProjectileEntity extends CraftProjectile {
    public CraftCustomProjectileEntity(CraftServer server, Projectile entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftCustomProjectileEntity{" + this.getType() + "}";
    }
}
