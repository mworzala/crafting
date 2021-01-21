package com.mattworzala.crafting.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Ingredient implements Predicate<ItemStack> {
    public static final Ingredient EMPTY = new Empty();

    @Override
    public abstract boolean test(@NotNull ItemStack itemStack);

    /**
     * Apply a craft to this itemstack, decrementing an appropriate amount,
     * or other actions which may be required, such as replacing the item.
     * <p>
     * It is valid to return the given itemStack with modified values.
     *
     * @param itemStack The itemstack to be applied
     * @return The result of applying these ingredients to the item
     */
    public abstract ItemStack apply(@NotNull ItemStack itemStack);

    public abstract Collection<ItemStack> getValidItems();

    public final void write(@NotNull BinaryWriter writer) {
        final Collection<ItemStack> items = getValidItems();
        writer.writeVarInt(items.size());

        //todo wiki.vg specifies that each item *should* have a count of 1, but is it actually required?
        for (ItemStack item : items) {
            byte amount = item.getAmount();
            item.setAmount((byte) 1);
            writer.writeItemStack(item);
            item.setAmount(amount);
        }
    }

    private static class Empty extends Ingredient {
        @Override
        public boolean test(@NotNull ItemStack itemStack) {
            return itemStack.isAir();
        }

        @Override
        public ItemStack apply(@NotNull ItemStack itemStack) {
            return itemStack;
        }

        @Override
        public Collection<ItemStack> getValidItems() {
            return Collections.emptyList();
        }
    }

    public static class Vanilla extends Ingredient {
        private final Material[] materials;
        private final Collection<ItemStack> items;

        public Vanilla(Material... materials) {
            this.materials = materials;
            this.items = Arrays.stream(materials)
                    .map(mat -> new ItemStack(mat, (byte) 1))
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public boolean test(@NotNull ItemStack itemStack) {
            if (itemStack.isAir()) return false;
            final Material item = itemStack.getMaterial();
            for (Material material : this.materials) {
                if (material == item)
                    return true;
            }
            return false;
        }

        @Override
        public ItemStack apply(@NotNull ItemStack itemStack) {
            itemStack.setAmount((byte) (itemStack.getAmount() - 1));
            return itemStack;
        }

        @Override
        public Collection<ItemStack> getValidItems() {
            return this.items;
        }
    }
}

