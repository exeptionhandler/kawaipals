package com.kawaipals.entity;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.kawaipals.KawaiPalsMod;
import net.minecraft.resources.Identifier;

/**
 * PalEntityModel – the GeckoLib 5.5 GeoModel for all Pal entities.
 */
public class PalEntityModel extends GeoModel<PalEntity> {

    private static final Identifier MODEL_LOCATION =
        Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "pal_entity");

    private static final Identifier FALLBACK_TEXTURE =
        Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "textures/entity/gotita.png");

    private static final Identifier ANIMATION_LOCATION =
        Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "pal_entity");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL_LOCATION;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState != null && renderState.hasGeckolibData(PalEntityRenderer.PAL_TEXTURE_TICKET)) {
            String palId = renderState.getGeckolibData(PalEntityRenderer.PAL_TEXTURE_TICKET);
            return Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "textures/entity/" + palId + ".png");
        }
        return FALLBACK_TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(PalEntity entity) {
        return ANIMATION_LOCATION;
    }
}
