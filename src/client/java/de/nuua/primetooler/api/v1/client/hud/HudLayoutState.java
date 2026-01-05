package de.nuua.primetooler.api.v1.client.hud;

import de.nuua.primetooler.core.config.HudLayoutConfig;

/**
 * WHY: Single source of truth for HUD element positions (normalized 0..1).
 * PERF: O(1) lookup per element render; no allocations in steady state.
 */
public final class HudLayoutState {
	public record NormalizedPos(float x, float y) {
	}

	private static final java.util.HashMap<String, NormalizedPos> POSITIONS = new java.util.HashMap<>();
	private static final java.util.HashMap<String, StablePixelX> STABLE_PIXEL_X = new java.util.HashMap<>();

	private HudLayoutState() {
	}

	public static void load(HudLayoutConfig config) {
		POSITIONS.clear();
		STABLE_PIXEL_X.clear();
		if (config == null || config.entries == null) {
			return;
		}
		for (int i = 0; i < config.entries.size(); i++) {
			HudLayoutConfig.Entry entry = config.entries.get(i);
			if (entry == null || entry.id == null || entry.id.isEmpty()) {
				continue;
			}
			float x = clamp01(entry.x);
			float y = clamp01(entry.y);
			POSITIONS.put(entry.id, new NormalizedPos(x, y));
		}
	}

	public static NormalizedPos get(String id) {
		if (id == null || id.isEmpty()) {
			return null;
		}
		return POSITIONS.get(id);
	}

	public static HudLayoutConfig snapshot() {
		java.util.ArrayList<HudLayoutConfig.Entry> entries = new java.util.ArrayList<>(POSITIONS.size());
		for (java.util.Map.Entry<String, NormalizedPos> e : POSITIONS.entrySet()) {
			NormalizedPos pos = e.getValue();
			if (pos == null) {
				continue;
			}
			entries.add(new HudLayoutConfig.Entry(e.getKey(), pos.x(), pos.y()));
		}
		return new HudLayoutConfig(entries);
	}

	public static int resolveX(String id, int screenWidth, int elementWidth, int defaultPixelX) {
		int max = Math.max(0, screenWidth - Math.max(0, elementWidth));
		if (max <= 0) {
			return 0;
		}
		float norm = defaultNormalized(defaultPixelX, max);
		NormalizedPos stored = get(id);
		if (stored != null) {
			norm = stored.x();
		}
		return (int) (clamp01(norm) * (float) max + 0.5f);
	}

	public static int resolveXStableLeft(String id, int screenWidth, int elementWidth, int defaultPixelX) {
		int max = Math.max(0, screenWidth - Math.max(0, elementWidth));
		if (max <= 0) {
			return 0;
		}

		float norm = defaultNormalized(defaultPixelX, max);
		NormalizedPos stored = get(id);
		if (stored != null) {
			norm = stored.x();
		}
		int baseX = (int) (clamp01(norm) * (float) max + 0.5f);

		// WHY: When text width changes (dynamic values), normalized positions cause the left edge to drift.
		// PERF: Cache is tiny (few HUD elements), O(1) lookups/updates.
		StablePixelX stable = STABLE_PIXEL_X.get(id);
		int normBits = Float.floatToIntBits(norm);
		if (stable == null) {
			stable = new StablePixelX(screenWidth, normBits, baseX);
			STABLE_PIXEL_X.put(id, stable);
		} else if (stable.screenWidth != screenWidth || stable.normBits != normBits) {
			stable.screenWidth = screenWidth;
			stable.normBits = normBits;
			stable.desiredPixelX = baseX;
		}

		int x = stable.desiredPixelX;
		if (x < 0) {
			return 0;
		}
		if (x > max) {
			return max;
		}
		return x;
	}

	public static int resolveY(String id, int screenHeight, int elementHeight, int defaultPixelY) {
		int max = Math.max(0, screenHeight - Math.max(0, elementHeight));
		if (max <= 0) {
			return 0;
		}
		float norm = defaultNormalized(defaultPixelY, max);
		NormalizedPos stored = get(id);
		if (stored != null) {
			norm = stored.y();
		}
		return (int) (clamp01(norm) * (float) max + 0.5f);
	}

	public static float defaultNormalized(int pixel, int maxPixel) {
		if (maxPixel <= 0) {
			return 0.0f;
		}
		return clamp01(pixel / (float) maxPixel);
	}

	public static float clamp01(float value) {
		if (value < 0.0f) {
			return 0.0f;
		}
		if (value > 1.0f) {
			return 1.0f;
		}
		return value;
	}

	private static final class StablePixelX {
		private int screenWidth;
		private int normBits;
		private int desiredPixelX;

		private StablePixelX(int screenWidth, int normBits, int desiredPixelX) {
			this.screenWidth = screenWidth;
			this.normBits = normBits;
			this.desiredPixelX = desiredPixelX;
		}
	}
}
