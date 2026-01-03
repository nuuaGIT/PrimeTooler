package de.nuua.primetooler.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.components.ScrollableLayout$Container")
public interface ScrollableLayoutContainerAccessor {
	@Invoker("setScrollAmount")
	void primetooler$setScrollAmount(double value);
}
