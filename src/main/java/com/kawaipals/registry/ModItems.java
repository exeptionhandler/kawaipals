package com.kawaipals.registry;

import com.kawaipals.KawaiPalsMod;
import com.kawaipals.item.CompendiumItem;
import com.kawaipals.item.PalOrbItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * ModItems – registers all custom Item instances for KawaiPals.
 *
 * In Minecraft 26.2, Item.Properties must have the ResourceKey assigned
 * via setId() BEFORE the Item constructor runs, so it can compute its
 * description ID. We create the key first, then pass it to setId().
 */
public final class ModItems {

    private static final Identifier PAL_ORB_ID = Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "pal_orb");
    private static final Identifier COMPENDIUM_ID = Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "compendium");
    private static final Identifier KAWAI_COMPASS_ID = Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "kawai_compass");

    private static final ResourceKey<Item> PAL_ORB_KEY = ResourceKey.create(Registries.ITEM, PAL_ORB_ID);
    private static final ResourceKey<Item> COMPENDIUM_KEY = ResourceKey.create(Registries.ITEM, COMPENDIUM_ID);
    private static final ResourceKey<Item> KAWAI_COMPASS_KEY = ResourceKey.create(Registries.ITEM, KAWAI_COMPASS_ID);

    public static final Item PAL_ORB = Registry.register(
        BuiltInRegistries.ITEM,
        PAL_ORB_KEY,
        new PalOrbItem(new Item.Properties().stacksTo(1).setId(PAL_ORB_KEY))
    );

    public static final Item COMPENDIUM = Registry.register(
        BuiltInRegistries.ITEM,
        COMPENDIUM_KEY,
        new CompendiumItem(new Item.Properties().stacksTo(1).setId(COMPENDIUM_KEY))
    );

    public static final Item KAWAI_COMPASS = Registry.register(
        BuiltInRegistries.ITEM,
        KAWAI_COMPASS_KEY,
        new com.kawaipals.item.KawaiCompassItem(new Item.Properties().stacksTo(1).setId(KAWAI_COMPASS_KEY))
    );

    public static void register() {
        KawaiPalsMod.LOGGER.info("[KawaiPals] Registered items.");
    }

    private ModItems() {}
}
