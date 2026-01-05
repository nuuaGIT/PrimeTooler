package de.nuua.primetooler.api.v1.client.itemcount;

import net.minecraft.world.item.ItemStack;

/**
 * WHY: Allow features to provide custom item-count overlays (e.g. terminal stacks, special markers).
 * PERF: Called during GUI item decoration rendering; keep logic branchy and allocation-free.
 */
public interface ItemCountOverlayProvider {
	/**
	 * @param stack the item being rendered
	 * @param vanillaCountText the original countText argument (may be null)
	 * @return overlay request or null to fall back to vanilla
	 */
	ItemCountOverlay provide(ItemStack stack, String vanillaCountText);
}

