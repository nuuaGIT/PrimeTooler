package de.nuua.primetooler.api.v1.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WHY: Stable contract for movable HUD elements used by the HUD editor and in-game rendering.
 * PERF: Width/height are queried only when rendering or editing; no tick registration required.
 */
public interface HudElement {
	String id();

	int defaultX(Minecraft client, int screenWidth, int screenHeight);

	int defaultY(Minecraft client, int screenWidth, int screenHeight);

	int width(Minecraft client);

	int height(Minecraft client);

	void render(GuiGraphics graphics, Minecraft client, int x, int y, float delta, boolean editing);
}
