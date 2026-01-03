package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.resourcepackguard.client.ResourcePackGuardState;
import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.client.resources.server.DownloadedPackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Enforce a client-side toggle that blocks server packs and unloads them.
 * PERF: O(1) checks, no hotpath allocations.
 * ALT: No Fabric hook for server pack control.
 */
@Mixin(DownloadedPackSource.class)
public abstract class DownloadedPackSourceMixin {
	@Shadow
	public abstract void rejectServerPacks();

	@Shadow
	public abstract void popAll();

	@Inject(method = "pushPack", at = @At("HEAD"))
	private void primetooler$handleRemotePack(UUID id, URL url, String hash, CallbackInfo ci) {
		ResourcePackGuardState.cacheRemote(id, url, hash);
		if (!ResourcePackGuardState.isEnabled() || ResourcePackGuardState.isReplaying()) {
			return;
		}
		ResourcePackGuardState.beginGuardUnload();
		rejectServerPacks();
		popAll();
		ResourcePackGuardState.endGuardUnload();
	}

	@Inject(method = "pushLocalPack", at = @At("HEAD"))
	private void primetooler$handleLocalPack(UUID id, Path path, CallbackInfo ci) {
		ResourcePackGuardState.cacheLocal(id, path);
		if (!ResourcePackGuardState.isEnabled() || ResourcePackGuardState.isReplaying()) {
			return;
		}
		ResourcePackGuardState.beginGuardUnload();
		rejectServerPacks();
		popAll();
		ResourcePackGuardState.endGuardUnload();
	}

	@Inject(method = "popPack", at = @At("HEAD"))
	private void primetooler$dropPack(UUID id, CallbackInfo ci) {
		if (!ResourcePackGuardState.isGuardUnloading()) {
			ResourcePackGuardState.remove(id);
		}
	}

	@Inject(method = "popAll", at = @At("HEAD"))
	private void primetooler$dropAll(CallbackInfo ci) {
		if (!ResourcePackGuardState.isGuardUnloading()) {
			ResourcePackGuardState.clear();
		}
	}

	@Inject(method = "cleanupAfterDisconnect", at = @At("HEAD"))
	private void primetooler$cleanup(CallbackInfo ci) {
		ResourcePackGuardState.clear();
	}
}
