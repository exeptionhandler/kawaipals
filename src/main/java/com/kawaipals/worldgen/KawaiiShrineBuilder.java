package com.kawaipals.worldgen;

import com.kawaipals.core.PalType;
import com.kawaipals.item.PalOrbItem;
import com.kawaipals.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.Random;

/**
 * KawaiiShrineBuilder – constructs the mystical KawaiPals Shrine.
 */
public final class KawaiiShrineBuilder {

    private KawaiiShrineBuilder() {}

    public static void build(LevelAccessor level, BlockPos origin) {
        int r = 3;

        // 1. Foundation & Floor (7x7)
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos floorPos = origin.offset(x, -1, z);
                if (Math.abs(x) == r || Math.abs(z) == r) {
                    level.setBlock(floorPos, Blocks.CHERRY_PLANKS.defaultBlockState(), 3);
                } else if ((Math.abs(x) + Math.abs(z)) % 2 == 0) {
                    level.setBlock(floorPos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 3);
                } else {
                    level.setBlock(floorPos, Blocks.PURPUR_BLOCK.defaultBlockState(), 3);
                }

                // Clear air above
                for (int y = 0; y <= 4; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // 2. Pillars at corners
        int[][] corners = {{-r, -r}, {r, -r}, {-r, r}, {r, r}};
        for (int[] c : corners) {
            for (int y = 0; y <= 2; y++) {
                level.setBlock(origin.offset(c[0], y, c[1]), Blocks.QUARTZ_PILLAR.defaultBlockState(), 3);
            }
            level.setBlock(origin.offset(c[0], 3, c[1]), Blocks.CHERRY_LEAVES.defaultBlockState(), 3);
            level.setBlock(origin.offset(c[0], 4, c[1]), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 3);
        }

        // 3. Central Altar Pedestal
        level.setBlock(origin.offset(0, 0, 0), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 3);
        BlockPos chestPos = origin.offset(0, 1, 0);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);

        // 4. Fill Altar Chest with Guaranteed KawaiPals Loot!
        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            chest.clearContent();

            // Guaranteed Legendary or Epic Pal!
            PalType[] specialPals = {PalType.PEGASITO, PalType.CELESTIA, PalType.XOLOTL_REY, PalType.LUMINARIX, PalType.TEMPESIN};
            PalType chosenPal = specialPals[new Random().nextInt(specialPals.length)];

            chest.setItem(13, PalOrbItem.createCaptured(chosenPal)); // Center slot
            chest.setItem(4, new ItemStack(ModItems.COMPENDIUM));    // Compendium
            chest.setItem(11, new ItemStack(Items.AMETHYST_SHARD, 4));
            chest.setItem(15, new ItemStack(Items.GOLDEN_APPLE, 2));
            chest.setItem(22, new ItemStack(Items.CHERRY_SAPLING, 3));
        }
    }
}
