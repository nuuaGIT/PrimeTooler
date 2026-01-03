package de.nuua.primetooler.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * WHY: Remove the "Java Edition" subtitle from the title logo.
 * PERF: O(1), skips a single blit.
 * ALT: No dedicated hook to toggle the edition texture.
 */
@Mixin(LogoRenderer.class)
public class LogoRendererMixin {
	@Shadow
	@Final
	private static ResourceLocation MINECRAFT_EDITION;

	@Redirect(
		method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IFI)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIIII)V"
		)
	)
	private void primetooler$skipEditionBlit(GuiGraphics graphics, RenderPipeline pipeline, ResourceLocation texture,
		int x, int y, float u, float v, int width, int height, int texWidth, int texHeight, int color) {
		if (texture != MINECRAFT_EDITION) {
			graphics.blit(pipeline, texture, x, y, u, v, width, height, texWidth, texHeight, color);
		}
	}
}
