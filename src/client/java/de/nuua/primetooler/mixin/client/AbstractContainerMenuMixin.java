package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import de.nuua.primetooler.features.doubledrop.client.DoubleDropState;
import de.nuua.primetooler.platform.sound.SoundPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Ensure items in saved slots cannot be moved via any container click path.
 * PERF: O(1) checks per click, no allocations.
 * ALT: Server-side enforcement not possible in client-only scope.
 */
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	@Inject(
		method = "doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void primetooler$blockSavedItemDrops(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		if (CheckItemClientModule.isSlotLockingEnabled() && slotId >= 0) {
			Slot slot = menu.getSlot(slotId);
			if (slot != null && CheckItemClientModule.isSlotSaved(player, slot)) {
				ci.cancel();
				return;
			}
		}
		if (isDropClick(clickType, slotId, menu)) {
			ItemStack stack = resolveDropStack(menu, slotId);
			if (!DoubleDropState.allowDrop(stack)) {
				playDropBlockedFeedback(player);
				ci.cancel();
				return;
			}
		}
	}

	private static boolean isDropClick(ClickType clickType, int slotId, AbstractContainerMenu menu) {
		if (clickType == ClickType.THROW) {
			return true;
		}
		if (clickType == ClickType.PICKUP && slotId < 0) {
			return menu != null && !menu.getCarried().isEmpty();
		}
		return false;
	}

	private static ItemStack resolveDropStack(AbstractContainerMenu menu, int slotId) {
		if (menu == null) {
			return ItemStack.EMPTY;
		}
		if (slotId >= 0) {
			Slot slot = menu.getSlot(slotId);
			return slot == null ? ItemStack.EMPTY : slot.getItem();
		}
		return menu.getCarried();
	}

	private static void playDropBlockedFeedback(Player player) {
		float pitch = DoubleDropState.blockedPitch();
		SoundPlayer.playWarning(SoundEvents.ANVIL_LAND, 0.5f, pitch);
		if (player != null && DoubleDropState.isFirstAttempt()) {
			player.displayClientMessage(
				Component.literal(Messages.applyColorCodes(Messages.get(Messages.Id.DOUBLE_DROP_CONFIRM))),
				false
			);
		}
	}

}
