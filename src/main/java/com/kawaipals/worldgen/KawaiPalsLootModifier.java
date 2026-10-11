package com.kawaipals.worldgen;

import com.kawaipals.KawaiPalsMod;
import com.kawaipals.registry.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * KawaiPalsLootModifier – injects Pal Orb items into vanilla structure chests.
 */
public final class KawaiPalsLootModifier {

    private KawaiPalsLootModifier() {}

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            float chance = 0.0f;

            if (BuiltInLootTables.SIMPLE_DUNGEON.equals(key)) {
                chance = 0.50f;
            } else if (BuiltInLootTables.DESERT_PYRAMID.equals(key)) {
                chance = 0.45f;
            } else if (BuiltInLootTables.ABANDONED_MINESHAFT.equals(key)) {
                chance = 0.40f;
            } else if (BuiltInLootTables.ANCIENT_CITY.equals(key)) {
                chance = 0.60f;
            } else if (BuiltInLootTables.WOODLAND_MANSION.equals(key)) {
                chance = 0.55f;
            } else if (BuiltInLootTables.UNDERWATER_RUIN_BIG.equals(key) || BuiltInLootTables.UNDERWATER_RUIN_SMALL.equals(key)) {
                chance = 0.40f;
            } else if (BuiltInLootTables.JUNGLE_TEMPLE.equals(key)) {
                chance = 0.45f;
            } else if (BuiltInLootTables.BASTION_TREASURE.equals(key)) {
                chance = 0.50f;
            } else if (BuiltInLootTables.END_CITY_TREASURE.equals(key)) {
                chance = 0.50f;
            }

            if (chance > 0.0f) {
                tableBuilder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(ModItems.PAL_ORB)));
            }
        });

        KawaiPalsMod.LOGGER.info("[KawaiPals] Loot table modifications registered with enhanced structure coverage.");
    }
}
