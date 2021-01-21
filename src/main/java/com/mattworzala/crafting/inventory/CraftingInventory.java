package com.mattworzala.crafting.inventory;

import net.minestom.server.inventory.InventoryModifier;
import net.minestom.server.inventory.condition.InventoryCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an inventory grid for crafting.
 *
 *
 */
public abstract class CraftingInventory implements InventoryModifier {



    public abstract int getWidth();

    public abstract int getHeight();

    @Override
    public int getSize() {
        return getWidth() * getHeight();
    }




    @NotNull
    @Override
    public List<InventoryCondition> getInventoryConditions() {
        throw new UnsupportedOperationException("Inventory conditions are not supported on CraftingInventory.");
    }

    @Override
    public void addInventoryCondition(@NotNull InventoryCondition inventoryCondition) {
        throw new UnsupportedOperationException("Inventory conditions are not supported on CraftingInventory.");
    }
}
