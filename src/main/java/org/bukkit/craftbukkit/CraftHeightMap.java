package org.bukkit.craftbukkit;

import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.HeightMap;

public final class CraftHeightMap {
    private CraftHeightMap() {}

    public static Heightmap.Types toNMS(HeightMap bukkitHeightMap) {
        switch (bukkitHeightMap) {
            case MOTION_BLOCKING_NO_LEAVES:
                return Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
            case OCEAN_FLOOR:
                return Heightmap.Types.OCEAN_FLOOR;
            case OCEAN_FLOOR_WG:
                return Heightmap.Types.OCEAN_FLOOR_WG;
            case WORLD_SURFACE:
                return Heightmap.Types.WORLD_SURFACE;
            case WORLD_SURFACE_WG:
                return Heightmap.Types.WORLD_SURFACE_WG;
            case MOTION_BLOCKING:
                return Heightmap.Types.MOTION_BLOCKING;
            default:
                throw new EnumConstantNotPresentException(Heightmap.Types.class, bukkitHeightMap.name());
        }
    }

    public static HeightMap fromNMS(Heightmap.Types nmsHeightMapType) {
        switch (nmsHeightMapType) {
            case WORLD_SURFACE_WG:
                return HeightMap.WORLD_SURFACE_WG;
            case WORLD_SURFACE:
                return HeightMap.WORLD_SURFACE;
            case OCEAN_FLOOR_WG:
                return HeightMap.OCEAN_FLOOR_WG;
            case OCEAN_FLOOR:
                return HeightMap.OCEAN_FLOOR;
            case MOTION_BLOCKING_NO_LEAVES:
                return HeightMap.MOTION_BLOCKING_NO_LEAVES;
            case MOTION_BLOCKING:
                return HeightMap.MOTION_BLOCKING;
            default:
                throw new EnumConstantNotPresentException(HeightMap.class, nmsHeightMapType.name());
        }
    }
}
