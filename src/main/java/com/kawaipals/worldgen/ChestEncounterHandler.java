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

        // 1. Physically open the 3D chest lid in the world!
        level.blockEvent(pos, level.getBlockState(pos).getBlock(), 1, 1);
        level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);

        // 2. Particle burst of hearts, enchantment sparkles, and totem dust
        level.sendParticles(ParticleTypes.HEART, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 25, 0.4, 0.5, 0.4, 0.15);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.5, 0.8, 0.5, 0.2);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 20, 0.3, 0.5, 0.3, 0.1);

        // 3. Magical sounds
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 1.3f);
        level.playSound(null, pos, SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.NEUTRAL, 1.0f, 1.2f);

        // 4. Spawn the wild companion physically emerging upwards from inside the chest
        com.kawaipals.entity.PalEntity wildPal = new com.kawaipals.entity.PalEntity(com.kawaipals.registry.ModEntities.PAL_ENTITY, level);
        wildPal.setPalType(pal);
        wildPal.setPos(pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5);
        wildPal.setDeltaMovement(0.0, 0.32, 0.0);
        level.addFreshEntity(wildPal);

        // Tell player
        player.sendSystemMessage(
            Component.literal("✨ ¡" + pal.displayName + " ha salido del cofre!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
        );

        // Open capture screen minigame
        OpenCaptureScreenPacket.send(player, pal);
    }
}
