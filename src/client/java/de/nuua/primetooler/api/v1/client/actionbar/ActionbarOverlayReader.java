package de.nuua.primetooler.api.v1.client.actionbar;

import de.nuua.primetooler.mixin.client.GuiOverlayMessageAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;

/**
 * WHY: Stable client API to read the current actionbar overlay message.
 * PERF: Single field access via mixin accessor.
 */
public final class ActionbarOverlayReader {
	private ActionbarOverlayReader() {
	}

	public static Component getOverlayMessage(Minecraft client) {
		if (client == null) {
			return null;
		}
		Gui gui = client.gui;
		if (gui == null) {
			return null;
		}
		if (gui instanceof GuiOverlayMessageAccessor accessor) {
			return accessor.primetooler$getOverlayMessage();
		}
		return null;
	}
}

