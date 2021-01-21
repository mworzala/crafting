package com.mattworzala.crafting.recipe.type;

import com.google.common.collect.Queues;
import com.mattworzala.crafting.inventory.CraftingInventory;
import com.mattworzala.crafting.recipe.Ingredient;
import com.mattworzala.crafting.recipe.RecipeType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class ShapelessRecipe extends CraftingRecipe {
    private final String id;
    private final List<Ingredient> ingredients;
    private final ItemStack result;

    public ShapelessRecipe(String id, List<Ingredient> ingredients, ItemStack result) {
        this.id = id;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public RecipeType getType() {
        return RecipeType.SHAPELESS;
    }

    public List<Ingredient> getIngredients() {
        return Collections.unmodifiableList(this.ingredients);
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean test(@NotNull CraftingInventory inventory) {
        Queue<Ingredient> ingredients = Queues.newArrayDeque(this.ingredients);
        IntList validSlots = new IntArrayList(IntIterators.fromTo(0, inventory.getSize()));
        ingredients:
        while (!ingredients.isEmpty()) {
            Ingredient ingredient = ingredients.poll();
            IntListIterator iterator = validSlots.iterator();
            while (iterator.hasNext()) {
                int index = iterator.nextInt();
                ItemStack itemStack = inventory.getItemStack(index);
                if (itemStack.isAir())
                    iterator.remove();
                else {
                    if (ingredient.test(itemStack)) {
                        iterator.remove();
                        continue ingredients;
                    }
                }
            }
            return false;
        }

        // Clear the rest of the empty slots
        IntListIterator iterator = validSlots.iterator();
        while (iterator.hasNext()) {
            if (inventory.getItemStack(iterator.nextInt()).isAir())
                iterator.remove();
        }

        return validSlots.isEmpty();
    }

    @Override
    public @NotNull ItemStack craft(@NotNull CraftingInventory inventory) {
        return getResult().clone();
    }

    //todo also egregious
    @Override
    public void apply(@NotNull CraftingInventory inventory) {
        Queue<Ingredient> ingredients = Queues.newArrayDeque(this.ingredients);
        IntList validSlots = new IntArrayList(IntIterators.fromTo(0, inventory.getSize()));
        ingredients:
        while (!ingredients.isEmpty()) {
            Ingredient ingredient = ingredients.poll();
            IntListIterator iterator = validSlots.iterator();
            while (iterator.hasNext()) {
                int index = iterator.nextInt();
                ItemStack itemStack = inventory.getItemStack(index);
                if (itemStack.isAir())
                    iterator.remove();
                else {
                    if (ingredient.test(itemStack)) {
                        iterator.remove();
                        itemStack = ingredient.apply(itemStack);
                        if (itemStack.getAmount() < 1) itemStack = ItemStack.getAirItem();
                        inventory.setItemStack(index, itemStack);
                        continue ingredients;
                    }
                }
            }
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        // Type and Id
        super.write(writer);
        // Group
        writer.writeSizedString("");
        // Ingredients
        writer.writeVarInt(ingredients.size());
        for (Ingredient ingredient : ingredients)
            ingredient.write(writer);
        // Result
        writer.writeItemStack(result);
    }
}
