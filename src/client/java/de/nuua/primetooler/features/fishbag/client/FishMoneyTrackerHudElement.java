package de.nuua.primetooler.features.fishbag.client;

import de.nuua.primetooler.api.v1.client.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WHY: Display coins/hour derived from fishbag coin increases.
 * PERF: Rate is updated on the scan interval (text refreshed every 3 seconds).
 */
public final class FishMoneyTrackerHudElement implements HudElement {
	public static final String ID = "fish_money_tracker";

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
		return 4 + line * 3;
	}

	@Override
	public int width(Minecraft client) {
		if (!FishbagTotalState.isMoneyTrackerEnabled() || client == null || client.font == null) {
			return 0;
		}
		String text = FishbagTotalState.moneyTrackerText();
		return client.font.width(text == null ? "" : text);
	}

	@Override
	public int height(Minecraft client) {
		if (!FishbagTotalState.isMoneyTrackerEnabled() || client == null || client.font == null) {
			return 0;
		}
		return client.font.lineHeight;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, int x, int y, float delta, boolean editing) {
		if (graphics == null || client == null || client.font == null) {
			return;
		}
		if (!editing && (!FishbagTotalState.isMoneyTrackerEnabled() || !FishbagTotalState.hasBags()
			|| !FishbagTotalState.shouldRenderMoneyTrackerHud())) {
			return;
		}
		String text = FishbagTotalState.moneyTrackerText();
		if (text == null || text.isEmpty()) {
			return;
		}
		graphics.drawString(client.font, text, x, y, 0xFFFFFFFF, true);
	}
}
