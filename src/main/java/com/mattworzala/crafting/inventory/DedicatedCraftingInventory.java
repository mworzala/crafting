package com.mattworzala.crafting.inventory;

import com.mattworzala.crafting.RecipeManager;
import com.mattworzala.crafting.recipe.Recipe;
import com.mattworzala.crafting.recipe.RecipeType;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.condition.InventoryConditionResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Represents a dedicated crafting window, typically opened by interacting
 * with a crafting table block.
 * <p>
 * The size is 3x3.
 */
public class DedicatedCraftingInventory extends CraftingInventory {
    private static final int CRAFTING_TABLE_SIDE_LENGTH = 3;
    private static final int MATRIX_INDEX_OFFSET = 1;

    private final Inventory delegate;
    private volatile Recipe<CraftingInventory> lastRecipe = null;


    public DedicatedCraftingInventory(Player owner) {
        this.delegate = new Inventory(InventoryType.CRAFTING, "Crafting");

        //todo watch for close and if its the owner give their items back
        this.delegate.addInventoryCondition((player, slot, type, result) -> {
            if (!MathUtils.isBetween(slot, 0, 9)) return;
            if (slot == 0) {
                result.setCancel(true);
                handleTakeResult(player, type, result);
            }

            //todo something of a hack because of how events deal with getting the item during the event
            owner.getInstance().scheduleNextTick(inst -> {
                handleItemChange(player, type, result);
            });
        });

        owner.openInventory(this.delegate);
    }

    private void handleTakeResult(Player player, ClickType type, InventoryConditionResult result) {
        if (lastRecipe == null) return;
        result.setClickedItem(ItemStack.getAirItem());

        switch (type) {
            case LEFT_CLICK:
            case RIGHT_CLICK:
                ItemStack cursor = result.getCursorItem();
                ItemStack resultItem = this.delegate.getItemStack(0);

                if (cursor.isSimilar(resultItem)) {
                    StackingRule cursorRule = cursor.getStackingRule();
                    if (cursorRule.canBeStacked(cursor, resultItem)) {
                        lastRecipe.apply(this);
                        result.setCursorItem(cursorRule.apply(cursor, cursor.getAmount() + resultItem.getAmount()));
                    }
                } else {
                    lastRecipe.apply(this);
                    result.setCursorItem(this.delegate.getItemStack(0));
                }
                break;
            case START_SHIFT_CLICK:
                // Should craft max and add to inventory
                ItemStack nextItem = this.delegate.getItemStack(0);
                do {
                    boolean added = player.getInventory().addItemStack(nextItem);
                    if (!added) break;
                    lastRecipe.apply(this);
                    nextItem = lastRecipe.craft(this);
                } while (lastRecipe.test(this));
                break;
            case CHANGE_HELD:
                // Should go to hotbar slot only if it is empty
                //todo how to get target slot?
                break;

            case DROP:
                lastRecipe.apply(this);
                player.dropItem(result.getClickedItem());
                break;
            case START_DRAGGING:
            case DRAGGING:
            case SHIFT_CLICK:
            case START_DOUBLE_CLICK:
            case DOUBLE_CLICK:
            default:
                // Unused
                break;
        }
    }

    private void handleItemChange(Player player, ClickType type, InventoryConditionResult result) {
        // If last known recipe is valid, no change is required.
        if (lastRecipe != null && lastRecipe.test(this)) {
            if (this.delegate.getItemStack(0).isAir())
                this.delegate.setItemStack(0, lastRecipe.craft(this));
            return;
        }

        // Find new recipe
        lastRecipe = RecipeManager.findValidRecipeOfTypes(this, RecipeType.SHAPELESS);
        if (lastRecipe == null)
            lastRecipe = RecipeManager.findValidRecipeOfTypes(this, RecipeType.SHAPED);
        if (lastRecipe == null) {
            this.delegate.setItemStack(0, ItemStack.getAirItem());
        } else {
            this.delegate.setItemStack(0, lastRecipe.craft(this));
        }
    }

    @Override
    public void setItemStack(int slot, @NotNull ItemStack itemStack) {
        Check.argCondition(slot > getSize(), "Cannot set slot outside crafting inventory!");
        this.delegate.setItemStack(slot + MATRIX_INDEX_OFFSET, itemStack);
    }

    @Override
    public boolean addItemStack(@NotNull ItemStack itemStack) {
        //todo in reality this should just apply to the matrix
        throw new UnsupportedOperationException("#addItemStack not supported on Crafting Inventory.");
    }

    @Override
    public void clear() {
        //todo in reality this should just apply to the matrix
        throw new UnsupportedOperationException("#clear not supported on Crafting Inventory.");
    }

    @NotNull
    @Override
    public ItemStack getItemStack(int slot) {
        Check.argCondition(slot > getSize(), "Cannot get slot outside crafting inventory!");
        return this.delegate.getItemStack(slot + MATRIX_INDEX_OFFSET);
    }

    @NotNull
    @Override
    public ItemStack[] getItemStacks() {
        ItemStack[] allStacks = this.delegate.getItemStacks();
        return Arrays.copyOfRange(allStacks, MATRIX_INDEX_OFFSET, allStacks.length);
    }

    @Override
    public int getWidth() {
        return CRAFTING_TABLE_SIDE_LENGTH;
    }

    @Override
    public int getHeight() {
        return CRAFTING_TABLE_SIDE_LENGTH;
    }
}
