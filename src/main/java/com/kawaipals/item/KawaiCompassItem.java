package com.kawaipals.item;

import com.kawaipals.KawaiPalsMod;
import com.kawaipals.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * KawaiCompassItem – magical compass that leads the player to the nearest chest containing a KawaiPal spirit!
 */
public class KawaiCompassItem extends Item {

    public KawaiCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            locateNearestPalChest(stack, serverPlayer, serverLevel);
        }

        return InteractionResult.SUCCESS;
    }

    private void locateNearestPalChest(ItemStack stack, ServerPlayer player, ServerLevel level) {
        BlockPos playerPos = player.blockPosition();
        int cx = playerPos.getX() >> 4;
        int cz = playerPos.getZ() >> 4;

        BlockPos closestPos = null;
        double closestDistSq = Double.MAX_VALUE;
        String foundType = "Cofre con Espíritu";

        // 1. Scan loaded chunks around player (radius 10 chunks = ~160 blocks)
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx + dx, cz + dz);
                if (chunk == null) continue;

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos bpos = entry.getKey();
                    BlockEntity be = entry.getValue();
                    boolean isTarget = false;

                    // A: Natural loot table chest (dungeon, pyramid, sanctuary, etc.)
                    if (be instanceof RandomizableContainerBlockEntity rc) {
                        ResourceKey<?> lootKey = rc.getLootTable();
                        if (lootKey != null) {
                            String namespace = lootKey.identifier().getNamespace();
                            String path = lootKey.identifier().getPath();

                            if (KawaiPalsMod.MOD_ID.equals(namespace)
                                    || path.contains("dungeon")
                                    || path.contains("pyramid")
                                    || path.contains("mineshaft")
                                    || path.contains("bastion")
                                    || path.contains("ancient_city")
                                    || path.contains("end_city")
                                    || path.contains("sanctuario")
                                    || path.contains("ruinas")) {
                                isTarget = true;
                            }
                        }
                    }

                    // B: Chest with an existing PalOrb inside
                    if (!isTarget && be instanceof Container container) {
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            ItemStack item = container.getItem(i);
                            if (item.is(ModItems.PAL_ORB)) {
                                isTarget = true;
                                break;
                            }
                        }
                    }

                    if (isTarget) {
                        double dsq = playerPos.distSqr(bpos);
                        if (dsq < closestDistSq) {
                            closestDistSq = dsq;
                            closestPos = bpos;
                            foundType = "Cofre con Espíritu";
                        }
                    }
                }
            }
        }

        // 2. If no chest in loaded chunks, search for nearby structures
        if (closestPos == null) {
            BlockPos structPos = level.findNearestMapStructure(StructureTags.MINESHAFT, playerPos, 64, false);
            if (structPos == null) {
                structPos = level.findNearestMapStructure(StructureTags.ON_DESERT_VILLAGE_MAPS, playerPos, 64, false);
            }
            if (structPos == null) {
                structPos = level.findNearestMapStructure(StructureTags.OCEAN_RUIN, playerPos, 64, false);
            }

            if (structPos != null) {
                closestPos = structPos;
                closestDistSq = playerPos.distSqr(structPos);
                foundType = "Estructura Ancestral";
            }
        }

        // 3. Handle result
        if (closestPos != null) {
            // Bind needle to the chest using LodestoneTracker
            stack.set(DataComponents.LODESTONE_TRACKER,
                new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), closestPos)), false));

            final BlockPos targetPosFinal = closestPos;
            final String finalFoundType = foundType;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                tag.putInt("targetX", targetPosFinal.getX());
                tag.putInt("targetY", targetPosFinal.getY());
                tag.putInt("targetZ", targetPosFinal.getZ());
                tag.putString("targetName", finalFoundType);
            });

            // Calculate distance & direction
            int distance = (int) Math.sqrt(closestDistSq);
            double dX = closestPos.getX() - playerPos.getX();
            double dZ = closestPos.getZ() - playerPos.getZ();
            double angle = Math.toDegrees(Math.atan2(dZ, dX)) - 90;
            if (angle < 0) angle += 360;
            String cardinal = getCardinal(angle);

            // Particles: magical trail towards the target
            Vec3 eye = player.getEyePosition();
            Vec3 targetCenter = Vec3.atCenterOf(closestPos);
            Vec3 ray = targetCenter.subtract(eye).normalize();
            for (int step = 1; step <= 10; step++) {
                Vec3 p = eye.add(ray.scale(step * 0.7));
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.01);
                level.sendParticles(ParticleTypes.ENCHANT, p.x, p.y, p.z, 2, 0.08, 0.08, 0.08, 0.05);
            }

            // Audio
            level.playSound(null, playerPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.4f);
            level.playSound(null, playerPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7f, 1.8f);

            // Actionbar Message
            if (distance <= 8) {
                player.sendSystemMessage(
                    Component.literal("💖 ¡Estás justo encima! El cofre con espíritu está aquí (~" + distance + "m)")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                    true
                );
            } else {
                player.sendSystemMessage(
                    Component.literal("🧭 ¡" + foundType + " detectado a " + distance + "m hacia el " + cardinal + "! (Y: ~" + closestPos.getY() + ")")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                    true
                );
            }
        } else {
            level.playSound(null, playerPos, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            player.sendSystemMessage(
                Component.literal("💨 No se detectan cofres con espíritus en este área... ¡Sigue explorando!")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                true
            );
        }
    }

    private static String getCardinal(double angle) {
        if (angle >= 337.5 || angle < 22.5) return "Sur";
        if (angle >= 22.5 && angle < 67.5) return "Suroeste";
        if (angle >= 67.5 && angle < 112.5) return "Oeste";
        if (angle >= 112.5 && angle < 157.5) return "Noroeste";
        if (angle >= 157.5 && angle < 202.5) return "Norte";
        if (angle >= 202.5 && angle < 247.5) return "Noreste";
        if (angle >= 247.5 && angle < 292.5) return "Este";
        return "Sureste";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        return tracker != null && tracker.target().isPresent();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("★ Guía Astral Kawaii ★").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        tooltip.accept(Component.literal("Rastrea y guía hacia cofres con espíritus KawaiPals.").withStyle(ChatFormatting.GRAY));

        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker != null && tracker.target().isPresent()) {
            BlockPos bp = tracker.target().get().pos();
            tooltip.accept(Component.literal(""));
            tooltip.accept(Component.literal("✦ Señal fijada:").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.literal("  Coordenadas: X: " + bp.getX() + "  Y: " + bp.getY() + "  Z: " + bp.getZ()).withStyle(ChatFormatting.WHITE));
            tooltip.accept(Component.literal("✦ [Clic derecho para reescanear]").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.literal(""));
            tooltip.accept(Component.literal("✦ [Clic derecho para escanear cofres]").withStyle(ChatFormatting.YELLOW));
        }
    }
}
