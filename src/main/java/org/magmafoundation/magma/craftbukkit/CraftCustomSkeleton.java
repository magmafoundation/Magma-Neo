package org.magmafoundation.magma.craftbukkit;

import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractSkeleton;
import org.bukkit.entity.Skeleton;
import org.jetbrains.annotations.NotNull;

public class CraftCustomSkeleton extends CraftAbstractSkeleton {
    public CraftCustomSkeleton(CraftServer server, AbstractSkeleton entity) {
        super(server, entity);
    }

    @Override
    public @NotNull Skeleton.SkeletonType getSkeletonType() {
        return Skeleton.SkeletonType.MAGMA_CUSTOM;
    }

    @Override
    public String toString() {
        return "CraftCustomSkeleton{" + this.getType() + "}";
    }
}
