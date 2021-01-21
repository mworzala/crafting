package com.mattworzala.crafting;

import com.mattworzala.crafting.recipe.Recipe;
import com.mattworzala.crafting.recipe.RecipeType;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryModifier;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RecipeManager {
    private static final DeclareRecipesPacket declareRecipesPacket = new DeclareRecipesPacket();
    private static final List<Recipe<? extends InventoryModifier>> recipes = new CopyOnWriteArrayList<>();

    public static void addRecipe(Recipe<? extends InventoryModifier> recipe) {
        recipes.add(recipe);
        refreshDeclareRecipesPacket();
    }

    public static <I extends InventoryModifier> Recipe<I> findValidRecipeOfTypes(I inventory, RecipeType... types) {
        return (Recipe<I>) recipes.stream()
                .filter(recipe -> {
                    for (RecipeType type : types) {
                        if (type == recipe.getType())
                            return true;
                    }
                    return false;
                })
                .filter(recipe -> ((Recipe<I>) recipe).test(inventory))
                .findFirst()
                .orElse(null);
    }

    protected static void init() {

    }

    //todo packet grouping
    private static void refreshDeclareRecipesPacket() {
        declareRecipesPacket.recipes = recipes.toArray(new Recipe[0]);
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers())
            sendDeclaredRecipesPacket(player);
    }

    protected static void sendDeclaredRecipesPacket(Player player) {
        player.getPlayerConnection().sendPacket(declareRecipesPacket);
    }

    private static class DeclareRecipesPacket implements ServerPacket {
        private Recipe<?>[] recipes;

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(recipes.length);
            for (Recipe<?> recipe : recipes)
                recipe.write(writer);
        }

        @Override
        public int getId() {
            return ServerPacketIdentifier.DECLARE_RECIPES;
        }
    }
}
