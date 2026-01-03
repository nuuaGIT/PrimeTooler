package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.durabilityguard.client.DurabilityGuardState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Prevent using or breaking with near-broken tools when the guard is enabled.
 * PERF: Single durability check per interaction call.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockStartDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		if (DurabilityGuardState.shouldBlock(player.getMainHandItem())) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockContinueDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		if (DurabilityGuardState.shouldBlock(player.getMainHandItem())) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		if (DurabilityGuardState.shouldBlock(player.getMainHandItem())) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockUseOn(
		LocalPlayer player,
		InteractionHand hand,
		BlockHitResult hitResult,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		if (player == null) {
			return;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (DurabilityGuardState.shouldBlock(stack)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (player == null) {
			return;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (DurabilityGuardState.shouldBlock(stack)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockInteract(Player player, Entity target, InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir) {
		if (player == null) {
			return;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (DurabilityGuardState.shouldBlock(stack)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockInteractAt(Player player, Entity target, net.minecraft.world.phys.EntityHitResult hit,
		InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (player == null) {
			return;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (DurabilityGuardState.shouldBlock(stack)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void primetooler$blockAttack(Player player, Entity target, CallbackInfo ci) {
		if (player == null) {
			return;
		}
		if (DurabilityGuardState.shouldBlock(player.getMainHandItem())
			|| DurabilityGuardState.shouldBlock(player.getOffhandItem())) {
			ci.cancel();
		}
	}
}
