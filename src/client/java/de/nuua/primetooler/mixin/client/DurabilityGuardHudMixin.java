package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.core.Messages;
import de.nuua.primetooler.features.durabilityguard.client.DurabilityGuardState;
import de.nuua.primetooler.platform.sound.SoundPlayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WARUM: Warnung anzeigen, wenn Ruestung unter den Haltbarkeitswert faellt.
 * PERF: Ein schneller Check pro Hotbar-Render, keine Allokationen.
 * ALT: Kein stabiler Fabric-Hook fuer Hotbar-nahe Overlays.
 */
@Mixin(Gui.class)
public class DurabilityGuardHudMixin {
	private static final int BLINK_TICKS = 10;
	private static final int HOTBAR_HEIGHT = 22;
	private static final int HOTBAR_OFFSET_Y = 22;
	private static final int HOTBAR_HALF_WIDTH = 91;
	private static final int TEXT_OFFSET_X = 8;
	private static final int COLOR_WARNING = 0xFFFF5555;
	private static final float WARNING_SOUND_VOLUME = 0.4f;
	private static final float WARNING_SOUND_PITCH = 0.3f;

	@Unique
	private static boolean primetooler$lastArmorWarningVisible;

	@Inject(method = "renderHotbarAndDecorations", at = @At("TAIL"))
	private void primetooler$renderArmorWarning(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
		if (!DurabilityGuardState.isEnabled()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (!DurabilityGuardState.shouldWarnArmor(player)) {
			primetooler$lastArmorWarningVisible = false;
			return;
		}
		boolean visible = shouldBlink(minecraft.gui);
		if (!visible) {
			primetooler$lastArmorWarningVisible = false;
			return;
		}
		if (!primetooler$lastArmorWarningVisible) {
			SoundPlayer.playWarning(SoundEvents.NOTE_BLOCK_BASS.value(), WARNING_SOUND_VOLUME, WARNING_SOUND_PITCH);
			primetooler$lastArmorWarningVisible = true;
		}
		Font font = minecraft.font;
		int width = minecraft.getWindow().getGuiScaledWidth();
		int height = minecraft.getWindow().getGuiScaledHeight();
		String text = Messages.applyColorCodes(Messages.get(Messages.Id.ARMOR_DURABILITY_LOW));
		int x = width / 2 + HOTBAR_HALF_WIDTH + TEXT_OFFSET_X;
		int y = height - HOTBAR_OFFSET_Y + (HOTBAR_HEIGHT - font.lineHeight) / 2;
		graphics.drawString(font, text, x, y, COLOR_WARNING, true);
	}

	private static boolean shouldBlink(Gui gui) {
		if (gui == null) {
			return false;
		}
		int ticks = gui.getGuiTicks();
		return (ticks / BLINK_TICKS) % 2 == 0;
	}
}
