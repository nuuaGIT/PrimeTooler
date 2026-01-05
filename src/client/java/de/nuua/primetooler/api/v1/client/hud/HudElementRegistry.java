package de.nuua.primetooler.api.v1.client.hud;

/**
 * WHY: Central registration point for HUD elements that can be positioned by the user.
 * PERF: Used by UI/editor screens only; no hotpath work.
 */
public final class HudElementRegistry {
	private static final java.util.ArrayList<HudElement> ELEMENTS = new java.util.ArrayList<>();

	private HudElementRegistry() {
	}

	public static void register(HudElement element) {
		if (element == null) {
			return;
		}
		String id = element.id();
		if (id == null || id.isEmpty()) {
			return;
		}
		for (int i = 0; i < ELEMENTS.size(); i++) {
			HudElement existing = ELEMENTS.get(i);
			if (existing != null && id.equals(existing.id())) {
				ELEMENTS.set(i, element);
				return;
			}
		}
		ELEMENTS.add(element);
	}

	public static HudElement[] elements() {
		return ELEMENTS.toArray(new HudElement[0]);
	}
}

