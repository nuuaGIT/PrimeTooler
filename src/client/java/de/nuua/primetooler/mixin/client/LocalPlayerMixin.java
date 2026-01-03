package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Prevent dropping items from client-saved hotbar slots.
 * PERF: Single check on drop, no allocations.
 * ALT: Server-side enforcement not possible in client-only scope.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockSavedItemDrop(boolean dropAll, CallbackInfoReturnable<Boolean> cir) {
		Player player = (LocalPlayer) (Object) this;
		if (!CheckItemClientModule.isSlotLockingEnabled()) {
			return;
		}
		if (CheckItemClientModule.isSelectedHotbarSlotSaved(player)) {
			cir.setReturnValue(false);
		}
	}
}
