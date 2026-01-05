package de.nuua.primetooler.features.terminalstackcount.client;

import de.nuua.primetooler.api.v1.client.itemcount.ItemCountOverlay;
import de.nuua.primetooler.api.v1.client.itemcount.ItemCountOverlayProvider;
import net.minecraft.world.item.ItemStack;

/**
 * WHY: Render terminal-style counts from the item name as the stack overlay count.
 * PERF: Single name scan only when item decorations are rendered.
 */
public final class TerminalStackCountOverlayProvider implements ItemCountOverlayProvider {
	@Override
	public ItemCountOverlay provide(ItemStack stack, String vanillaCountText) {
		if (!TerminalStackCountState.isEnabled()) {
			return null;
		}
		String text = TerminalCountOverlayText.fromStack(stack);
		if (text == null || text.isEmpty()) {
			return null;
		}
		float scale = TerminalStackCountState.OVERLAY_SCALE;
		if (!Float.isFinite(scale) || scale <= 0.0f) {
			scale = 0.66f;
		}
		return new ItemCountOverlay(
			text,
			TerminalStackCountState.OVERLAY_COLOR,
			scale,
			TerminalStackCountState.OVERLAY_NUDGE_Y
		);
	}
}

