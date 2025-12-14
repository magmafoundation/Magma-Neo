package org.magmafoundation.magma.util;

import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;

public record MagmaRecipeChoice(Ingredient ingredient) implements RecipeChoice {
    @Override
    public @NotNull ItemStack getItemStack() {
        net.minecraft.world.item.ItemStack[] items = ingredient.getItems();
        return items.length > 0 ? CraftItemStack.asCraftMirror(items[0]) : new ItemStack(Material.AIR, 0);
    }

    @Override
    public @NotNull RecipeChoice clone() {
        try {
            return (RecipeChoice) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone MagmaRecipeChoice", e);
        }
    }

    @Override
    public boolean test(@NotNull ItemStack itemStack) {
        return ingredient.test(CraftItemStack.asNMSCopy(itemStack));
    }
}
