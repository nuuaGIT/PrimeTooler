package de.nuua.primetooler;

import de.nuua.primetooler.core.lifecycle.Boot;
import de.nuua.primetooler.core.lifecycle.EnvironmentInfo;
import de.nuua.primetooler.core.config.ClientSettingsConfig;
import de.nuua.primetooler.features.camerazoom.client.CameraZoomState;
import de.nuua.primetooler.features.chatmention.client.ChatMentionState;
import de.nuua.primetooler.features.durabilityguard.client.DurabilityGuardState;
import de.nuua.primetooler.features.doubledrop.client.DoubleDropState;
import de.nuua.primetooler.features.inventorycalc.client.InventoryCalculatorState;
import de.nuua.primetooler.features.inventoryeffects.client.InventoryEffectsState;
import de.nuua.primetooler.features.inventoryeffects.client.HudEffectsState;
import de.nuua.primetooler.features.locatorbar.client.LocatorBarState;
import de.nuua.primetooler.features.resourcepackguard.client.ResourcePackGuardState;
import de.nuua.primetooler.features.autospawn.client.AutoSpawnClientModule;
import de.nuua.primetooler.features.autospawn.client.AutoSpawnState;
import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import de.nuua.primetooler.features.primemenu.client.PrimeMenuClientModule;
import de.nuua.primetooler.features.camerazoom.client.FrontCameraToggleState;
import de.nuua.primetooler.features.playermark.client.SpecialNamesState;
import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import de.nuua.primetooler.features.playermark.client.ClanTagState;
import de.nuua.primetooler.features.sound.client.BeaconSoundState;
import de.nuua.primetooler.features.sound.client.JackpotSoundState;
import de.nuua.primetooler.features.sound.client.SoundMuteRegistry;
import de.nuua.primetooler.platform.sound.SoundPlayer;
import de.nuua.primetooler.platform.config.ClientConfigIO;
import de.nuua.primetooler.platform.PlatformEnvironment;
import de.nuua.primetooler.platform.event.FabricClientTickBridge;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class PrimeToolerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EnvironmentInfo env = PlatformEnvironment.current();
		ClientSettingsConfig settings = ClientConfigIO.loadClientSettings();
		boolean specialAccess = PlayerMarkRegistry.isAuthorizedUser();
		CameraZoomState.setEnabled(settings.unlimitedZoom && specialAccess);
		DurabilityGuardState.setEnabled(settings.durabilityGuard);
		InventoryCalculatorState.setEnabled(settings.inventoryCalc);
		LocatorBarState.setEnabled(settings.locatorBar);
		ResourcePackGuardState.setEnabled(settings.blockServerPacks);
		CheckItemClientModule.setSlotLockingEnabled(settings.slotLocking);
		AutoSpawnState.setEnabled(settings.autoSpawnLowHealth && specialAccess);
		SpecialNamesState.setEnabled(settings.specialNames);
		FrontCameraToggleState.setDisabled(settings.disableFrontCamera);
		InventoryEffectsState.setEnabled(settings.inventoryEffects);
		HudEffectsState.setEnabled(settings.hudEffects);
		ClanTagState.setEnabled(settings.hideClanTag);
		BeaconSoundState.setEnabled(settings.muteBoosterSound);
		JackpotSoundState.setEnabled(settings.muteJackpotSound);
		DoubleDropState.setMode(DoubleDropState.fromConfigValue(settings.doubleDropMode));
		SoundPlayer.setWarningVolume(settings.warningSoundVolume / 100.0f);
		ChatMentionState.setEnabled(settings.chatMention);
		SoundMuteRegistry.register(SoundEvents.BEACON_ACTIVATE.location(), BeaconSoundState::isEnabled);
		SoundMuteRegistry.register(ResourceLocation.withDefaultNamespace("entity.ender_dragon.growl"),
			JackpotSoundState::isEnabled);
		if (settings.blockServerPacks) {
			ResourcePackGuardState.applyClientState(true);
		}
		Boot.bootClient(
			env,
			new FabricClientTickBridge(),
			new PrimeMenuClientModule(),
			new CheckItemClientModule(),
			new AutoSpawnClientModule()
		);
	}
}
