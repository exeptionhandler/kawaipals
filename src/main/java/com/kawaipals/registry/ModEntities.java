package com.kawaipals.registry;

import com.kawaipals.KawaiPalsMod;
import com.kawaipals.entity.PalEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * ModEntities – central registration point for all custom entity types in KawaiPals.
 */
public final class ModEntities {

    public static final Identifier PAL_ENTITY_ID = Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "pal_entity");
    public static final net.minecraft.resources.ResourceKey<EntityType<?>> PAL_ENTITY_KEY = 
        net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, PAL_ENTITY_ID);

    public static final EntityType<PalEntity> PAL_ENTITY = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        PAL_ENTITY_KEY,
        EntityType.Builder.of(PalEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 0.8f)
            .clientTrackingRange(10)
            .build(PAL_ENTITY_KEY)
    );

    public static void register() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(
            PAL_ENTITY,
            PalEntity.createAttributes()
        );
        KawaiPalsMod.LOGGER.info("[KawaiPals] Registered entity types and default attributes.");
    }

    private ModEntities() {}
}
