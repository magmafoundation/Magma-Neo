package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractHorse;
import org.bukkit.entity.Horse;
import org.jetbrains.annotations.NotNull;

public class CraftCustomAbstractHorse extends CraftAbstractHorse {
    public CraftCustomAbstractHorse(CraftServer server, AbstractHorse entity) {
        super(server, entity);
    }

    @Override
    public @NotNull Horse.Variant getVariant() {
        return Horse.Variant.MAGMA_CUSTOM;
    }

    @Override
    public String toString() {
        return "CraftCustomAbstractHorse{" + this.getType() + "}";
    }
}
