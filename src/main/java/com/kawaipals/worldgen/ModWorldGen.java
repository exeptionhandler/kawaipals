package com.kawaipals.worldgen;

import com.kawaipals.KawaiPalsMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class ModWorldGen {

    public static final Feature<NoneFeatureConfiguration> KAWAII_SHRINE_FEATURE = Registry.register(
        BuiltInRegistries.FEATURE,
        Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "kawaii_shrine"),
        new KawaiiShrineFeature(NoneFeatureConfiguration.CODEC)
    );

    public static final ResourceKey<PlacedFeature> SHRINE_PLACED =
        ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "kawaii_shrine"));

    public static void register() {
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.SURFACE_STRUCTURES,
            SHRINE_PLACED
        );
        KawaiPalsMod.LOGGER.info("[KawaiPals] Worldgen features registered.");
    }

    private ModWorldGen() {}
}
