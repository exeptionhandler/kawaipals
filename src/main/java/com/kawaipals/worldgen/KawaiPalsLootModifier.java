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
            if (BuiltInLootTables.SIMPLE_DUNGEON.equals(key)) {
                tableBuilder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.15f))
                    .add(LootItem.lootTableItem(ModItems.PAL_ORB)));
            } else if (BuiltInLootTables.DESERT_PYRAMID.equals(key)) {
                tableBuilder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.10f))
                    .add(LootItem.lootTableItem(ModItems.PAL_ORB)));
            } else if (BuiltInLootTables.BASTION_TREASURE.equals(key)) {
                tableBuilder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.05f))
                    .add(LootItem.lootTableItem(ModItems.PAL_ORB)));
            } else if (BuiltInLootTables.END_CITY_TREASURE.equals(key)) {
                tableBuilder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.03f))
                    .add(LootItem.lootTableItem(ModItems.PAL_ORB)));
            }
        });

        KawaiPalsMod.LOGGER.info("[KawaiPals] Loot table modifications registered.");
    }
}
