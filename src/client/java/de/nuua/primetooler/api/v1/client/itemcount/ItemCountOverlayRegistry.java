package de.nuua.primetooler.api.v1.client.itemcount;

import net.minecraft.world.item.ItemStack;

/**
 * WHY: Central registry for item-count overlay providers.
 * PERF: Volatile array snapshot; iteration is allocation-free.
 */
public final class ItemCountOverlayRegistry {
	private static final ItemCountOverlayProvider[] EMPTY = new ItemCountOverlayProvider[0];
	private static volatile ItemCountOverlayProvider[] providers = EMPTY;

	private ItemCountOverlayRegistry() {
	}

	public static void register(ItemCountOverlayProvider provider) {
		if (provider == null) {
			return;
		}
		synchronized (ItemCountOverlayRegistry.class) {
			ItemCountOverlayProvider[] current = providers;
			int len = current.length;
			ItemCountOverlayProvider[] next = new ItemCountOverlayProvider[len + 1];
			System.arraycopy(current, 0, next, 0, len);
			next[len] = provider;
			providers = next;
		}
	}

	public static ItemCountOverlay resolve(ItemStack stack, String vanillaCountText) {
		if (stack == null) {
			return null;
		}
		ItemCountOverlayProvider[] current = providers;
		for (int i = 0; i < current.length; i++) {
			ItemCountOverlay overlay = current[i].provide(stack, vanillaCountText);
			if (overlay != null && overlay.text() != null && !overlay.text().isEmpty()) {
				return overlay;
			}
		}
		return null;
	}
}

