package de.nuua.primetooler.features.playermark.client;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * WHY: Central list of special player UUIDs for client-only name markers.
 * PERF: Small linear scan, prebuilt prefix components.
 */
public final class PlayerMarkRegistry {
	public record Member(String name, UUID uuid) {
	}

	private static final Member[] ADMIN_PLAYERS = new Member[] {
		new Member("Nuua", UUID.fromString("0b437436-3ef4-4c65-9f32-8811f0c09004"))
	};
	private static final Member[] SPECIAL_PLAYERS = new Member[] {
		new Member("Nuua", UUID.fromString("0b437436-3ef4-4c65-9f32-8811f0c09004")),
		new Member("OSCAVI", UUID.fromString("13233136-498e-4881-960c-4b0e332f463e")),
		new Member("1Reflexx", UUID.fromString("1d6e086a-41f7-4ced-b8dc-12b763e9843f")),
		new Member("Schwammig", UUID.fromString("4917ca1b-3098-428a-a962-9d4c01ca822f")),
		new Member("EpicBuilderHD", UUID.fromString("3cfd67ea-df0e-465d-acc0-137373329a3a")),
		new Member("Flow_737", UUID.fromString("303ffd76-6351-41a7-822b-9da40da1352b"))
	};
	private static final Component STAR_PREFIX =
		Component.literal("★").withStyle(ChatFormatting.RED);
	private static final float RAINBOW_SPEED = 0.25f;
	private static final float RAINBOW_SATURATION = 0.66f;
	private static final float RAINBOW_BRIGHTNESS = 1.0f;
	private static final float RAINBOW_ALPHA = 1.0f;
	private static final float RAINBOW_WAVELENGTH = 180.0f;
	private static final int[] RAINBOW_HEX = new int[] {
		0xFFFF4D4D,
		0xFFFFA64D,
		0xFFFFFF4D,
		0xFF4DFF4D,
		0xFF4DD2FF,
		0xFF4D4DFF,
		0xFFFF4DFF
	};

	private PlayerMarkRegistry() {
	}

	public static Component decorateMarkedName(UUID uuid, Component baseName, String rawName,
		float timeSeconds, Font font) {
		if (uuid == null || baseName == null || rawName == null || rawName.isEmpty()) {
			return baseName;
		}
		if (shouldStripClanTag()) {
			baseName = stripClanTagSuffix(baseName);
		}
		if (!SpecialNamesState.isEnabled()) {
			return baseName;
		}
		boolean admin = isAdmin(uuid);
		boolean special = isSpecial(uuid);
		if (!admin && !special) {
			return baseName;
		}
		if (special) {
			Component rainbow = rainbowComponent(rawName, timeSeconds, font);
			baseName = replaceNameToken(baseName, rawName, rainbow);
		}
		if (admin) {
			return Component.empty().append(STAR_PREFIX).append(baseName);
		}
		return baseName;
	}

