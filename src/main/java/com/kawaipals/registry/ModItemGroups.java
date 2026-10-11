package com.kawaipals.registry;

import com.kawaipals.KawaiPalsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * ModItemGroups – registers the KawaiPals creative-mode item tab.
 */
public final class ModItemGroups {

    public static final CreativeModeTab KAWAIPALS_GROUP = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "kawaipals"),
        CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.kawaipals.kawaipals"))
            .icon(() -> new ItemStack(ModItems.COMPENDIUM))
            .displayItems((params, output) -> {
                output.accept(ModItems.COMPENDIUM);
                output.accept(ModItems.PAL_ORB);
                for (com.kawaipals.core.PalType type : com.kawaipals.core.PalType.values()) {
                    output.accept(com.kawaipals.item.PalOrbItem.createCaptured(type));
                }
            })
            .build()
    );

    public static void register() {
        KawaiPalsMod.LOGGER.info("[KawaiPals] Registered item groups.");
    }

    private ModItemGroups() {}
}
