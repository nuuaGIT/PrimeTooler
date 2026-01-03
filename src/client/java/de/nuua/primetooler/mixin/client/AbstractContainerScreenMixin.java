package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import de.nuua.primetooler.platform.input.PrimeToolerKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Prevent moving or throwing items from client-saved inventory slots.
 * PERF: O(1) checks per click, no allocations.
 * ALT: Server-side enforcement not possible in client-only scope.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
	@Shadow
	protected AbstractContainerMenu menu;

	@Shadow
	protected Slot lastClickSlot;

	@Shadow
	protected Slot hoveredSlot;

	@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

	@Shadow
	protected abstract boolean hasClickedOutside(double mouseX, double mouseY, int left, int top);

	@Shadow
	protected abstract void slotClicked(Slot slot, int slotId, int button, ClickType clickType);

	@Shadow
	protected abstract void clearDraggingState();

	@Unique
	private boolean primetooler$allowSavedSlotClick;

	@Unique
	private static final int SAVED_FRAME_SIZE = 16;
	@Unique
	private static final int SAVED_FRAME_COLOR = 0x66FF3333;
	@Unique
	private static final int SAVED_FRAME_THICKNESS = 1;

	@Inject(
		method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void primetooler$blockSavedItemMoves(Slot slot, int slotId, int button, ClickType clickType, CallbackInfo ci) {
		if (primetooler$allowSavedSlotClick) {
			return;
		}
		if (!CheckItemClientModule.isSlotLockingEnabled()) {
			return;
		}
		if (menu == null) {
			return;
		}
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if (slot != null && CheckItemClientModule.isSlotSaved(player, slot)) {
			ci.cancel();
			return;
		}
		if (slotId == -999 && lastClickSlot != null && CheckItemClientModule.isSlotSaved(player, lastClickSlot)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void primetooler$blockSavedItemDrop(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!CheckItemClientModule.isSlotLockingEnabled()) {
			return;
		}
		if (menu == null) {
			return;
		}
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if (lastClickSlot == null || !CheckItemClientModule.isSlotSaved(player, lastClickSlot)) {
			return;
		}
		if (!hasClickedOutside(event.x(), event.y(), leftPos, topPos)) {
			return;
		}
		if (lastClickSlot != null) {
			primetooler$allowSavedSlotClick = true;
			try {
				slotClicked(lastClickSlot, lastClickSlot.getContainerSlot(), 0, ClickType.PICKUP);
			} finally {
				primetooler$allowSavedSlotClick = false;
			}
		}
		clearDraggingState();
		cir.setReturnValue(true);
	}

	@Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
	private void primetooler$toggleSavedSlot(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!CheckItemClientModule.isSlotLockingEnabled()) {
			return;
		}
		if (!matchesSlotLockKey(event)) {
			return;
		}
		if (hoveredSlot == null) {
			return;
		}
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if (!CheckItemClientModule.toggleSlotSaved(player, hoveredSlot)) {
			return;
		}
		cir.setReturnValue(true);
	}

	private static boolean matchesSlotLockKey(KeyEvent event) {
		if (event == null) {
			return false;
		}
		var key = PrimeToolerKeyBindings.slotLockKey();
		if (key == null) {
			return false;
		}
		return key.matches(event);
	}

	@Inject(
		method = "renderSlot",
		at = @At("HEAD")
	)
	private void primetooler$renderSavedSlotOverlay(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
		if (!CheckItemClientModule.isSlotLockingEnabled()) {
			return;
		}
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if (!CheckItemClientModule.isSlotSaved(player, slot)) {
			return;
		}
		int x = slot.x;
		int y = slot.y;
		int size = SAVED_FRAME_SIZE;
		int t = SAVED_FRAME_THICKNESS;
		int c = SAVED_FRAME_COLOR;
		graphics.fill(x, y, x + size, y + t, c);
		graphics.fill(x, y + size - t, x + size, y + size, c);
		graphics.fill(x, y + t, x + t, y + size - t, c);
		graphics.fill(x + size - t, y + t, x + size, y + size - t, c);
	}
}