	private static boolean shouldStripClanTag() {
		if (!ClanTagState.isEnabled()) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null) {
			return false;
		}
		return !client.hasSingleplayerServer();
	}

	private static Component stripClanTagSuffix(Component baseName) {
		String full = baseName.getString();
		if (full == null || full.isEmpty()) {
			return baseName;
		}
		int end = full.length() - 1;
		if (end < 1 || full.charAt(end) != ']') {
			return baseName;
		}
		int open = full.lastIndexOf('[', end);
		if (open < 0) {
			return baseName;
		}
		int cut = open;
		if (open > 0 && full.charAt(open - 1) == ' ') {
			cut = open - 1;
		}
		if (cut <= 0) {
			return baseName;
		}
		return truncateComponent(baseName, cut);
	}

	private static Component truncateComponent(Component baseName, int maxChars) {
		MutableComponent out = Component.empty();
		int[] remaining = new int[] { maxChars };
		baseName.visit((style, text) -> {
			if (remaining[0] <= 0) {
				return java.util.Optional.empty();
			}
			int take = text.length();
			if (take > remaining[0]) {
				take = remaining[0];
			}
			if (take > 0) {
				out.append(Component.literal(text.substring(0, take)).setStyle(style));
				remaining[0] -= take;
			}
			return java.util.Optional.empty();
		}, Style.EMPTY);
		return out;
	}

	private static Component replaceNameToken(Component baseName, String rawName, Component replacement) {
		if (rawName == null || rawName.isEmpty() || replacement == null) {
			return baseName;
		}
		MutableComponent out = Component.empty();
		baseName.visit((style, text) -> {
			int index = 0;
			int hit = text.indexOf(rawName, index);
			while (hit >= 0) {
				if (hit > index) {
					out.append(Component.literal(text.substring(index, hit)).setStyle(style));
				}
				out.append(replacement);
				index = hit + rawName.length();
				hit = text.indexOf(rawName, index);
			}
			if (index < text.length()) {
				out.append(Component.literal(text.substring(index)).setStyle(style));
			}
			return java.util.Optional.empty();
		}, Style.EMPTY);
		return out;
	}

	public static Member[] adminMembers() {
		return ADMIN_PLAYERS.clone();
	}

	public static Member[] specialMembers() {
		int count = 0;
		for (int i = 0; i < SPECIAL_PLAYERS.length; i++) {
			if (!isAdmin(SPECIAL_PLAYERS[i].uuid)) {
				count++;
			}
		}
		if (count == SPECIAL_PLAYERS.length) {
			return SPECIAL_PLAYERS.clone();
		}
		Member[] filtered = new Member[count];
		int cursor = 0;
		for (int i = 0; i < SPECIAL_PLAYERS.length; i++) {
			Member member = SPECIAL_PLAYERS[i];
			if (!isAdmin(member.uuid)) {
				filtered[cursor++] = member;
			}
		}
		return filtered;
	}

	public static Component rainbowName(String name, float timeSeconds, Font font) {
		return rainbowComponent(name, timeSeconds, font);
	}

	public static boolean isAuthorizedUser() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getUser() == null) {
			return false;
		}
		UUID uuid = client.getUser().getProfileId();
		return uuid != null && (isAdmin(uuid) || isSpecial(uuid));
	}

	private static boolean isAdmin(UUID uuid) {
		for (int i = 0; i < ADMIN_PLAYERS.length; i++) {
			if (ADMIN_PLAYERS[i].uuid.equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isSpecial(UUID uuid) {
		for (int i = 0; i < SPECIAL_PLAYERS.length; i++) {
			if (SPECIAL_PLAYERS[i].uuid.equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	private static Component rainbowComponent(String text, float timeSeconds, Font font) {
		if (text == null || text.isEmpty()) {
			return Component.empty();
		}
		if (font == null) {
			font = Minecraft.getInstance().font;
		}
		MutableComponent out = Component.empty();
		float timePhase = timeSeconds * RAINBOW_SPEED;
		int cursorX = 0;
		for (int i = 0; i < text.length(); i++) {
			String glyph = String.valueOf(text.charAt(i));
			int charWidth = font.width(glyph);
			float centerX = cursorX + (charWidth * 0.5f);
			float hue = positiveMod1(1.0f - ((centerX / RAINBOW_WAVELENGTH) + timePhase));
			int color = applySaturationBrightness(colorFromPalette(hue), RAINBOW_SATURATION,
				RAINBOW_BRIGHTNESS, RAINBOW_ALPHA);
			out.append(Component.literal(glyph).setStyle(Style.EMPTY.withColor(color)));
			cursorX += charWidth;
		}
		return out;
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

	private static float clamp01(float value) {
		if (value < 0.0f) {
			return 0.0f;
		}
		if (value > 1.0f) {
			return 1.0f;
		}
		return value;
	}
}
