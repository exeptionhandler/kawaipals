package com.kawaipals.entity;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * PalEntityRenderer – GeckoLib 5.5 GeoEntityRenderer for PalEntity instances.
 */
public class PalEntityRenderer extends GeoEntityRenderer<PalEntity, EntityRenderState> {

    public static final DataTicket<String> PAL_TEXTURE_TICKET = DataTicket.create("pal_texture", String.class);

    public PalEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PalEntityModel());
    }

    @Override
    public void extractRenderState(PalEntity entity, EntityRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        if (renderState instanceof GeoRenderState geoState && entity.getPalType() != null) {
            geoState.addGeckolibData(PAL_TEXTURE_TICKET, entity.getPalType().id);
        }
    }
}
