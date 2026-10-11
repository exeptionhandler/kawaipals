package com.kawaipals.item;

import com.kawaipals.core.PalType;
import com.kawaipals.entity.PalEntity;
import com.kawaipals.registry.ModEntities;
import com.kawaipals.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * PalOrbItem – the core collectible item that stores a captured Pal.
 */
public class PalOrbItem extends Item {

    private static final String NBT_PAL_TYPE = "palType";
    private static final String NBT_CAPTURED = "captured";
    private static final String NBT_PAL_UUID = "palUUID";

    public PalOrbItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createCaptured(PalType type) {
        ItemStack stack = new ItemStack(ModItems.PAL_ORB);
        setCaptured(stack, true);
        setPalType(stack, type);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Orbe de " + type.displayName).withStyle(type.rarity.color));
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (level instanceof ServerLevel serverLevel) {
                return recallPal(stack, player, serverLevel);
            }
            return InteractionResult.SUCCESS;
        }

        if (!isCaptured(stack)) {
            if (player.isCreative()) {
                PalType[] types = PalType.values();
                PalType randomType = types[player.getRandom().nextInt(types.length)];
                setCaptured(stack, true);
                setPalType(stack, randomType);
                stack.set(DataComponents.CUSTOM_NAME, Component.literal("Orbe de " + randomType.displayName).withStyle(randomType.rarity.color));
                if (level.isClientSide()) {
                    player.sendSystemMessage(Component.literal("¡Orbe cargado con " + randomType.displayName + "! Clic de nuevo para invocarlo.")
                        .withStyle(randomType.rarity.color));
                }
                return InteractionResult.SUCCESS;
            }
            if (level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("kawaipals.orb.empty")
                    .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            return summonPal(stack, player, serverLevel);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        PalType type = getPalType(stack);
        if (type != null && isCaptured(stack)) {
            tooltip.accept(Component.literal("★ " + type.rarity.displayName + " ★").withStyle(type.rarity.color));
            tooltip.accept(Component.literal("✦ " + type.powerDescription).withStyle(ChatFormatting.GRAY));
            if (type == PalType.PEGASITO) {
                tooltip.accept(Component.literal("🦄 ¡Montable y vuela libremente!").withStyle(ChatFormatting.AQUA));
            }
        } else {
            tooltip.accept(Component.translatable("kawaipals.orb.empty").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private InteractionResult summonPal(ItemStack stack, Player player, ServerLevel level) {
        PalType type = getPalType(stack);
        if (type == null) return InteractionResult.FAIL;

        PalEntity pal = new PalEntity(ModEntities.PAL_ENTITY, level);
        pal.setPalType(type);
        pal.setOwnerUUID(player.getUUID());
        pal.setPos(player.getX(), player.getY(), player.getZ());

        level.addFreshEntity(pal);

        setPalUUID(stack, pal.getUUID());
        setCaptured(stack, false);

        player.sendSystemMessage(
            Component.translatable("kawaipals.orb.summoned", type.displayName)
                .withStyle(type.rarity.color));

        level.playSound(null, player.blockPosition(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.4f);

        return InteractionResult.SUCCESS;
    }

    private InteractionResult recallPal(ItemStack stack, Player player, ServerLevel level) {
        if (isCaptured(stack)) {
            player.sendSystemMessage(
                Component.translatable("kawaipals.orb.already_captured")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }

        UUID palUUID = getPalUUID(stack);
        if (palUUID == null) return InteractionResult.FAIL;

        PalEntity found = null;
        for (PalEntity entity : level.getEntities(ModEntities.PAL_ENTITY,
                player.getBoundingBox().inflate(16),
                e -> palUUID.equals(e.getUUID()))) {
            found = entity;
            break;
        }

        if (found == null) {
            player.sendSystemMessage(
                Component.translatable("kawaipals.orb.not_nearby")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        found.discard();
        setCaptured(stack, true);

        PalType type = getPalType(stack);
        player.sendSystemMessage(
            Component.translatable("kawaipals.orb.recalled",
                type != null ? type.displayName : "Pal")
                .withStyle(ChatFormatting.GREEN));

        return InteractionResult.SUCCESS;
    }



    @Override
    public boolean isFoil(ItemStack stack) {
        return isCaptured(stack);
    }

    public static boolean isCaptured(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(NBT_CAPTURED).orElse(false);
    }

    public static void setCaptured(ItemStack stack, boolean captured) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(NBT_CAPTURED, captured));
    }

    @Nullable
    public static PalType getPalType(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        return PalType.fromId(data.copyTag().getString(NBT_PAL_TYPE).orElse(""));
    }

    public static void setPalType(ItemStack stack, PalType type) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(NBT_PAL_TYPE, type.id));
        stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(com.kawaipals.KawaiPalsMod.MOD_ID, "pal_orb_" + type.id));
    }

    @Nullable
    public static UUID getPalUUID(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String id = data.copyTag().getString(NBT_PAL_UUID).orElse("");
        return id.isEmpty() ? null : UUID.fromString(id);
    }

    public static void setPalUUID(ItemStack stack, UUID uuid) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(NBT_PAL_UUID, uuid.toString()));
    }
}
