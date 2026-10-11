package com.kawaipals.registry;

import com.kawaipals.KawaiPalsMod;
import com.kawaipals.item.CompendiumItem;
import com.kawaipals.item.PalOrbItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * ModItems – registers all custom Item instances for KawaiPals.
 */
public final class ModItems {

    public static final Item PAL_ORB = Registry.register(
        BuiltInRegistries.ITEM,
        Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "pal_orb"),
        new PalOrbItem(new Item.Properties().stacksTo(1))
    );

    public static final Item COMPENDIUM = Registry.register(
        BuiltInRegistries.ITEM,
        Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "compendium"),
        new CompendiumItem(new Item.Properties().stacksTo(1))
    );

    public static void register() {
        KawaiPalsMod.LOGGER.info("[KawaiPals] Registered items.");
    }

    private ModItems() {}
}
