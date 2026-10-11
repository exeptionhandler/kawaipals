package com.kawaipals;

import com.kawaipals.entity.PalEntityRenderer;
import com.kawaipals.network.OpenCaptureScreenPacket;
import com.kawaipals.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * KawaiPalsClient – the client-only mod entry point.
 *
 * <p>Fabric calls {@link #onInitializeClient()} only on the physical client
 * (never on a dedicated server). All rendering, screen, and client-packet
 * registration goes here.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Register the GeckoLib {@link PalEntityRenderer} for {@link com.kawaipals.entity.PalEntity}.</li>
 *   <li>Register the client-side handler for {@link OpenCaptureScreenPacket} (opens CaptureScreen).</li>
 * </ul>
 */
@Environment(EnvType.CLIENT)
public class KawaiPalsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KawaiPalsMod.LOGGER.info("[KawaiPals] Initialising client…");

        // ── Entity renderers ──────────────────────────────────────────────────
        // Register the GeckoLib renderer for the shared PalEntity type.
        // PalEntityRenderer reads the PalType from NBT to choose the right texture.
        EntityRendererRegistry.register(
            ModEntities.PAL_ENTITY,
            PalEntityRenderer::new
        );

        // ── Packet receivers ──────────────────────────────────────────────────
        // Register the client-side handler that opens CaptureScreen when the
        // server sends an OpenCaptureScreenPacket.
        OpenCaptureScreenPacket.registerClient();

        KawaiPalsMod.LOGGER.info("[KawaiPals] Client initialisation complete.");
    }
}
