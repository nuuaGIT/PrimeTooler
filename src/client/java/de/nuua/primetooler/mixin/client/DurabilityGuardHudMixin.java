package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.api.v1.client.hud.HudLayoutState;
import de.nuua.primetooler.features.durabilityguard.client.DurabilityGuardState;
import de.nuua.primetooler.features.durabilityguard.client.ArmorDurabilityWarningHudElement;
import de.nuua.primetooler.platform.sound.SoundPlayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
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
	private static final float WARNING_SOUND_VOLUME = 0.4f;
	private static final float WARNING_SOUND_PITCH = 0.3f;

	@Unique
	private static boolean primetooler$lastArmorWarningVisible;
	@Unique
	private static final ArmorDurabilityWarningHudElement primetooler$armorWarningElement =
		new ArmorDurabilityWarningHudElement();

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
		int width = minecraft.getWindow().getGuiScaledWidth();
		int height = minecraft.getWindow().getGuiScaledHeight();
		int elementWidth = primetooler$armorWarningElement.width(minecraft);
		int elementHeight = primetooler$armorWarningElement.height(minecraft);
		int defaultX = primetooler$armorWarningElement.defaultX(minecraft, width, height);
		int defaultY = primetooler$armorWarningElement.defaultY(minecraft, width, height);
		int x = HudLayoutState.resolveX(primetooler$armorWarningElement.id(), width, elementWidth, defaultX);
		int y = HudLayoutState.resolveY(primetooler$armorWarningElement.id(), height, elementHeight, defaultY);
		primetooler$armorWarningElement.render(graphics, minecraft, x, y, 0.0f, false);
	}

	private static boolean shouldBlink(Gui gui) {
		if (gui == null) {
			return false;
		}
		int ticks = gui.getGuiTicks();
		return (ticks / BLINK_TICKS) % 2 == 0;
	}
}
