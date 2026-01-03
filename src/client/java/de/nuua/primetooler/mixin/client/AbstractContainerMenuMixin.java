package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
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
		if (!CheckItemClientModule.isSlotLockingEnabled()) {
			return;
		}
		AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
		if (slotId >= 0) {
			Slot slot = menu.getSlot(slotId);
			if (slot != null && CheckItemClientModule.isSlotSaved(player, slot)) {
				ci.cancel();
			}
		}
	}
}
