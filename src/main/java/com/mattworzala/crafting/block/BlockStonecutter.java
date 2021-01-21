package com.mattworzala.crafting.block;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockStonecutter extends CustomBlock {
    private static final int BLOCK_ID = 29437;

    public BlockStonecutter() {
        super(Block.STONECUTTER, "minestom:stonecutter");
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

        Inventory inventory = new Inventory(InventoryType.STONE_CUTTER, "Stonecutter");
        player.openInventory(inventory);

        return true;
    }

    @Override
    public short getCustomBlockId() {
        return BLOCK_ID;
    }
}
