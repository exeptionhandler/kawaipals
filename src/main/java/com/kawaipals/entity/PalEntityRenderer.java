package com.kawaipals.entity;

import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * PalEntityRenderer – GeckoLib 5.5 GeoEntityRenderer for PalEntity instances.
 */
public class PalEntityRenderer extends GeoEntityRenderer<PalEntity, EntityRenderState> {

    public PalEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PalEntityModel());
    }
}
