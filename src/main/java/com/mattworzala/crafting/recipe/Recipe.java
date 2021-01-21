package com.mattworzala.crafting.recipe;

import net.minestom.server.inventory.InventoryModifier;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public abstract class Recipe<I extends InventoryModifier> implements Predicate<I>, Writeable {

    public abstract String getId();

    public abstract RecipeType getType();

    /**
     * Checks whether this recipe is valid in the given inventory.
     * <p>
     * This method should <b>not</b> mutate the inventory in any way, calling it does not
     * indicate that a craft has taken place.
     *
     * @param inventory The inventory to check
     * @return True if the recipe can be crafted in the inventory
     */
    @Override
    public abstract boolean test(@NotNull I inventory);

    /**
     * Returns the result of performing this craft.
     * <p>
     * This method should <b>not</b> mutate the inventory in any way, calling it does not
     * indicate that a craft has taken place.
     *
     * @param inventory The inventory which the craft is taking place
     * @return The result of crafting, with any needed metadata
     */
    @NotNull
    public abstract ItemStack craft(@NotNull I inventory);

    /**
     * Applies this craft to the given inventory. This method may assume that the inventory
     * has this recipe and is valid, so it just needs to remove the appropriate items.
     *
     * @param inventory The inventory to apply
     */
    public abstract void apply(@NotNull I inventory);

    @Override
    public void write(@NotNull BinaryWriter writer) {
        Check.stateCondition(!shouldSendToClient(), "Recipes without Minecraft parity may not be sent to the client!");
        writer.writeSizedString(getType().getId());
        writer.writeSizedString(getId());
    }

    public boolean shouldSendToClient() {
        return true;
    }
}
