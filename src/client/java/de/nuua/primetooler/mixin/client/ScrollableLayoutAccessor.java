package de.nuua.primetooler.mixin.client;

import net.minecraft.client.gui.components.ScrollableLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScrollableLayout.class)
public interface ScrollableLayoutAccessor {
	@Accessor("container")
	Object primetooler$getContainer();
}
