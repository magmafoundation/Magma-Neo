package org.magmafoundation.magma.craftbukkit.inv;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerArmorInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public class CraftCustomInventory implements InventoryHolder {
    private final CraftInventory container;

    public CraftCustomInventory(Container inventory) {
        this.container = new CraftInventory(inventory);
    }

    public CraftCustomInventory(ItemStackHandler handler) {
        this.container = null;
    }

    @Nullable
    public static InventoryHolder getHolder(IItemHandler handler) {
        if (handler == null) {
            return null;
        }
        if (handler instanceof ItemStackHandler) {
            return new CraftCustomInventory((ItemStackHandler) handler);
        }
        if (handler instanceof SlotItemHandler) {
            return new CraftCustomInventory(((SlotItemHandler) handler).container);
        }
        if (handler instanceof InvWrapper) {
            return new CraftCustomInventory(((InvWrapper) handler).getInv());
        }
        if (handler instanceof SidedInvWrapper) {
            return new CraftCustomInventory(((SidedInvWrapper) handler).inv);
        }
        if (handler instanceof PlayerInvWrapper) {
            IItemHandlerModifiable[] playerInventoryWrapper = ObfuscationReflectionHelper.getPrivateValue(CombinedInvWrapper.class, (PlayerInvWrapper) handler, "itemHandler");
            for (IItemHandlerModifiable itemHandler : playerInventoryWrapper) {
                if (itemHandler instanceof PlayerMainInvWrapper) {
                    return new CraftCustomInventory(((PlayerMainInvWrapper) itemHandler).getInventoryPlayer());
                }
                if (itemHandler instanceof PlayerArmorInvWrapper) {
                    return new CraftCustomInventory(((PlayerArmorInvWrapper) itemHandler).getInventoryPlayer());
                }
            }
        }
        return null;
    }

    @Nullable
    public static Inventory getCustomInventory(IItemHandler handler) {
        InventoryHolder holder = getHolder(handler);
        return holder != null ? holder.getInventory() : null;
    }

    @Override
    public Inventory getInventory() {
        return this.container;
    }

    public static List<HumanEntity> getViewers(Container inventory) {
        try {
            return inventory.getViewers();
        } catch (AbstractMethodError e) {
            return new ArrayList<HumanEntity>();
        }
    }
}
