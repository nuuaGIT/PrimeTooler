package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.features.chatmention.client.ChatMentionHighlighter;
import de.nuua.primetooler.features.chatmention.client.ChatMentionState;
import de.nuua.primetooler.features.chatmention.client.ChatMentionToast;
import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import de.nuua.primetooler.platform.sound.SoundPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.sounds.SoundEvents;
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
		index = 1
	)
	private PlayerChatMessage primetooler$highlightMentions(PlayerChatMessage value, PlayerChatMessage msg,
		GameProfile profile, ChatType.Bound bound) {
		if (value == null || !ChatMentionState.isEnabled()) {
			return value;
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null) {
			return value;
		}
		String name = client.player.getName().getString();
		if (name == null || name.isEmpty()) {
			return value;
		}
		ChatMentionHighlighter.Result result = ChatMentionHighlighter.highlight(value.decoratedContent(), name);
		boolean matched = result.matched();
		if (!matched && !isSelf(profile, client)) {
			return value;
		}
		Component title = Component.literal(Messages.applyColorCodes(Messages.get(Messages.Id.CHAT_MENTION_TOAST_TITLE)));
		String alertText = Messages.get(Messages.Id.CHAT_MENTION_ALERT);
		if (profile != null && profile.name() != null) {
			alertText = alertText.replace("<NAME>", profile.name());
		}
		Component alert = Component.literal(Messages.applyColorCodes(alertText));
		ChatMentionToast.addOrUpdate(client.getToastManager(), title, alert);
		SoundPlayer.playWarning(SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 1.1f);
		if (!matched) {
			return value;
		}
		return value.withUnsignedContent(result.component());
	}

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
		Component baseName = bound.name();
		if (ChatMentionState.isEnabled() && isSelf(profile, Minecraft.getInstance())) {
			ChatMentionHighlighter.Result highlighted = ChatMentionHighlighter.highlight(baseName, profile.name());
			if (highlighted.matched()) {
				baseName = highlighted.component();
			}
		}
		float timeSeconds = (float) (System.nanoTime() * 1.0e-9);
		Component decorated = PlayerMarkRegistry.decorateMarkedName(
			profile.id(),
			baseName,
			profile.name(),
			timeSeconds,
			Minecraft.getInstance().font
		);
		if (decorated == null) {
			return bound;
		}
		return new ChatType.Bound(bound.chatType(), decorated, bound.targetName());
	}

	private static boolean isSelf(GameProfile profile, Minecraft client) {
		if (profile == null || client == null || client.player == null) {
			return false;
		}
		java.util.UUID self = client.player.getUUID();
		return self != null && self.equals(profile.id());
	}
}
