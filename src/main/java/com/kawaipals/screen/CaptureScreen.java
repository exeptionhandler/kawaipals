package com.kawaipals.screen;

import com.kawaipals.core.PalRarity;
import com.kawaipals.core.PalType;
import com.kawaipals.network.CaptureSuccessPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * CaptureScreen – interactive minigame screen when opening a chest with a Pal!
 */
public class CaptureScreen extends Screen {

    private enum Minigame { TIMING_RING, SEQUENCE_QTE, AFFINITY_BAR }
    private enum Phase { CHEST_SHAKE, MINIGAME, SUCCESS, FAIL }

    private static final int W = 300;
    private static final int H = 200;

    private final PalType palType;
    private final PalRarity rarity;
    private final Minigame minigame;
    private Phase phase = Phase.CHEST_SHAKE;

    private int phaseTicks = 0;
    private static final int SHAKE_DURATION = 40;

    // Minigame 1: Timing Ring
    private float currentRadius;
    private final float targetRadius;
    private final float shrinkSpeed;

    // Minigame 2: Sequence QTE
    private final List<Integer> keySequence = new ArrayList<>();
    private int currentKeyIndex = 0;
    private int qteTimer;

    // Minigame 3: Affinity Bar
    private float indicatorPos = 0.5f;
    private float sweetSpotPos = 0.5f;
    private float affinityProgress = 0.0f;
    private boolean mouseHeld = false;

    private static final RandomSource RANDOM = RandomSource.create();

    public CaptureScreen(PalType palType) {
        super(Component.translatable("kawaipals.screen.capture.title"));
        this.palType = palType;
        this.rarity = palType.rarity;

        Minigame[] games = Minigame.values();
        this.minigame = games[RANDOM.nextInt(games.length)];

        this.targetRadius = rarity.captureCircleSize;
        this.currentRadius = targetRadius + 60f;
        this.shrinkSpeed = rarity.captureSpeed;

        int seqLen = switch (rarity) {
            case COMMON -> 3;
            case RARE -> 4;
            case EPIC -> 4;
            case LEGENDARY -> 5;
        };
        int[] arrowKeys = { 265, 264, 263, 262 };
        for (int i = 0; i < seqLen; i++) {
            keySequence.add(arrowKeys[RANDOM.nextInt(arrowKeys.length)]);
        }
        this.qteTimer = 200;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        phaseTicks++;

        switch (phase) {
            case CHEST_SHAKE -> {
                if (phaseTicks >= SHAKE_DURATION) {
                    phase = Phase.MINIGAME;
                    phaseTicks = 0;
                    playSound(SoundEvents.PLAYER_LEVELUP, 1.2f);
                }
            }
            case MINIGAME -> tickMinigame();
            case SUCCESS -> {
                if (phaseTicks >= 30) onClose();
            }
            case FAIL -> {
                if (phaseTicks >= 40) onClose();
            }
        }
    }

