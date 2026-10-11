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
 * KawaiiShrineBuilder – constructs the mystical KawaiPals Shrines & Temples.
 */
public final class KawaiiShrineBuilder {

    private static final Random RANDOM = new Random();

    private KawaiiShrineBuilder() {}

    /** 1. Santuario Kawaii (Overworld standard shrine) */
    public static void build(LevelAccessor level, BlockPos origin) {
        int r = 3;
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
                for (int y = 0; y <= 4; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        int[][] corners = {{-r, -r}, {r, -r}, {-r, r}, {r, r}};
        for (int[] c : corners) {
            for (int y = 0; y <= 2; y++) {
                level.setBlock(origin.offset(c[0], y, c[1]), Blocks.QUARTZ_PILLAR.defaultBlockState(), 3);
            }
            level.setBlock(origin.offset(c[0], 3, c[1]), Blocks.CHERRY_LEAVES.defaultBlockState(), 3);
            level.setBlock(origin.offset(c[0], 4, c[1]), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 3);
        }

        level.setBlock(origin.offset(0, 0, 0), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 3);
        BlockPos chestPos = origin.offset(0, 1, 0);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);

        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            chest.clearContent();
            PalType[] specialPals = {PalType.PEGASITO, PalType.CELESTIA, PalType.XOLOTL_REY, PalType.LUMINARIX, PalType.TEMPESIN};
            PalType chosenPal = specialPals[RANDOM.nextInt(specialPals.length)];
            chest.setItem(13, PalOrbItem.createCaptured(chosenPal));
            chest.setItem(4, new ItemStack(ModItems.COMPENDIUM));
            chest.setItem(11, new ItemStack(Items.AMETHYST_SHARD, 4));
            chest.setItem(15, new ItemStack(Items.GOLDEN_APPLE, 2));
            chest.setItem(22, new ItemStack(Items.CHERRY_SAPLING, 3));
        }
    }

    /** 2. Ruinas Celestiales (High altitude / floating sky temple for Pegasito & Celestia) */
    public static void buildCelestial(LevelAccessor level, BlockPos origin) {
        int r = 4;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos floorPos = origin.offset(x, -1, z);
                int dist = Math.abs(x) + Math.abs(z);
                if (dist <= 5) {
                    if (dist == 0) {
                        level.setBlock(floorPos, Blocks.SEA_LANTERN.defaultBlockState(), 3);
                    } else if (dist % 2 == 0) {
                        level.setBlock(floorPos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 3);
                    } else {
                        level.setBlock(floorPos, Blocks.CALCITE.defaultBlockState(), 3);
                    }
                }
                for (int y = 0; y <= 5; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // Sky Arch Pillars
        int[][] pillars = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] p : pillars) {
            for (int y = 0; y <= 3; y++) {
                level.setBlock(origin.offset(p[0], y, p[1]), Blocks.QUARTZ_PILLAR.defaultBlockState(), 3);
            }
            level.setBlock(origin.offset(p[0], 4, p[1]), Blocks.END_ROD.defaultBlockState(), 3);
            level.setBlock(origin.offset(p[0], 5, p[1]), Blocks.AMETHYST_CLUSTER.defaultBlockState(), 3);
        }

        // Celestial Altar
        level.setBlock(origin.offset(0, 0, 0), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), 3);
        BlockPos chestPos = origin.offset(0, 1, 0);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);

        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            chest.clearContent();
            PalType chosenPal = RANDOM.nextBoolean() ? PalType.PEGASITO : PalType.CELESTIA;
            chest.setItem(13, PalOrbItem.createCaptured(chosenPal));
            chest.setItem(4, new ItemStack(ModItems.COMPENDIUM));
            chest.setItem(10, new ItemStack(Items.FEATHER, 16));
            chest.setItem(12, new ItemStack(Items.FIREWORK_ROCKET, 8));
            chest.setItem(16, new ItemStack(Items.GOLDEN_APPLE, 3));
        }
    }

    /** 3. Templo del Ajolote Místico (Lush water temple for Xolotl Rey & Gotita) */
    public static void buildXolotl(LevelAccessor level, BlockPos origin) {
        int r = 4;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos floorPos = origin.offset(x, -1, z);
                if (Math.abs(x) == r || Math.abs(z) == r) {
                    level.setBlock(floorPos, Blocks.MOSS_BLOCK.defaultBlockState(), 3);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(floorPos, Blocks.PRISMARINE_BRICKS.defaultBlockState(), 3);
                } else {
                    level.setBlock(floorPos, Blocks.DARK_PRISMARINE.defaultBlockState(), 3);
                }
                for (int y = 0; y <= 4; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // Cute Axolotl statues at corners
        int[][] statues = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] s : statues) {
            level.setBlock(origin.offset(s[0], 0, s[1]), Blocks.CHERRY_PLANKS.defaultBlockState(), 3);
            level.setBlock(origin.offset(s[0], 1, s[1]), Blocks.FLOWERING_AZALEA.defaultBlockState(), 3);
        }

        // Central Altar
        level.setBlock(origin.offset(0, 0, 0), Blocks.PURPUR_BLOCK.defaultBlockState(), 3);
        BlockPos chestPos = origin.offset(0, 1, 0);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);

        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            chest.clearContent();
            PalType chosenPal = RANDOM.nextBoolean() ? PalType.XOLOTL_REY : PalType.GOTITA;
            chest.setItem(13, PalOrbItem.createCaptured(chosenPal));
            chest.setItem(4, new ItemStack(ModItems.COMPENDIUM));
            chest.setItem(11, new ItemStack(Items.HEART_OF_THE_SEA, 1));
            chest.setItem(15, new ItemStack(Items.NAUTILUS_SHELL, 2));
            chest.setItem(22, new ItemStack(Items.LILY_PAD, 5));
        }
    }

    /** 4. Altar Infernal de Ascuas (Cute Nether altar for Fenixito & Carbonito) */
    public static void buildInfernal(LevelAccessor level, BlockPos origin) {
        int r = 3;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos floorPos = origin.offset(x, -1, z);
                if (Math.abs(x) == 1 && Math.abs(z) == 1) {
                    level.setBlock(floorPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
                } else if ((x + z) % 2 == 0) {
                    level.setBlock(floorPos, Blocks.GILDED_BLACKSTONE.defaultBlockState(), 3);
                } else {
                    level.setBlock(floorPos, Blocks.POLISHED_BASALT.defaultBlockState(), 3);
                }
                for (int y = 0; y <= 4; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // Horn pillars
        int[][] horns = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int[] h : horns) {
            level.setBlock(origin.offset(h[0], 0, h[1]), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
            level.setBlock(origin.offset(h[0], 1, h[1]), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
            level.setBlock(origin.offset(h[0], 2, h[1]), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
        }

        // Fiery Altar
        level.setBlock(origin.offset(0, 0, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 3);
        BlockPos chestPos = origin.offset(0, 1, 0);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);

        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            chest.clearContent();
            PalType chosenPal = RANDOM.nextBoolean() ? PalType.FENIXITO : PalType.CARBONITO;
            chest.setItem(13, PalOrbItem.createCaptured(chosenPal));
            chest.setItem(4, new ItemStack(ModItems.COMPENDIUM));
            chest.setItem(10, new ItemStack(Items.BLAZE_POWDER, 6));
            chest.setItem(12, new ItemStack(Items.GOLD_INGOT, 8));
            chest.setItem(16, new ItemStack(Items.MAGMA_CREAM, 4));
        }
    }
}
