package de.nuua.primetooler.features.durabilityguard.client;

import de.nuua.primetooler.api.v1.client.hud.HudElement;
import de.nuua.primetooler.core.Messages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WHY: Expose the low-armor-durability warning as a movable HUD element.
 * PERF: Simple text render; width/height are derived from the current font.
 */
public final class ArmorDurabilityWarningHudElement implements HudElement {
	public static final String ID = "primetooler:armor_durability_warning";

	private static final int HOTBAR_HEIGHT = 22;
	private static final int HOTBAR_OFFSET_Y = 22;
	private static final int HOTBAR_HALF_WIDTH = 91;
	private static final int TEXT_OFFSET_X = 8;
	private static final int COLOR_WARNING = 0xFFFF5555;

	@Override
	public String id() {
		return ID;
	}

	@Override
	public int defaultX(Minecraft client, int screenWidth, int screenHeight) {
		return screenWidth / 2 + HOTBAR_HALF_WIDTH + TEXT_OFFSET_X;
	}

	@Override
	public int defaultY(Minecraft client, int screenWidth, int screenHeight) {
		Font font = client == null ? Minecraft.getInstance().font : client.font;
		return screenHeight - HOTBAR_OFFSET_Y + (HOTBAR_HEIGHT - font.lineHeight) / 2;
	}

	@Override
	public int width(Minecraft client) {
		Minecraft mc = client == null ? Minecraft.getInstance() : client;
		Font font = mc.font;
		return font.width(text());
	}

	@Override
	public int height(Minecraft client) {
		Minecraft mc = client == null ? Minecraft.getInstance() : client;
		return mc.font.lineHeight;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, int x, int y, float delta, boolean editing) {
		Minecraft mc = client == null ? Minecraft.getInstance() : client;
		graphics.drawString(mc.font, text(), x, y, COLOR_WARNING, true);
	}

	private static String text() {
		return Messages.applyColorCodes(Messages.get(Messages.Id.ARMOR_DURABILITY_LOW));
	}
}

