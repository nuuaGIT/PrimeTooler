package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.sound.client.SoundMuteRegistry;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Zentraler Hook für clientseitige Sound-Mute-Regeln.
 * PERF: Kleiner Loop über registrierte Mutes pro Sound-Play.
 * ALT: Kein stabiler Fabric-Hook für Sound-Filter verfügbar.
 */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@Inject(method = "play", at = @At("HEAD"), cancellable = true)
	private void primetooler$muteBeacon(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
		if (SoundMuteRegistry.shouldMute(sound)) {
			cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
		}
	}
}
