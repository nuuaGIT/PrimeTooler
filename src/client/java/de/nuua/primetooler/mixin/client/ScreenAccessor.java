package de.nuua.primetooler.mixin.client;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
	@Accessor("renderables")
	List<Renderable> primetooler$renderables();

	@Accessor("children")
	List<GuiEventListener> primetooler$children();

	@Accessor("narratables")
	List<NarratableEntry> primetooler$narratables();

	@Accessor("width")
	int primetooler$getWidth();

	@Accessor("height")
	int primetooler$getHeight();

	@Invoker("renderBlurredBackground")
	void primetooler$renderBlurredBackground(GuiGraphics graphics);

	@Invoker("addRenderableWidget")
	<T extends GuiEventListener & Renderable & NarratableEntry> T primetooler$addRenderableWidget(T widget);

	@Accessor("font")
	Font primetooler$getFont();
}
