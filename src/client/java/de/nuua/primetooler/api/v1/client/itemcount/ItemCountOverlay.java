package de.nuua.primetooler.api.v1.client.itemcount;

/**
 * WHY: Describe a custom item-count overlay render request.
 * PERF: Small immutable data holder; used only during item decoration rendering.
 */
public record ItemCountOverlay(String text, int argb, float scale, float nudgeY) {
	public ItemCountOverlay {
		if (text == null) {
			text = "";
		}
	}
}

