package de.nuua.primetooler.features.fishbag.client;

import de.nuua.primetooler.api.v1.client.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WHY: Display total fishbag capacity (sum of "current/max" across all bags) on the HUD.
 * PERF: Text is recalculated only when the inventory changes.
 */
public final class FishbagTotalHudElement implements HudElement {
	public static final String ID = "fishbag_total";

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
		return 4;
	}

	@Override
	public int width(Minecraft client) {
		if (!FishbagTotalState.isTotalEnabled() || client == null || client.font == null) {
			return 0;
		}
		String text = FishbagTotalState.totalText();
		return client.font.width(text == null ? "" : text);
	}

	@Override
	public int height(Minecraft client) {
		if (!FishbagTotalState.isTotalEnabled() || client == null || client.font == null) {
			return 0;
		}
		return client.font.lineHeight;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, int x, int y, float delta, boolean editing) {
		if (graphics == null || client == null || client.font == null) {
			return;
		}
		boolean hasBags = FishbagTotalState.hasBags();
		if (!editing && (!FishbagTotalState.isTotalEnabled() || !hasBags)) {
			return;
		}
		String text = FishbagTotalState.totalText();
		if (text == null || text.isEmpty()) {
			return;
		}
		graphics.drawString(client.font, text, x, y, 0xFFFFFFFF, true);
	}
}
