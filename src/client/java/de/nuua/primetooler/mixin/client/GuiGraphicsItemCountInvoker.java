package de.nuua.primetooler.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * WHY: Allow delegating to vanilla item-count rendering when Terminal Count isn't applicable.
 * PERF: Direct call to vanilla method, no extra logic.
 */
@Mixin(GuiGraphics.class)
public interface GuiGraphicsItemCountInvoker {
	@Invoker("renderItemCount")
	void primetooler$invokeRenderItemCount(Font font, ItemStack stack, int x, int y, String countText);
}

