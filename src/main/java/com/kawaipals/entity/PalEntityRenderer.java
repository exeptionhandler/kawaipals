package com.kawaipals.entity;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.kawaipals.KawaiPalsMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * PalEntityRenderer – GeckoLib 5.5 GeoEntityRenderer for PalEntity instances.
 */
public class PalEntityRenderer extends GeoEntityRenderer<PalEntity, EntityRenderState> {

    public static final DataTicket<String> PAL_TEXTURE_TICKET = DataTicket.create("pal_texture", String.class);
    private static final Identifier FALLBACK_TEXTURE =
        Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "textures/entity/gotita.png");

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

    @Override
    public Identifier getTextureLocation(EntityRenderState renderState) {
        if (renderState instanceof GeoRenderState geoState && geoState.hasGeckolibData(PAL_TEXTURE_TICKET)) {
            String palId = geoState.getGeckolibData(PAL_TEXTURE_TICKET);
            return Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "textures/entity/" + palId + ".png");
        }
        return FALLBACK_TEXTURE;
    }
}
