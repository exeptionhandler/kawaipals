package com.kawaipals.worldgen;

import com.kawaipals.core.PalType;
import com.kawaipals.network.OpenCaptureScreenPacket;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * ChestEncounterHandler – triggers the KawaiPals capture sequence when opening chests.
 */
public final class ChestEncounterHandler {

    private static final Set<BlockPos> TRIGGERED_CHESTS = new HashSet<>();
    private static final Random RANDOM = new Random();

    private ChestEncounterHandler() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.getBlock() instanceof ChestBlock || state.getBlock() instanceof BarrelBlock) {
                    if (!TRIGGERED_CHESTS.contains(pos)) {
                        BlockEntity be = world.getBlockEntity(pos);
                        boolean isNaturalLoot = (be instanceof RandomizableContainerBlockEntity rc && rc.getLootTable() != null);

                        // Always trigger on natural structure chests; 35% chance on any regular chest for testing!
                        if (isNaturalLoot || RANDOM.nextFloat() < 0.35f) {
                            TRIGGERED_CHESTS.add(pos);
                            triggerEncounter(serverPlayer, (ServerLevel) world, pos);
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    private static void triggerEncounter(ServerPlayer player, ServerLevel level, BlockPos pos) {
        PalType[] all = PalType.values();
        PalType pal = all[RANDOM.nextInt(all.length)];

        // Particle burst
        level.sendParticles(ParticleTypes.HEART, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.1);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.05);

        // Chime sound
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);

        // Tell player
        player.sendSystemMessage(
            Component.literal("✨ ¡Un espíritu salvaje ha despertado en el cofre!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
        );

        // Open capture screen minigame
        OpenCaptureScreenPacket.send(player, pal);
    }
}
