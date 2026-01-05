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
		method = "handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
		at = @At("HEAD"),
		argsOnly = true,
		index = 1
	)
	private Component primetooler$highlightMentionsSystem(Component value, Component msg, boolean overlay) {
		if (value == null || overlay || !ChatMentionState.isEnabled()) {
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
		if (isLikelyOwnChatLine(value, name)) {
			return value;
		}
		ChatMentionHighlighter.Result result = ChatMentionHighlighter.highlight(value, name);
		if (!result.matched()) {
			return value;
		}
		Component title = Component.literal(Messages.applyColorCodes(Messages.get(Messages.Id.CHAT_MENTION_TOAST_TITLE)));
		String alertText = Messages.get(Messages.Id.CHAT_MENTION_ALERT).replace("<NAME>", "System");
		Component alert = Component.literal(Messages.applyColorCodes(alertText));
		ChatMentionToast.addOrUpdate(client.getToastManager(), title, alert);
		SoundPlayer.playWarning(SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 1.1f);
		return result.component();
	}

	@ModifyVariable(
		method = "handleDisguisedChatMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType$Bound;)V",
		at = @At("HEAD"),
		argsOnly = true,
		index = 1
	)
	private Component primetooler$highlightMentionsDisguised(Component value, Component msg, ChatType.Bound bound) {
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
		if (bound != null && bound.name() != null && name.equals(bound.name().getString())) {
			return value;
		}
		if (isLikelyOwnChatLine(value, name)) {
			return value;
		}
		ChatMentionHighlighter.Result result = ChatMentionHighlighter.highlight(value, name);
		if (!result.matched()) {
			return value;
		}
		Component title = Component.literal(Messages.applyColorCodes(Messages.get(Messages.Id.CHAT_MENTION_TOAST_TITLE)));
		String sender = bound != null && bound.name() != null ? bound.name().getString() : "Chat";
		String alertText = Messages.get(Messages.Id.CHAT_MENTION_ALERT).replace("<NAME>", sender);
		Component alert = Component.literal(Messages.applyColorCodes(alertText));
		ChatMentionToast.addOrUpdate(client.getToastManager(), title, alert);
		SoundPlayer.playWarning(SoundEvents.NOTE_BLOCK_PLING.value(), 0.5f, 1.1f);
		return result.component();
	}

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
		if (isSelf(profile, client)) {
			return value;
		}
		String name = client.player.getName().getString();
		if (name == null || name.isEmpty()) {
			return value;
		}
		ChatMentionHighlighter.Result result = ChatMentionHighlighter.highlight(value.decoratedContent(), name);
		if (!result.matched()) {
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

	private static boolean isLikelyOwnChatLine(Component message, String selfName) {
		if (message == null || selfName == null || selfName.isEmpty()) {
			return false;
		}
		String raw = message.getString();
		if (raw == null || raw.isEmpty()) {
			return false;
		}
		int len = raw.length();
		int start = 0;
		while (start < len) {
			char ch = raw.charAt(start);
			if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r') {
				break;
			}
			start++;
		}
		if (start >= len) {
			return false;
		}
		String s = raw.substring(start);

		// Common chat formats that include the sender name prefix in the rendered line.
		String angle = "<" + selfName + ">";
		if (s.startsWith(angle)) {
			return true;
		}

		int idx = s.indexOf(selfName);
		if (idx < 0 || idx > 24) {
			return false;
		}
		if (idx > 0) {
			char before = s.charAt(idx - 1);
			if (before != ' ' && before != ']' && before != ')' && before != '>') {
				return false;
			}
		}
		int end = idx + selfName.length();
		if (end >= s.length()) {
			return true;
		}
		char next = s.charAt(end);
		if (next == ':' || next == '>' || next == '|' || next == '-') {
			return true;
		}
		if (next == ' ' && end + 1 < s.length()) {
			char after = s.charAt(end + 1);
			if (after == ':' || after == '>' || after == '|' || after == '-' || after == '\u00BB') {
				return true;
			}
		}
		if (next == '\u00BB') {
			return true;
		}
		return false;
	}
}
