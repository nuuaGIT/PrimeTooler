package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * WHY: Prefix chat names for admin/special players on the client.
 * PERF: One component build per incoming chat message.
 */
@Mixin(ChatListener.class)
public class ChatListenerMixin {
	@ModifyVariable(
		method = "handlePlayerChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/chat/ChatType$Bound;)V",
		at = @At("HEAD"),
		argsOnly = true,
		index = 3
	)
	private ChatType.Bound primetooler$decorateChatName(ChatType.Bound bound, PlayerChatMessage msg, GameProfile profile) {
		if (bound == null || profile == null) {
			return bound;
		}
		float timeSeconds = (float) (System.nanoTime() * 1.0e-9);
		Component decorated = PlayerMarkRegistry.decorateMarkedName(
			profile.id(),
			bound.name(),
			profile.name(),
			timeSeconds,
			Minecraft.getInstance().font
		);
		if (decorated == null) {
			return bound;
		}
		return new ChatType.Bound(bound.chatType(), decorated, bound.targetName());
	}
}
