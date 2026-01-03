package de.nuua.primetooler.mixin.client;

import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractWidget.class)
public interface AbstractWidgetAccessor {
	@Accessor("x")
	int primetooler$getX();

	@Accessor("y")
	int primetooler$getY();

	@Accessor("y")
	void primetooler$setY(int y);

	@Accessor("width")
	int primetooler$getWidth();

	@Accessor("height")
	int primetooler$getHeight();
}
