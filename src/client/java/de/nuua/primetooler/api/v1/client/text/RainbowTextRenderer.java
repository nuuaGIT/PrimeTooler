package de.nuua.primetooler.api.v1.client.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * WHY: Provide a small, reusable API for animated rainbow text.
 * PERF: O(n) over characters, avoids per-frame allocations for ASCII.
 * ALT: No vanilla helper for per-character hue animation.
 */
public final class RainbowTextRenderer {
	private static final int ASCII_LIMIT = 128;
	private static final String[] ASCII_CHARS = buildAsciiCache();
	private static final int[] RAINBOW_HEX = new int[] {
		0xFFFF4D4D, // red
		0xFFFFA64D, // orange
		0xFFFFFF4D, // yellow
		0xFF4DFF4D, // green
		0xFF4DD2FF, // cyan
		0xFF4D4DFF, // blue
		0xFFFF4DFF  // magenta
	};

	private RainbowTextRenderer() {
	}

	public static int draw(GuiGraphics graphics, Font font, String text, int x, int y,
		float timeSeconds, RainbowTextStyle style) {
		if (text == null || text.isEmpty()) {
			return x;
		}

		float speed = style.speed();
		float saturation = clamp01(style.saturation());
		float brightness = clamp01(style.brightness());
		float alpha = clamp01(style.alpha());
		float wavelength = style.wavelengthPx() <= 0.0f ? 1.0f : style.wavelengthPx();
		boolean shadow = style.shadow();

		int cursorX = x;
		float timePhase = timeSeconds * speed;
		int length = text.length();

		for (int i = 0; i < length; i++) {
			char ch = text.charAt(i);
			String glyph = toCachedString(ch);
			int charWidth = font.width(glyph);
			float centerX = cursorX + (charWidth * 0.5f);

			float hue = positiveMod1(1.0f - ((centerX / wavelength) + timePhase));
			int color = applySaturationBrightness(colorFromPalette(hue), saturation, brightness, alpha);

			graphics.drawString(font, glyph, cursorX, y, color, shadow);
			cursorX += charWidth;
		}

		return cursorX;
	}

	private static String toCachedString(char ch) {
		if (ch < ASCII_LIMIT) {
			return ASCII_CHARS[ch];
		}
		return String.valueOf(ch);
	}

	private static String[] buildAsciiCache() {
		String[] cache = new String[ASCII_LIMIT];
		for (int i = 0; i < ASCII_LIMIT; i++) {
			cache[i] = String.valueOf((char) i);
		}
		return cache;
	}

	private static float clamp01(float value) {
		if (value < 0.0f) {
			return 0.0f;
		}
		if (value > 1.0f) {
			return 1.0f;
		}
		return value;
	}

	private static float positiveMod1(float value) {
		float mod = value - (float) Math.floor(value);
		if (mod < 0.0f) {
			mod += 1.0f;
		}
		return mod;
	}

	private static int colorFromPalette(float hue) {
		int count = RAINBOW_HEX.length;
		float scaled = hue * count;
		int idx = (int) scaled;
		float t = scaled - idx;
		int c0 = RAINBOW_HEX[idx % count];
		int c1 = RAINBOW_HEX[(idx + 1) % count];
		return lerpColor(c0, c1, t);
	}

	private static int lerpColor(int c0, int c1, float t) {
		int r0 = (c0 >> 16) & 0xFF;
		int g0 = (c0 >> 8) & 0xFF;
		int b0 = c0 & 0xFF;
		int r1 = (c1 >> 16) & 0xFF;
		int g1 = (c1 >> 8) & 0xFF;
		int b1 = c1 & 0xFF;

		int r = (int) (r0 + (r1 - r0) * t);
		int g = (int) (g0 + (g1 - g0) * t);
		int b = (int) (b0 + (b1 - b0) * t);
		return (0xFF << 24) | (r << 16) | (g << 8) | b;
	}

	private static int applySaturationBrightness(int color, float saturation, float brightness, float alpha) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		float rf = r / 255.0f;
		float gf = g / 255.0f;
		float bf = b / 255.0f;
		float gray = (rf + gf + bf) / 3.0f;

		rf = gray + (rf - gray) * saturation;
		gf = gray + (gf - gray) * saturation;
		bf = gray + (bf - gray) * saturation;

		rf *= brightness;
		gf *= brightness;
		bf *= brightness;

		int ai = (int) (alpha * 255.0f) & 0xFF;
		int ri = (int) (clamp01(rf) * 255.0f) & 0xFF;
		int gi = (int) (clamp01(gf) * 255.0f) & 0xFF;
		int bi = (int) (clamp01(bf) * 255.0f) & 0xFF;

		return (ai << 24) | (ri << 16) | (gi << 8) | bi;
	}
}
