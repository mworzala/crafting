package com.mattworzala.crafting;

import com.mattworzala.crafting.block.BlockCraftingTable;
import com.mattworzala.crafting.block.BlockStonecutter;
import com.mattworzala.crafting.recipe.Ingredient;
import com.mattworzala.crafting.recipe.type.ShapedRecipe;
import com.mattworzala.crafting.recipe.type.ShapelessRecipe;
import it.unimi.dsi.fastutil.shorts.Short2ShortArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.validate.Check;

import java.util.List;

public class CraftingSupport {
    public static final int BLOCK_ID_START = 29432;
    private static final Short2ShortMap customBlockMap = new Short2ShortArrayMap();

    private static boolean initialized = false;

    public static void init(boolean registerBlocks) {
        Check.stateCondition(initialized, "Cannot initialize CraftingSupport twice!");
        initialized = true;

        RecipeManager.init();
        RecipeManager.addRecipe(new ShapelessRecipe("test", List.of(
                new Ingredient.Vanilla(Material.GLOWSTONE_DUST),
                new Ingredient.Vanilla(Material.CRAFTING_TABLE)
        ), new ItemStack(Material.GOLD_INGOT, (byte) 1)));
        RecipeManager.addRecipe(new ShapedRecipe("test_shaped", 3, 3, new Ingredient[]{
                new Ingredient.Vanilla(Material.GLOWSTONE_DUST),
                new Ingredient.Vanilla(Material.GLOWSTONE_DUST),
                new Ingredient.Vanilla(Material.GLOWSTONE_DUST),
                Ingredient.EMPTY,
                new Ingredient.Vanilla(Material.CRAFTING_TABLE),
                Ingredient.EMPTY,
                Ingredient.EMPTY,
                new Ingredient.Vanilla(Material.CRAFTING_TABLE),
                Ingredient.EMPTY
        }, new ItemStack(Material.DIAMOND_PICKAXE, (byte) 1)));
        RecipeManager.addRecipe(new ShapedRecipe("test_shaped", 1, 3, new Ingredient[]{
                new Ingredient.Vanilla(Material.GLOWSTONE_DUST),
                new Ingredient.Vanilla(Material.GLOWSTONE_DUST),
                new Ingredient.Vanilla(Material.GLOWSTONE_DUST)
        }, new ItemStack(Material.GLOWSTONE, (byte) 1)));

        // Events
        MinecraftServer.getConnectionManager().addPlayerInitialization(player -> {
            // Recipes packet
            player.addEventCallback(PlayerSpawnEvent.class, e -> {
                if (!e.isFirstSpawn()) return;

                RecipeManager.sendDeclaredRecipesPacket(e.getPlayer());
            });

            // Placement
            player.addEventCallback(PlayerBlockPlaceEvent.class, e -> {
                final short blockStateId = e.getBlockStateId();
                if (customBlockMap.containsKey(blockStateId))
                    e.setCustomBlockId(customBlockMap.get(blockStateId));
            });
        });

        // Blocks
        if (registerBlocks) {
            registerBlock(new BlockCraftingTable());
            // Furnace
            // Blast Furnace
            // Smoker
            // Campfire
            registerBlock(new BlockStonecutter());
            // Smithing
        }

    }

    private static void registerBlock(CustomBlock block) {
        MinecraftServer.getBlockManager().registerCustomBlock(block);
        customBlockMap.put(block.getDefaultBlockStateId(), block.getCustomBlockId());
    }
}
