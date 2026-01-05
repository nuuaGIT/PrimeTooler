package de.nuua.primetooler.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * WHY: Read current title/subtitle text for debug tooling.
 * PERF: Accessor only; no per-tick reflection.
 */
@Mixin(Gui.class)
public interface GuiTitleAccessor {
	@Accessor("title")
	Component primetooler$getTitle();

	@Accessor("subtitle")
	Component primetooler$getSubtitle();
}

