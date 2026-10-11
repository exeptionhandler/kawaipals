package com.kawaipals.network;

import com.kawaipals.KawaiPalsMod;
import com.kawaipals.core.PalType;
import com.kawaipals.screen.CaptureScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * OpenCaptureScreenPacket – Server-to-Client packet to trigger minigame screen.
 */
public record OpenCaptureScreenPacket(String palTypeId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenCaptureScreenPacket> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(KawaiPalsMod.MOD_ID, "open_capture_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenCaptureScreenPacket> STREAM_CODEC =
        StreamCodec.of((buf, pkt) -> buf.writeUtf(pkt.palTypeId), buf -> new OpenCaptureScreenPacket(buf.readUtf()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(ServerPlayer player, PalType palType) {
        ServerPlayNetworking.send(player, new OpenCaptureScreenPacket(palType.id));
    }

    public static void registerServer() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, STREAM_CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
            context.client().execute(() -> {
                PalType palType = PalType.fromId(payload.palTypeId());
                if (palType != null) {
                    Minecraft.getInstance().setScreenAndShow(new CaptureScreen(palType));
                }
            });
        });
    }
}
