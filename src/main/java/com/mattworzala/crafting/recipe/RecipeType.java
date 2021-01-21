package com.mattworzala.crafting.recipe;

public enum RecipeType {
    SHAPELESS("crafting_shapeless"),
    SHAPED("crafting_shaped"),
    SMELTING("smelting"),
    BLASTING("blasting"),
    SMOKING("smoking"),
    CAMPFIRE("campfire_cooking"),
    STONECUTTING("stonecutting"),
    SMITHING("smithing")
    ;

    private final String id;
    RecipeType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
//public class RecipeType {
//    public static final RecipeType SHAPELESS = new RecipeType("crafting_shapeless");
//
//
//
//
//
//
//
//
//    private static final List<String> MINECRAFT_RECIPE_TYPES = List.of("crafting_shapeless", "crafting_shaped", "smelting", "blasting", "smoking", "campfire_cooking", "stonecutting", "smithing");
//    private final String typeId;
//    private final boolean hasParity;
//
//    public RecipeType(String typeId) {
//        this.typeId = typeId;
//        this.hasParity = MINECRAFT_RECIPE_TYPES.contains(typeId);
//    }
//
//    public String getTypeId() {
//        return typeId;
//    }
//
//    public boolean hasMinecraftParity() {
//        return this.hasParity;
//    }
//}
