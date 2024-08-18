package com.Pdiddy973.AllTheCompressed.data.server;

import com.Pdiddy973.AllTheCompressed.AllTheCompressed;
import com.Pdiddy973.AllTheCompressed.data.compat.EnergizingRecipeBuilder;
import com.Pdiddy973.AllTheCompressed.overlay.Overlays;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import owmii.powah.Powah;

import java.util.concurrent.CompletableFuture;

import static com.Pdiddy973.AllTheCompressed.util.ResourceUtil.compress;
import static com.Pdiddy973.AllTheCompressed.util.ResourceUtil.decompress;

public class CraftingRecipes extends RecipeProvider {
    public CraftingRecipes(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, provider);
    }

    @Override
    protected void buildRecipes(RecipeOutput consumer) {
        energizingRecipes(consumer);
        for (Overlays value : Overlays.values()) {
            var parent = value.overlay.parent;
            var block = BuiltInRegistries.BLOCK.getOptional(parent);
            var conditional = consumer;
            if (!parent.getNamespace().equals("minecraft")) {
                conditional = consumer.withConditions(new ModLoadedCondition(parent.getNamespace()));
            }

            if (block.isEmpty() || block.get() == Blocks.AIR) {
                AllTheCompressed.LOGGER.error("missing block during datagen: {}", parent);
                continue;
            }

            var ingredient = block.get().asItem();
            for (var item : value.overlay.iall) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ingredient, 9)
                    .requires(item.get())
                    .unlockedBy(getHasName(item.get()), has(item.get()))
                    .save(conditional, decompress(item.getId()));

                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, item.get())
                    .define('#', ingredient)
                    .pattern("###")
                    .pattern("###")
                    .pattern("###")
                    .unlockedBy(getHasName(ingredient), has(ingredient))
                    .save(conditional, compress(item.getId()));

                ingredient = item.get();
            }
        }
    }

    protected void energizingRecipes(RecipeOutput consumer) {
        RecipeOutput powah = consumer.withConditions(new ModLoadedCondition(Powah.MOD_ID));

        EnergizingRecipeBuilder.build(Overlays.ENERGIZED_STEEL)
            .setEnergy(90_000)
            .addIngredient(Overlays.IRON)
            .addIngredient(Overlays.GOLD)
            .save(powah);

        EnergizingRecipeBuilder.build(Overlays.NIOTIC_CRYSTAL)
            .setEnergy(2_700_000)
            .addIngredient(Overlays.DIAMOND)
            .save(powah);

        EnergizingRecipeBuilder.build(Overlays.SPIRITED_CRYSTAL)
            .setEnergy(81_000_000)
            .addIngredient(Overlays.EMERALD)
            .save(powah);

//        RecipeOutput botania = powah.withConditions(new ModLoadedCondition("botania"));
//        EnergizingRecipeBuilder.build(Overlays.BLAZING_CRYSTAL)
//            .setEnergy(7_290_000)
//            .addIngredient(Overlays.BLAZE)
//            .save(botania);
    }
}
