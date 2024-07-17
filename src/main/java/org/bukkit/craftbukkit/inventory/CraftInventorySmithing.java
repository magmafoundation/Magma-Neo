package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingInventory;

public class CraftInventorySmithing extends CraftResultInventory implements SmithingInventory {
    private final Location location;

    public CraftInventorySmithing(Location location, net.minecraft.world.Container inventory, net.minecraft.world.inventory.ResultContainer resultInventory) {
        super(inventory, resultInventory);
        this.location = location;
    }

    @Override
    public net.minecraft.world.inventory.ResultContainer getResultInventory() {
        return (net.minecraft.world.inventory.ResultContainer) super.getResultInventory();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public ItemStack getResult() {
        return getItem(3);
    }

    @Override
    public void setResult(ItemStack item) {
        setItem(3, item);
    }

    @Override
    public Recipe getRecipe() {
        RecipeHolder<?> recipe = getResultInventory().getRecipeUsed();
        return (recipe == null) ? null : recipe.toBukkitRecipe();
    }
}
