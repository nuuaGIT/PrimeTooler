package de.nuua.primetooler;

import de.nuua.primetooler.core.lifecycle.Boot;
import de.nuua.primetooler.core.lifecycle.EnvironmentInfo;
import de.nuua.primetooler.core.config.ClientSettingsConfig;
import de.nuua.primetooler.features.camerazoom.client.CameraZoomState;
import de.nuua.primetooler.features.chatmention.client.ChatMentionState;
import de.nuua.primetooler.features.durabilityguard.client.DurabilityGuardState;
import de.nuua.primetooler.features.durabilityguard.client.ArmorDurabilityWarningHudElement;
import de.nuua.primetooler.features.doubledrop.client.DoubleDropState;
import de.nuua.primetooler.features.inventorycalc.client.InventoryCalculatorState;
import de.nuua.primetooler.features.inventoryeffects.client.InventoryEffectsState;
import de.nuua.primetooler.features.inventoryeffects.client.HudEffectsState;
import de.nuua.primetooler.features.locatorbar.client.LocatorBarState;
import de.nuua.primetooler.features.resourcepackguard.client.ResourcePackGuardState;
import de.nuua.primetooler.features.terminalstackcount.client.TerminalStackCountState;
import de.nuua.primetooler.features.terminalstackcount.client.TerminalStackCountOverlayProvider;
import de.nuua.primetooler.features.autospawn.client.AutoSpawnClientModule;
import de.nuua.primetooler.features.autospawn.client.AutoSpawnState;
import de.nuua.primetooler.features.actionbar.client.ActionbarMoveState;
import de.nuua.primetooler.features.actionbar.client.ActionbarHudElement;
import de.nuua.primetooler.features.fishbag.client.FishbagTotalHudElement;
import de.nuua.primetooler.features.fishbag.client.FishbagWeightHudElement;
import de.nuua.primetooler.features.fishbag.client.FishbagCoinsHudElement;
import de.nuua.primetooler.features.fishbag.client.FishMoneyTrackerHudElement;
import de.nuua.primetooler.features.fishbag.client.FishbagTotalScanJob;
import de.nuua.primetooler.features.jobtracker.client.JobMoneyTrackerHudElement;
import de.nuua.primetooler.features.jobtracker.client.JobTrackerClientModule;
import de.nuua.primetooler.features.jobtracker.client.JobTrackerState;
import de.nuua.primetooler.features.jobtracker.client.JobXpTrackerHudElement;
import de.nuua.primetooler.features.autoangelsystem.client.AutoAngelSystemClientModule;
import de.nuua.primetooler.features.autoangelsystem.client.AutoAngelSystemState;
import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import de.nuua.primetooler.features.debugtitles.client.DebugTitlesClientModule;
import de.nuua.primetooler.features.primemenu.client.PrimeMenuClientModule;
import de.nuua.primetooler.features.camerazoom.client.FrontCameraToggleState;
import de.nuua.primetooler.features.playermark.client.SpecialNamesState;
import de.nuua.primetooler.features.playermark.client.PlayerMarkRegistry;
import de.nuua.primetooler.features.playermark.client.ClanTagState;
import de.nuua.primetooler.features.sound.client.BeaconSoundState;
import de.nuua.primetooler.features.sound.client.JackpotSoundState;
import de.nuua.primetooler.features.sound.client.SoundMuteRegistry;
import de.nuua.primetooler.api.v1.client.hud.HudElementRegistry;
import de.nuua.primetooler.api.v1.client.hud.HudLayoutState;
import de.nuua.primetooler.api.v1.client.itemcount.ItemCountOverlayRegistry;
import de.nuua.primetooler.api.v1.client.inventoryscan.InventoryScanScheduler;
import de.nuua.primetooler.platform.sound.SoundPlayer;
import de.nuua.primetooler.platform.config.ClientConfigIO;
import de.nuua.primetooler.platform.PlatformEnvironment;
import de.nuua.primetooler.platform.event.FabricClientTickBridge;
import de.nuua.primetooler.platform.inventory.InventoryScanClientModule;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class PrimeToolerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EnvironmentInfo env = PlatformEnvironment.current();
		ClientSettingsConfig settings = ClientConfigIO.loadClientSettings();
		boolean uwuAccess = PlayerMarkRegistry.isUwuUser();
		CameraZoomState.setEnabled(settings.unlimitedZoom);
		DurabilityGuardState.setEnabled(settings.durabilityGuard);
		InventoryCalculatorState.setEnabled(settings.inventoryCalc);
		LocatorBarState.setEnabled(settings.locatorBar);
		ResourcePackGuardState.setEnabled(settings.blockServerPacks);
		CheckItemClientModule.setSlotLockingEnabled(settings.slotLocking);
		AutoSpawnState.setEnabled(settings.autoSpawnLowHealth && uwuAccess);
		AutoSpawnState.setHeartsThreshold(settings.autoSpawnHeartsThreshold);
		SpecialNamesState.setEnabled(settings.specialNames);
		FrontCameraToggleState.setDisabled(settings.disableFrontCamera);
		InventoryEffectsState.setEnabled(settings.inventoryEffects);
		HudEffectsState.setEnabled(settings.hudEffects);
		ActionbarMoveState.setEnabled(settings.moveActionbar);
		TerminalStackCountState.setEnabled(settings.terminalStackCount);
		de.nuua.primetooler.features.fishbag.client.FishbagTotalState.setTotalEnabled(settings.fishbagTotal);
		de.nuua.primetooler.features.fishbag.client.FishbagTotalState.setWeightEnabled(settings.fishbagWeight);
		de.nuua.primetooler.features.fishbag.client.FishbagTotalState.setCoinsEnabled(settings.fishbagCoins);
		de.nuua.primetooler.features.fishbag.client.FishbagTotalState.setMoneyTrackerEnabled(settings.fishMoneyTracker);
		JobTrackerState.setXpEnabled(settings.jobXpTracker);
		JobTrackerState.setMoneyEnabled(settings.jobMoneyTracker);
		AutoAngelSystemState.setEnabled(settings.autoAngelSystem && uwuAccess);
		ClanTagState.setEnabled(settings.hideClanTag);
		BeaconSoundState.setEnabled(settings.muteBoosterSound);
		JackpotSoundState.setEnabled(settings.muteJackpotSound);
		DoubleDropState.setMode(DoubleDropState.fromConfigValue(settings.doubleDropMode));
		SoundPlayer.setWarningVolume(settings.warningSoundVolume / 100.0f);
		ChatMentionState.setEnabled(settings.chatMention);
		HudLayoutState.load(ClientConfigIO.loadHudLayout());
		HudElementRegistry.register(new ArmorDurabilityWarningHudElement());
		HudElementRegistry.register(new ActionbarHudElement());
		HudElementRegistry.register(new FishbagTotalHudElement());
		HudElementRegistry.register(new FishbagWeightHudElement());
		HudElementRegistry.register(new FishbagCoinsHudElement());
		HudElementRegistry.register(new FishMoneyTrackerHudElement());
		HudElementRegistry.register(new JobXpTrackerHudElement());
		HudElementRegistry.register(new JobMoneyTrackerHudElement());
		ItemCountOverlayRegistry.register(new TerminalStackCountOverlayProvider());
		InventoryScanScheduler.register(new FishbagTotalScanJob());
		SoundMuteRegistry.register(SoundEvents.BEACON_ACTIVATE.location(), BeaconSoundState::isEnabled);
		SoundMuteRegistry.register(ResourceLocation.withDefaultNamespace("entity.ender_dragon.growl"),
			JackpotSoundState::isEnabled);
		if (settings.blockServerPacks) {
			ResourcePackGuardState.applyClientState(true);
		}
		Boot.bootClient(
			env,
			new FabricClientTickBridge(),
			new InventoryScanClientModule(),
			new PrimeMenuClientModule(),
			new CheckItemClientModule(),
			new DebugTitlesClientModule(),
			new AutoAngelSystemClientModule(),
			new JobTrackerClientModule(),
			new AutoSpawnClientModule()
		);
	}
}
