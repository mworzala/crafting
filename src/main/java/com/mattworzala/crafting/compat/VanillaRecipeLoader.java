package com.mattworzala.crafting.compat;

import com.google.gson.*;
import com.mattworzala.crafting.RecipeManager;
import com.mattworzala.crafting.recipe.Ingredient;
import com.mattworzala.crafting.recipe.type.ShapedRecipe;
import com.mattworzala.crafting.recipe.type.ShapelessRecipe;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class VanillaRecipeLoader {
    private static final Logger logger = LoggerFactory.getLogger(VanillaRecipeLoader.class);

    private static final Gson gson = new GsonBuilder().create();

    public static int loadVanillaRecipes(Path recipeDirectory) {
        try {
            return Files.list(recipeDirectory)
                    .mapToInt(VanillaRecipeLoader::loadVanillaRecipe)
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int loadVanillaRecipe(Path path) {
        String fileName = path.getFileName().toString();
        String recipeId = fileName.replace(".json", "");
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            JsonObject recipe = JsonParser.parseReader(reader).getAsJsonObject();
            String type = recipe.get("type").getAsString().substring("minecraft:".length());

            try {
                switch (type) {
                    case "crafting_shapeless":
                        loadShapeless(recipeId, recipe);
                        break;
                    case "crafting_shaped":
                        loadShaped(recipeId, recipe);
                        break;
                    default:
//                        logger.warn("Skipping unknown recipe type {} in {}.", type, fileName);
                        break;
                }
            } catch (Exception e) {
                logger.warn("Skipping {}: {}", fileName, e.getMessage());
            }

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static void loadShapeless(String id, JsonObject obj) {
        // Ingredients
        List<Ingredient> ingredients = new ArrayList<>();
        for (JsonElement ingredientElement : obj.get("ingredients").getAsJsonArray()) {
            ingredients.add(parseIngredient(ingredientElement));
        }

        // Result
        ItemStack result = parseResult(obj.get("result").getAsJsonObject());

        ShapelessRecipe recipe = new ShapelessRecipe(id, ingredients, result);
        RecipeManager.addRecipe(recipe);
    }

    private static void loadShaped(String id, JsonObject obj) {
        // Keys
        Char2ObjectMap<Ingredient> ingredientKeys = new Char2ObjectOpenHashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("key").entrySet()) {
            char key = entry.getKey().charAt(0);
            Ingredient ingredient = parseIngredient(entry.getValue());
            ingredientKeys.put(key, ingredient);
        }

        // Pattern
        String[] pattern = gson.fromJson(obj.get("pattern"), String[].class);
        int width = pattern[0].length();
        int height = pattern.length;
        int size = width * height;

        // Ingredients
        Ingredient[] ingredients = new Ingredient[size];
        for (int i = 0; i < size; i++) {
            char key = pattern[i / width].charAt(i % width);
            if (key == ' ')
                ingredients[i] = Ingredient.EMPTY;
            else if (ingredientKeys.containsKey(key)) {
                ingredients[i] = ingredientKeys.get(key);
            } else throw new IllegalStateException("Missing keymap for " + key);
        }

        // Result
        ItemStack result = parseResult(obj.get("result").getAsJsonObject());

        ShapedRecipe recipe = new ShapedRecipe(id, width, height, ingredients, result);
        RecipeManager.addRecipe(recipe);
    }

    private static Material findMaterial(String namespacedId) {
        Material match = Registries.getMaterial(namespacedId);
        if (match == Material.AIR && !namespacedId.equals("minecraft:air"))
            throw new IllegalArgumentException("Not a Material: " + namespacedId);
        return match;
    }

    private static Ingredient parseIngredient(JsonElement ingredientElement) {
        if (ingredientElement.isJsonArray()) {
            Material[] options = StreamSupport
                    .stream(ingredientElement.getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(obj -> {
                        if (!obj.has("item"))
                            throw new RuntimeException("Cannot load item tags (yet)");
                        return findMaterial(obj.get("item").getAsString());
                    })
                    .toArray(Material[]::new);
            return new Ingredient.Vanilla(options);
        } else {
            JsonObject ingredientObject = ingredientElement.getAsJsonObject();
            if (!ingredientObject.has("item"))
                throw new RuntimeException("Cannot load item tags (yet)");
            Material ingredientMaterial = findMaterial(ingredientObject.get("item").getAsString());
            return new Ingredient.Vanilla(ingredientMaterial);
        }
    }

    private static ItemStack parseResult(JsonObject resultObject) {
        Material resultMaterial = findMaterial(resultObject.get("item").getAsString());
        byte resultCount = 1;
        if (resultObject.has("count"))
            resultCount = resultObject.get("count").getAsByte();
        return new ItemStack(resultMaterial, resultCount);
    }
}
