package de.nuua.primetooler.features.jobtracker.client;

import de.nuua.primetooler.api.v1.client.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WHY: Display job XP/hour derived from the actionbar Jobs display.
 * PERF: Text updates on the poll interval (and is rate-limited to every 3 seconds).
 */
public final class JobXpTrackerHudElement implements HudElement {
	public static final String ID = "job_xp_tracker";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public int defaultX(Minecraft client, int screenWidth, int screenHeight) {
		return 4;
	}

	@Override
	public int defaultY(Minecraft client, int screenWidth, int screenHeight) {
		int line = client == null ? 10 : (client.font.lineHeight + 2);
		return 4 + line * 5;
	}

	@Override
	public int width(Minecraft client) {
		if (!JobTrackerState.isXpEnabled() || client == null || client.font == null) {
			return 0;
		}
		String text = JobTrackerState.xpText();
		return client.font.width(text == null ? "" : text);
	}

	@Override
	public int height(Minecraft client) {
		if (!JobTrackerState.isXpEnabled() || client == null || client.font == null) {
			return 0;
		}
		return client.font.lineHeight;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, int x, int y, float delta, boolean editing) {
		if (graphics == null || client == null || client.font == null) {
			return;
		}
		if (!editing && !JobTrackerState.isXpEnabled()) {
			return;
		}
		if (!editing && !JobTrackerState.shouldRenderXpHud()) {
			return;
		}
		String text = JobTrackerState.xpText();
		if (text == null || text.isEmpty()) {
			return;
		}
		graphics.drawString(client.font, text, x, y, 0xFFFFFFFF, true);
	}
}
