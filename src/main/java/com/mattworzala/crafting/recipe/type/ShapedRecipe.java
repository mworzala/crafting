package com.mattworzala.crafting.recipe.type;

import com.mattworzala.crafting.inventory.CraftingInventory;
import com.mattworzala.crafting.recipe.Ingredient;
import com.mattworzala.crafting.recipe.RecipeType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public class ShapedRecipe extends CraftingRecipe {
    private final String id;
    private final int width;
    private final int height;
    // Length = width * height
    private final Ingredient[] ingredients;
    private final ItemStack result;

    public ShapedRecipe(@NotNull String id, int width, int height, @NotNull Ingredient[] ingredients, @NotNull ItemStack result) {
        Check.argCondition(ingredients.length != width * height, "Ingredients array must match size.");

        this.id = id;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public RecipeType getType() {
        return RecipeType.SHAPED;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean test(@NotNull CraftingInventory inventory) {
        // Iterate over every possible location for this shape.
        // For example, given the recipe
        //   AB
        //   AB
        // It can go in the following possible places (in a 3x3):
        //   AB_    _AB    ___    ___
        //   AB_    _AB    AB_    _AB
        //   ___    ___    AB_    _AB
        //  (0, 0) (1, 0) (0, 1) (1, 1)
        for (int x = 0; x <= inventory.getWidth() - getWidth(); x++) {
            for (int y = 0; y <= inventory.getHeight() - getHeight(); y++) {
                // Test in this location
                if (test(inventory, x, y)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean test(@NotNull CraftingInventory inventory, int offsetX, int offsetY) {
        for (int x = 0; x < inventory.getWidth(); x++) {
            for (int y = 0; y < inventory.getHeight(); y++) {
                ItemStack itemStack = inventory.getItemStack(x + (y * inventory.getWidth()));

                // If its inside the recipe area
                if (x >= offsetX && x < offsetX + getWidth() && y >= offsetY && y < offsetY + getHeight()) {
                    // Should be valid ingredient
                    Ingredient ingredient = this.ingredients[(x - offsetX) + ((y - offsetY) * getWidth())];
                    if (!ingredient.test(itemStack))
                        return false;
                } else {
                    // Should be air
                    if (!itemStack.isAir())
                        return false;
                }
            }
        }

        return true;
    }

    @Override
    public @NotNull ItemStack craft(@NotNull CraftingInventory inventory) {
        return getResult().clone();
    }

    @Override
    public void apply(@NotNull CraftingInventory inventory) {
        // See test
        for (int x = 0; x <= inventory.getWidth() - getWidth(); x++) {
            for (int y = 0; y <= inventory.getHeight() - getHeight(); y++) {
                if (test(inventory, x, y))
                    apply(inventory, x, y);
            }
        }
    }

    protected void apply(@NotNull CraftingInventory inventory, int offsetX, int offsetY) {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int parentIndex = (x + offsetX) + ((y + offsetY) * inventory.getWidth());
                Ingredient ingredient = this.ingredients[x + (y * getWidth())];
                ItemStack itemStack = inventory.getItemStack(parentIndex);
                itemStack = ingredient.apply(itemStack);
                if (itemStack.getAmount() < 1) itemStack = ItemStack.getAirItem();
                inventory.setItemStack(parentIndex, itemStack);
            }
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        // Id and Type
        super.write(writer);
        // Size
        writer.writeVarInt(getWidth());
        writer.writeVarInt(getHeight());
        // Group
        writer.writeSizedString("");
        // Ingredients
        for (Ingredient ingredient : ingredients) {
            ingredient.write(writer);
        }
        // Result
        writer.writeItemStack(getResult());
    }
}
