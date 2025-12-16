package org.magmafoundation.magma.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.inventory.InventoryHolder;
import org.magmafoundation.magma.craftbukkit.inv.CraftCustomInventory;

public class InventoryHelper {
    /**
     * Get a {@link InventoryHolder}
     * 
     * @param tileEntity The Tile entity
     * @return {@link InventoryHolder} or {@link CraftCustomInventory}
     */
    public static InventoryHolder getHolder(BlockEntity tileEntity) {
        return getHolder(tileEntity.getLevel(), tileEntity.getBlockPos());
    }

    /**
     * Get a {@link InventoryHolder}
     *
     * @param world The server world.
     * @param pos   The tile entity block position
     * @return {@link InventoryHolder} or {@link CraftCustomInventory}
     */
    public static InventoryHolder getHolder(Level world, BlockPos pos) {
        if (world == null) {
            return null;
        }
        // Spigot start
        org.bukkit.block.Block block = world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        if (block == null) {
            org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "No block for owner at %s %d %d %d", new Object[] { world.getWorld(), pos.getX(), pos.getY(), pos.getZ() });
            return null;
        }
        // Spigot end
        org.bukkit.block.BlockState state = block.getState();
        if (state instanceof InventoryHolder) {
            return (InventoryHolder) state;
            // Magma start - add forge inventory support
        } else if (state instanceof CraftBlockEntityState craftBlockEntityState) {
            BlockEntity tileEntity = craftBlockEntityState.getTileEntity();
            if (tileEntity instanceof Container) {
                return new CraftCustomInventory((Container) tileEntity);
            }
        }
        // Magma end
        return null;
    }

    /**
     * Get inventory owner
     *
     * @param inventory Tile entity inventory
     * @return owner
     */
    public static InventoryHolder getHolderOwner(Container inventory) {
        try {
            return inventory.getOwner();
        } catch (AbstractMethodError error) {
            return (inventory instanceof BlockEntity) ? getHolder((BlockEntity) inventory) : null;
        }
    }
}
