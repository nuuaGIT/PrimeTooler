package de.nuua.primetooler.features.fishbag.client;

import de.nuua.primetooler.api.v1.client.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WHY: Display total fishbag weight (sum of the 2nd lore line numbers) on the HUD.
 * PERF: Text is updated by the periodic inventory scan job.
 */
public final class FishbagWeightHudElement implements HudElement {
	public static final String ID = "fishbag_total_weight";

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
		return 4 + (client == null ? 10 : (client.font.lineHeight + 2));
	}

	@Override
	public int width(Minecraft client) {
		if (!FishbagTotalState.isWeightEnabled() || client == null || client.font == null) {
			return 0;
		}
		String text = FishbagTotalState.weightText();
		return client.font.width(text == null ? "" : text);
	}

	@Override
	public int height(Minecraft client) {
		if (!FishbagTotalState.isWeightEnabled() || client == null || client.font == null) {
			return 0;
		}
		return client.font.lineHeight;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, int x, int y, float delta, boolean editing) {
		if (graphics == null || client == null || client.font == null) {
			return;
		}
		if (!editing && (!FishbagTotalState.isWeightEnabled() || !FishbagTotalState.hasBags() || !FishbagTotalState.shouldRenderWeightHud())) {
			return;
		}
		String text = FishbagTotalState.weightText();
		if (text == null || text.isEmpty()) {
			return;
		}
		graphics.drawString(client.font, text, x, y, 0xFFFFFFFF, true);
	}
}
