package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import de.nuua.primetooler.features.durabilityguard.client.DurabilityGuardState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Visual warning for items blocked by durability guard.
 * PERF: Single conditional per item decoration render, no allocations.
 * ALT: No stable render hook for slot overlays without a mixin.
 */
@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
	private static final ResourceLocation BROKEN_ICON =
		ResourceLocation.withDefaultNamespace("textures/gui/sprites/world_list/error_highlighted.png");
	private static final int BROKEN_ICON_SIZE = 16;
	private static final int BROKEN_ICON_OFFSET = 16 - BROKEN_ICON_SIZE;
	private static final int BROKEN_ICON_NUDGE_X = 3;
	private static final int SAVED_FRAME_SIZE = 16;
	private static final int SAVED_FRAME_COLOR = 0x66FF3333;
	private static final int SAVED_FRAME_THICKNESS = 1;

	@Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
		at = @At("HEAD"))
	private void primetooler$renderSavedSlotFrame(Font font, ItemStack stack, int x, int y, String countText, CallbackInfo ci) {
		Player player = Minecraft.getInstance().player;
		boolean saved = CheckItemClientModule.isSlotLockingEnabled()
			&& player != null
			&& CheckItemClientModule.isStackInSavedSlot(player, stack);
		if (!saved) {
			return;
		}
		int size = SAVED_FRAME_SIZE;
		int t = SAVED_FRAME_THICKNESS;
		int c = SAVED_FRAME_COLOR;
		((GuiGraphics) (Object) this).fill(x, y, x + size, y + t, c);
		((GuiGraphics) (Object) this).fill(x, y + size - t, x + size, y + size, c);
		((GuiGraphics) (Object) this).fill(x, y + t, x + t, y + size - t, c);
		((GuiGraphics) (Object) this).fill(x + size - t, y + t, x + size, y + size - t, c);
	}

	@Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
		at = @At("TAIL"))
	private void primetooler$renderDurabilityGuardOverlay(Font font, ItemStack stack, int x, int y, String countText, CallbackInfo ci) {
		boolean broken = DurabilityGuardState.shouldBlock(stack);
		if (!broken) {
			return;
		}
		if (broken) {
			((GuiGraphics) (Object) this).blit(
				RenderPipelines.GUI_TEXTURED,
				BROKEN_ICON,
				x + BROKEN_ICON_OFFSET + BROKEN_ICON_NUDGE_X,
				y + BROKEN_ICON_OFFSET,
				0.0f,
				0.0f,
				BROKEN_ICON_SIZE,
				BROKEN_ICON_SIZE,
				BROKEN_ICON_SIZE,
				BROKEN_ICON_SIZE
			);
		}
	}
}
