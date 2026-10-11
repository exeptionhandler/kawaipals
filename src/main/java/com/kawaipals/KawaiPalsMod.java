package com.kawaipals;

import com.kawaipals.network.CaptureSuccessPacket;
import com.kawaipals.network.OpenCaptureScreenPacket;
import com.kawaipals.registry.ModEntities;
import com.kawaipals.registry.ModItemGroups;
import com.kawaipals.registry.ModItems;
import com.kawaipals.worldgen.KawaiPalsLootModifier;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KawaiPalsMod – the common (server + client) mod entry point.
 *
 * <p>Fabric calls {@link #onInitialize()} once on both the dedicated server
 * and the integrated server side of a client. Client-only work (rendering,
 * screens) lives in {@link KawaiPalsClient}.
 *
 * <p>Responsibilities here:
 * <ul>
 *   <li>Register entities, items, item groups.</li>
 *   <li>Register network packets (both S2C and C2S ids).</li>
 *   <li>Register loot-table modifications.</li>
 * </ul>
 */
public class KawaiPalsMod implements ModInitializer {

    // ── Mod metadata ─────────────────────────────────────────────────────────

    /** The canonical mod ID used in all {@code Identifier.of(MOD_ID, …)} calls. */
    public static final String MOD_ID = "kawaipals";

    /** Shared logger; usage: {@code KawaiPalsMod.LOGGER.info("…")}. */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ── ModInitializer ────────────────────────────────────────────────────────

    @Override
    public void onInitialize() {
        LOGGER.info("[KawaiPals] Initialising KawaiPals mod…");

        // 1. Register all entity types first (items may reference entity types)
        ModEntities.register();

        // 2. Register items
        ModItems.register();

        // 3. Register creative-mode item groups
        ModItemGroups.register();

        // 4. Register network packets
        //    – C2S: client tells server a minigame was won
        CaptureSuccessPacket.registerServer();
        //    – S2C: server tells client to open the capture screen
        OpenCaptureScreenPacket.registerServer();

        // 5. Register loot-table injections
        KawaiPalsLootModifier.register();

        // 6. Register chest encounter event (opening structure chests wakes up KawaiPals!)
        com.kawaipals.worldgen.ChestEncounterHandler.register();

        // 7. Register debug & player commands (/kawaipals give, spawn, minigame, build_shrine)
        com.kawaipals.command.KawaiPalsCommands.register();

        // 8. Register custom shrine worldgen features
        com.kawaipals.worldgen.ModWorldGen.register();

        LOGGER.info("[KawaiPals] Initialisation complete.");
    }
}
