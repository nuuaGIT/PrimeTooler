package de.nuua.primetooler.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * WHY: Read current actionbar overlay message for client-side trackers.
 * PERF: Accessor only; no reflection or allocations.
 */
@Mixin(Gui.class)
public interface GuiOverlayMessageAccessor {
	@Accessor("overlayMessageString")
	Component primetooler$getOverlayMessage();
}

