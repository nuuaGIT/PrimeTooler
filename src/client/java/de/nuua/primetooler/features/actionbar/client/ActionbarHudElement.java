package de.nuua.primetooler.features.actionbar.client;

import de.nuua.primetooler.api.v1.client.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * WHY: Make the actionbar overlay position configurable via the HUD editor.
 * PERF: No work when disabled; editor-only preview otherwise.
 */
public final class ActionbarHudElement implements HudElement {
	public static final String ID = "primetooler:actionbar";
	private static final int DEFAULT_Y_OFFSET = 72;
	private static final int DEFAULT_TEXT_Y_OFFSET = 4;
	private static final Component PREVIEW = Component.literal("Actionbar");

	@Override
	public String id() {
		return ID;
	}

	@Override
	public int defaultX(Minecraft client, int screenWidth, int screenHeight) {
		Minecraft mc = client == null ? Minecraft.getInstance() : client;
		Font font = mc.font;
		int w = font.width(PREVIEW);
		return (screenWidth - w) / 2;
	}

	@Override
	public int defaultY(Minecraft client, int screenWidth, int screenHeight) {
		Minecraft mc = client == null ? Minecraft.getInstance() : client;
		return Math.max(0, screenHeight - DEFAULT_Y_OFFSET);
	}

	@Override
	public int width(Minecraft client) {
		if (!ActionbarMoveState.isEnabled()) {
			return 0;
		}
		Minecraft mc = client == null ? Minecraft.getInstance() : client;
		return mc.font.width(PREVIEW);
	}

	@Override
	public int height(Minecraft client) {
		if (!ActionbarMoveState.isEnabled()) {
			return 0;
		}
		Minecraft mc = client == null ? Minecraft.getInstance() : client;
		return mc.font.lineHeight;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, int x, int y, float delta, boolean editing) {
		if (!ActionbarMoveState.isEnabled()) {
			return;
		}
		Minecraft mc = client == null ? Minecraft.getInstance() : client;
		Font font = mc.font;
		int w = font.width(PREVIEW);
		graphics.drawStringWithBackdrop(font, PREVIEW, x, y - DEFAULT_TEXT_Y_OFFSET, w, 0xFFFFFFFF);
	}
}

