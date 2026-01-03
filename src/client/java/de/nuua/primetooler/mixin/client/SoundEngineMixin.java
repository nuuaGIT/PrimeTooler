package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.sound.client.BeaconSoundState;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Mute beacon event sounds on multiplayer when enabled.
 * PERF: One ID compare per sound play.
 * ALT: No stable Fabric hook to filter specific sound events.
 */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@Inject(method = "play", at = @At("HEAD"), cancellable = true)
	private void primetooler$muteBeacon(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
		if (!BeaconSoundState.isEnabled() || sound == null) {
			return;
		}
		ResourceLocation id = sound.getLocation();
		if (id == null) {
			return;
		}
		if (id.equals(SoundEvents.BEACON_ACTIVATE.location())) {
			cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
		}
	}
}
