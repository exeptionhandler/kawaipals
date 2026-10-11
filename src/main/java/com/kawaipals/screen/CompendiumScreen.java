package com.kawaipals.screen;

import com.kawaipals.core.PalRarity;
import com.kawaipals.core.PalType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * CompendiumScreen – Pokédex-style GUI showing all 12 collectible Pals.
 */
public class CompendiumScreen extends Screen {

    private static final int W = 340;
    private static final int H = 220;

    private PalRarity activeFilter = null;
    private PalType selectedPal = PalType.GOTITA;

    private final List<PalType> displayedPals = new ArrayList<>();

    public CompendiumScreen() {
        super(Component.translatable("kawaipals.compendium.title"));
        updateDisplayedPals();
    }

    private void updateDisplayedPals() {
        displayedPals.clear();
        for (PalType pal : PalType.values()) {
            if (activeFilter == null || pal.rarity == activeFilter) {
                displayedPals.add(pal);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        int btnX = left + 10;
        int btnY = top + 10;
        int btnW = 38;
        int btnH = 16;

        this.addRenderableWidget(Button.builder(Component.literal("ALL"), b -> {
            activeFilter = null;
            updateDisplayedPals();
        }).bounds(btnX, btnY, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(Component.literal("COM"), b -> {
            activeFilter = PalRarity.COMMON;
            updateDisplayedPals();
        }).bounds(btnX + 42, btnY, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(Component.literal("RAR"), b -> {
            activeFilter = PalRarity.RARE;
            updateDisplayedPals();
        }).bounds(btnX + 84, btnY, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(Component.literal("EPC"), b -> {
            activeFilter = PalRarity.EPIC;
            updateDisplayedPals();
        }).bounds(btnX + 126, btnY, btnW, btnH).build());

        this.addRenderableWidget(Button.builder(Component.literal("LEG"), b -> {
            activeFilter = PalRarity.LEGENDARY;
            updateDisplayedPals();
        }).bounds(btnX + 168, btnY, btnW, btnH).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;

        extractor.fill(left, top, left + W, top + H, 0xEE1A132B);
        extractor.fill(left + 2, top + 2, left + W - 2, top + H - 2, 0xDD281E3D);
        extractor.fill(left + 195, top + 32, left + 197, top + H - 10, 0x44FFFFFF);

        int gridX = left + 10;
        int gridY = top + 36;
        int cols = 3;
        int cellW = 58;
        int cellH = 42;

        for (int i = 0; i < displayedPals.size(); i++) {
            PalType pal = displayedPals.get(i);
            int row = i / cols;
            int col = i % cols;
            int cx = gridX + col * cellW;
            int cy = gridY + row * cellH;

            boolean isHovered = mouseX >= cx && mouseX < cx + cellW - 4 && mouseY >= cy && mouseY < cy + cellH - 4;
            int bgColor = (pal == selectedPal) ? 0x669955FF : (isHovered ? 0x44FFFFFF : 0x22FFFFFF);

            extractor.fill(cx, cy, cx + cellW - 4, cy + cellH - 4, bgColor);
            extractor.text(this.font, pal.displayName, cx + 4, cy + 8, 0xFFFFFF, false);
            extractor.text(this.font, pal.rarity.displayName.substring(0, 3), cx + 4, cy + 22, pal.rarity.rgbColor, false);
        }

        if (selectedPal != null) {
            int dx = left + 205;
            int dy = top + 38;

            extractor.text(this.font, Component.literal(selectedPal.displayName)
                .withStyle(selectedPal.rarity.color, ChatFormatting.BOLD), dx, dy, 0xFFFFFF, false);

            extractor.text(this.font, Component.literal("★ " + selectedPal.rarity.displayName)
                .withStyle(selectedPal.rarity.color), dx, dy + 16, 0xCCCCCC, false);

            extractor.text(this.font, Component.literal("✦ Poder Especial:").withStyle(ChatFormatting.GOLD), dx, dy + 36, 0xFFCC00, false);
            extractor.textWithWordWrap(this.font, Component.literal(selectedPal.powerDescription), dx, dy + 50, 120, 0xDDDDDD);

            if (selectedPal == PalType.PEGASITO) {
                extractor.text(this.font, Component.literal("¡Montable y Vuela!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), dx, dy + 110, 0x55FFFF, false);
            }
        }

        int collectedCount = 3;
        extractor.text(this.font, Component.literal("Colección: " + collectedCount + "/12").withStyle(ChatFormatting.LIGHT_PURPLE), left + 220, top + 14, 0xFFAAFF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int left = (this.width - W) / 2;
        int top = (this.height - H) / 2;
        int gridX = left + 10;
        int gridY = top + 36;
        int cols = 3;
        int cellW = 58;
        int cellH = 42;

        double mouseX = event.x();
        double mouseY = event.y();

        for (int i = 0; i < displayedPals.size(); i++) {
            PalType pal = displayedPals.get(i);
            int row = i / cols;
            int col = i % cols;
            int cx = gridX + col * cellW;
            int cy = gridY + row * cellH;

            if (mouseX >= cx && mouseX < cx + cellW - 4 && mouseY >= cy && mouseY < cy + cellH - 4) {
                selectedPal = pal;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
