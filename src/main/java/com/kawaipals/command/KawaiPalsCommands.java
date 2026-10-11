package com.kawaipals.command;

import com.kawaipals.core.PalType;
import com.kawaipals.entity.PalEntity;
import com.kawaipals.item.PalOrbItem;
import com.kawaipals.network.OpenCaptureScreenPacket;
import com.kawaipals.registry.ModEntities;
import com.kawaipals.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class KawaiPalsCommands {

    private KawaiPalsCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        List<String> palSuggestions = new ArrayList<>();
        palSuggestions.add("all");
        for (PalType pal : PalType.values()) {
            palSuggestions.add(pal.id);
        }

        dispatcher.register(
            Commands.literal("kawaipals")
                .requires(source -> true)
                .then(Commands.literal("give")
                    .then(Commands.argument("pal", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(palSuggestions, builder))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String arg = StringArgumentType.getString(ctx, "pal").toLowerCase();

                            if ("all".equals(arg)) {
                                for (PalType type : PalType.values()) {
                                    ItemStack orb = PalOrbItem.createCaptured(type);
                                    player.getInventory().add(orb);
                                }
                                ctx.getSource().sendSuccess(() -> Component.literal("✨ ¡Has recibido los 12 orbes de espíritus KawaiPals!")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
                                return 1;
                            }

                            PalType type = PalType.fromId(arg);
                            if (type == null) {
                                ctx.getSource().sendFailure(Component.literal("Espíritu desconocido: " + arg));
                                return 0;
                            }

                            ItemStack orb = PalOrbItem.createCaptured(type);
                            player.getInventory().add(orb);
                            ctx.getSource().sendSuccess(() -> Component.literal("✨ ¡Has recibido el orbe de " + type.displayName + "!")
                                .withStyle(type.rarity.color), false);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("spawn")
                    .then(Commands.argument("pal", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(palSuggestions, builder))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String arg = StringArgumentType.getString(ctx, "pal").toLowerCase();
                            PalType type = PalType.fromId(arg);

                            if (type == null) {
                                ctx.getSource().sendFailure(Component.literal("Espíritu desconocido: " + arg));
                                return 0;
                            }

                            net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player.level();
                            PalEntity pal = new PalEntity(ModEntities.PAL_ENTITY, serverLevel);
                            pal.setPalType(type);
                            pal.setOwnerUUID(player.getUUID());
                            pal.setPos(player.getX(), player.getY(), player.getZ());
                            serverLevel.addFreshEntity(pal);

                            ctx.getSource().sendSuccess(() -> Component.literal("🐾 ¡" + type.displayName + " ha aparecido a tu lado!")
                                .withStyle(type.rarity.color), false);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("minigame")
                    .then(Commands.argument("pal", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(palSuggestions, builder))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String arg = StringArgumentType.getString(ctx, "pal").toLowerCase();
                            PalType type = PalType.fromId(arg);

                            if (type == null) {
                                type = PalType.GOTITA;
                            }

                            OpenCaptureScreenPacket.send(player, type);
                            ctx.getSource().sendSuccess(() -> Component.literal("🎮 Abriendo minijuego de captura para " + arg + "...")
                                .withStyle(ChatFormatting.AQUA), false);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("compendium")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        player.getInventory().add(new ItemStack(ModItems.COMPENDIUM));
                        ctx.getSource().sendSuccess(() -> Component.literal("📖 ¡Has recibido el Compendio Kawaii!")
                            .withStyle(ChatFormatting.GOLD), false);
                        return 1;
                    })
                )
                .then(Commands.literal("build_shrine")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        com.kawaipals.worldgen.KawaiiShrineBuilder.build(player.level(), player.blockPosition());
                        ctx.getSource().sendSuccess(() -> Component.literal("⛩️ ¡Santuario Kawaii construido con éxito!")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
                        return 1;
                    })
                )
        );
    }
}
