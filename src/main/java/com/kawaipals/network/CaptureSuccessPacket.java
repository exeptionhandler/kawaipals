package com.kawaipals.network;

import com.kawaipals.KawaiPalsMod;
import com.kawaipals.core.PalType;
import com.kawaipals.item.PalOrbItem;
import com.kawaipals.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * CaptureSuccessPacket – Client-to-Server packet sent when minigame succeeds.
 */
public record CaptureSuccessPacket(String palTypeId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CaptureSuccessPacket> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "capture_success"));

    public static final StreamCodec<FriendlyByteBuf, CaptureSuccessPacket> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> buf.writeUtf(pkt.palTypeId), buf -> new CaptureSuccessPacket(buf.readUtf()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(PalType palType) {
        ClientPlayNetworking.send(new CaptureSuccessPacket(palType.id));
    }

    public static void registerServer() {
        PayloadTypeRegistry.serverboundPlay().register(TYPE, STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            context.server().execute(() -> handleOnServer(context.player(), payload.palTypeId()));
        });
    }

    private static void handleOnServer(ServerPlayer player, String palTypeId) {
        PalType palType = PalType.fromId(palTypeId);
        if (palType == null) return;

        ItemStack orb = new ItemStack(ModItems.PAL_ORB);
        PalOrbItem.setPalType(orb, palType);
        PalOrbItem.setCaptured(orb, true);

        if (!player.getInventory().add(orb)) {
            player.drop(orb, false);
        }
    }
}
