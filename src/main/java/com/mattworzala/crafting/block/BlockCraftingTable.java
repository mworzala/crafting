package com.mattworzala.crafting.block;

import com.mattworzala.crafting.inventory.DedicatedCraftingInventory;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockCraftingTable extends CustomBlock {
    private static final int BLOCK_ID = 29432;

    public BlockCraftingTable() {
        super(Block.CRAFTING_TABLE.getBlockId(), "minestom:crafting_table");
    }

    @Override
    public void onPlace(@NotNull Instance instance, @NotNull BlockPosition blockPosition, @Nullable Data data) {

    }

    @Override
    public void onDestroy(@NotNull Instance instance, @NotNull BlockPosition blockPosition, @Nullable Data data) {

    }

    @Override
    public boolean onInteract(@NotNull Player player, @NotNull Player.Hand hand, @NotNull BlockPosition blockPosition, @Nullable Data data) {
        if (player.isSneaking() && !player.getItemInHand(hand).isAir())
            return false;

        new DedicatedCraftingInventory(player);
        return true;
    }

    @Override
    public short getCustomBlockId() {
        return BLOCK_ID;
    }
}
