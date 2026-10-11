package com.kawaipals.core;

import net.minecraft.ChatFormatting;

/**
 * PalRarity – defines the four rarity tiers for Pals.
 */
public enum PalRarity {

    COMMON(
        "Common",
        ChatFormatting.WHITE,
        0xFFFFFF,
        /*dropWeight=*/  60,
        /*circleSize=*/  40,
        /*captureSpeed=*/ 1.2f
    ),

    RARE(
        "Rare",
        ChatFormatting.AQUA,
        0x55FFFF,
        /*dropWeight=*/  25,
        /*circleSize=*/  28,
        /*captureSpeed=*/ 1.8f
    ),

    EPIC(
        "Epic",
        ChatFormatting.LIGHT_PURPLE,
        0xFF55FF,
        /*dropWeight=*/  12,
        /*circleSize=*/  16,
        /*captureSpeed=*/ 2.6f
    ),

    LEGENDARY(
        "Legendary",
        ChatFormatting.GOLD,
        0xFFAA00,
        /*dropWeight=*/   3,
        /*circleSize=*/   8,
        /*captureSpeed=*/ 3.5f
    );

    public final String displayName;
    public final ChatFormatting color;
    public final int rgbColor;
    public final int dropWeight;
    public final int captureCircleSize;
    public final float captureSpeed;

    PalRarity(String displayName, ChatFormatting color, int rgbColor, int dropWeight,
              int captureCircleSize, float captureSpeed) {
        this.displayName       = displayName;
        this.color             = color;
        this.rgbColor          = rgbColor;
        this.dropWeight        = dropWeight;
        this.captureCircleSize = captureCircleSize;
        this.captureSpeed      = captureSpeed;
    }

    public String getColoredName() {
        return color.toString() + displayName + ChatFormatting.RESET;
    }

    public static PalRarity weightedRandom(java.util.function.IntUnaryOperator random) {
        int roll = random.applyAsInt(100);
        if (roll < 60)  return COMMON;
        if (roll < 85)  return RARE;
        if (roll < 97)  return EPIC;
        return LEGENDARY;
    }
}
