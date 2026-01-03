package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import de.nuua.primetooler.features.doubledrop.client.DoubleDropState;
import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.platform.sound.SoundPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
			if (!DoubleDropState.allowDrop(player.getMainHandItem())) {
				playDropBlockedFeedback(player);
				cir.setReturnValue(false);
			}
			return;
		}
		if (CheckItemClientModule.isSelectedHotbarSlotSaved(player)) {
			cir.setReturnValue(false);
			return;
		}
		ItemStack stack = player.getMainHandItem();
		if (!DoubleDropState.allowDrop(stack)) {
			playDropBlockedFeedback(player);
			cir.setReturnValue(false);
		}
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
