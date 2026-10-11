package com.kawaipals.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * KawaiiShrineFeature – world generation feature placing Kawaii Shrines across overworld.
 */
public class KawaiiShrineFeature extends Feature<NoneFeatureConfiguration> {

    public KawaiiShrineFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin);

        if (surfacePos.getY() <= level.getMinY() + 10) return false;

        // Verify terrain underneath is solid
        if (level.getBlockState(surfacePos.below()).isAir()) return false;

        KawaiiShrineBuilder.build(level, surfacePos);
        return true;
    }
}