    private void tickMinigame() {
        switch (minigame) {
            case TIMING_RING -> {
                currentRadius -= shrinkSpeed;
                if (currentRadius < 5f) {
                    currentRadius = targetRadius + 60f;
                }
            }
            case SEQUENCE_QTE -> {
                if (--qteTimer <= 0) {
                    failCapture();
                }
            }
            case AFFINITY_BAR -> {
                sweetSpotPos += (float) Math.sin(phaseTicks * 0.1) * 0.03f;
                sweetSpotPos = Math.max(0.2f, Math.min(0.8f, sweetSpotPos));

                if (mouseHeld) {
                    indicatorPos += 0.04f;
                } else {
                    indicatorPos -= 0.03f;
                }
                indicatorPos = Math.max(0.0f, Math.min(1.0f, indicatorPos));

                float tolerance = (rarity == PalRarity.LEGENDARY) ? 0.08f : 0.15f;
                if (Math.abs(indicatorPos - sweetSpotPos) < tolerance) {
                    affinityProgress += 0.015f;
                    if (affinityProgress >= 1.0f) {
                        succeedCapture();
                    }
                } else {
                    affinityProgress = Math.max(0.0f, affinityProgress - 0.005f);
                }
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        extractor.fill(left, top, left + W, top + H, 0xDD111625);
        extractor.fill(left + 2, top + 2, left + W - 2, top + H - 2, 0xEE1E2438);

        switch (phase) {
            case CHEST_SHAKE -> renderChestShake(extractor, left, top);
            case MINIGAME -> renderMinigame(extractor, left, top);
            case SUCCESS -> renderSuccess(extractor, left, top);
            case FAIL -> renderFail(extractor, left, top);
        }
    }

    private void renderChestShake(GuiGraphicsExtractor extractor, int left, int top) {
        int cx = left + W / 2;
        int cy = top + H / 2;

        int shakeX = (phaseTicks % 4 < 2) ? -4 : 4;
        extractor.centeredText(this.font, Component.literal("✦ ¡UN ESPÍRITU ESTÁ SALIENDO DEL COFRE! ✦")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), cx + shakeX, cy - 30, 0xFFFFFFFF);

        extractor.centeredText(this.font, Component.literal("✧ " + palType.displayName + " ✧")
            .withStyle(rarity.color, ChatFormatting.BOLD), cx, cy, 0xFFFFFFFF);

        extractor.centeredText(this.font, Component.literal("★ Rareza: " + rarity.displayName)
            .withStyle(rarity.color), cx, cy + 20, 0xFFAAAAAA);
    }

    private void renderMinigame(GuiGraphicsExtractor extractor, int left, int top) {
        int cx = left + W / 2;
        int cy = top + H / 2;

        extractor.centeredText(this.font, Component.literal("¡CAPTURANDO A " + palType.displayName.toUpperCase() + "!")
            .withStyle(rarity.color, ChatFormatting.BOLD), cx, top + 15, 0xFFFFFFFF);

        switch (minigame) {
            case TIMING_RING -> {
                extractor.centeredText(this.font, Component.literal("Haz clic cuando el anillo exterior coincida con el objetivo"), cx, top + 35, 0xFFDDDDDD);

                int tr = (int) targetRadius;
                extractor.fill(cx - tr, cy - tr, cx + tr, cy + tr, 0x5500FF88);

                int cr = (int) currentRadius;
                extractor.fill(cx - cr, cy - cr, cx + cr, cy - cr + 2, 0xFFFFFFFF);
                extractor.fill(cx - cr, cy + cr - 2, cx + cr, cy + cr, 0xFFFFFFFF);
                extractor.fill(cx - cr, cy - cr, cx - cr + 2, cy + cr, 0xFFFFFFFF);
                extractor.fill(cx + cr - 2, cy - cr, cx + cr, cy + cr, 0xFFFFFFFF);
            }
            case SEQUENCE_QTE -> {
                extractor.centeredText(this.font, Component.literal("Presiona las flechas en orden antes de que se acabe el tiempo:"), cx, top + 35, 0xFFDDDDDD);

                StringBuilder seqStr = new StringBuilder();
                for (int i = 0; i < keySequence.size(); i++) {
                    String arrow = switch (keySequence.get(i)) {
                        case 265 -> "↑ ";
                        case 264 -> "↓ ";
                        case 263 -> "← ";
                        case 262 -> "→ ";
                        default -> "? ";
                    };
                    seqStr.append(i < currentKeyIndex ? "[✓] " : arrow);
                }

                extractor.centeredText(this.font, Component.literal(seqStr.toString())
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), cx, cy, 0xFFFFFFFF);

                int barW = (int) ((qteTimer / 200.0f) * 160);
                extractor.fill(cx - 80, cy + 30, cx - 80 + barW, cy + 36, 0xFFFF4444);
            }
            case AFFINITY_BAR -> {
                extractor.centeredText(this.font, Component.literal("Mantén presionado el clic para mantener el corazón en la zona verde"), cx, top + 35, 0xFFDDDDDD);

                int barX = cx - 15;
                int barY = top + 55;
                int barH = 90;

                extractor.fill(barX, barY, barX + 30, barY + barH, 0x88222222);

                int sweetY = barY + (int) ((1.0f - sweetSpotPos) * (barH - 25));
                extractor.fill(barX, sweetY, barX + 30, sweetY + 25, 0xAA00DD66);

                int indY = barY + (int) ((1.0f - indicatorPos) * (barH - 10));
                extractor.fill(barX + 5, indY, barX + 25, indY + 10, 0xFFFFCC00);

                extractor.centeredText(this.font, Component.literal("Amistad: " + (int)(affinityProgress * 100) + "%"), cx, top + 160, 0xFFFFFFAA);
            }
        }
    }

    private void renderSuccess(GuiGraphicsExtractor extractor, int left, int top) {
        int cx = left + W / 2;
        int cy = top + H / 2;
        extractor.centeredText(this.font, Component.literal("✨ ¡ESPÍRITU CAPTURADO CON ÉXITO! ✨")
            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), cx, cy - 20, 0xFF55FF55);
        extractor.centeredText(this.font, Component.literal(palType.displayName + " ahora es tu compañero!")
            .withStyle(rarity.color), cx, cy + 5, 0xFFFFFFFF);
    }

    private void renderFail(GuiGraphicsExtractor extractor, int left, int top) {
        int cx = left + W / 2;
        int cy = top + H / 2;
        extractor.centeredText(this.font, Component.literal("💨 ¡El espíritu se asustó y escapó!")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), cx, cy - 10, 0xFFFF5555);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (phase == Phase.MINIGAME) {
            if (minigame == Minigame.TIMING_RING) {
                float diff = Math.abs(currentRadius - targetRadius);
                float tolerance = (rarity == PalRarity.LEGENDARY) ? 8f : 16f;
                if (diff <= tolerance) {
                    succeedCapture();
                } else {
                    currentRadius = targetRadius + 60f;
                    playSound(SoundEvents.ITEM_BREAK.value(), 1.2f);
                }
                return true;
            } else if (minigame == Minigame.AFFINITY_BAR) {
                mouseHeld = true;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (minigame == Minigame.AFFINITY_BAR) {
            mouseHeld = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (phase == Phase.MINIGAME && minigame == Minigame.SEQUENCE_QTE) {
            if (currentKeyIndex < keySequence.size()) {
                if (event.key() == keySequence.get(currentKeyIndex)) {
                    currentKeyIndex++;
                    playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.4f);
                    if (currentKeyIndex >= keySequence.size()) {
                        succeedCapture();
                    }
                    return true;
                } else {
                    playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8f);
                    failCapture();
                    return true;
                }
            }
        }
        return super.keyPressed(event);
    }

    private void succeedCapture() {
        phase = Phase.SUCCESS;
        phaseTicks = 0;
        playSound(SoundEvents.PLAYER_LEVELUP, 1.0f);
        CaptureSuccessPacket.send(palType);
    }

    private void failCapture() {
        phase = Phase.FAIL;
        phaseTicks = 0;
        playSound(SoundEvents.BAT_TAKEOFF, 0.8f);
    }

    private void playSound(net.minecraft.sounds.SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }
}
