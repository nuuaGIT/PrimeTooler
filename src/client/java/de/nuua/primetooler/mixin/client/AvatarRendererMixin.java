package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WHY: Prefix player name tags with mod marker icons.
 * PERF: One component concat per render state.
 */
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void primetooler$prefixNameTag(Avatar entity, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
		if (state == null || state.nameTag == null) {
			return;
		}
		float timeSeconds = (float) (System.nanoTime() * 1.0e-9);
		Component decorated = PlayerMarkRegistry.decorateMarkedName(
			entity.getUUID(),
			state.nameTag,
			entity.getScoreboardName(),
			timeSeconds,
			Minecraft.getInstance().font
		);
		state.nameTag = decorated;
	}
}
