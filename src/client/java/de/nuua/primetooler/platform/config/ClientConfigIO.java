package de.nuua.primetooler.platform.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.nuua.primetooler.core.config.ChatInputsConfig;
import de.nuua.primetooler.core.config.ClientSettingsConfig;
import de.nuua.primetooler.core.config.ContainerCacheConfig;
import de.nuua.primetooler.core.config.SavedItemsConfig;
import de.nuua.primetooler.core.config.SavedSlotsConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfigIO {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String DIRECTORY = "primetooler";
	private static final String CHAT_INPUTS_FILE = "chat_inputs.json";
	private static final String SETTINGS_FILE = "settings.json";
	private static final String SAVED_ITEMS_FILE = "saved_items.json";
	private static final String CONTAINER_CACHE_FILE = "container_cache.json";
	private static final String SAVED_SLOTS_FILE = "saved_slots.json";

	private ClientConfigIO() {
	}

	public static ChatInputsConfig loadChatInputs(int expected) {
		Path path = configPath(CHAT_INPUTS_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ChatInputsConfig config = GSON.fromJson(reader, ChatInputsConfig.class);
				if (config != null) {
					config.lines = normalizeLines(config.lines, expected);
					config.cooldownEnds = normalizeCooldowns(config.cooldownEnds, expected);
					return config;
				}
			} catch (IOException ignored) {
			}
		}
		return new ChatInputsConfig(normalizeLines(null, expected), normalizeCooldowns(null, expected));
	}

	public static void saveChatInputs(ChatInputsConfig config) {
		if (config == null) {
			return;
		}
		Path path = configPath(CHAT_INPUTS_FILE);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				ChatInputsConfig trimmed = trimChatInputs(config);
				GSON.toJson(trimmed, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static ClientSettingsConfig loadClientSettings() {
		Path path = configPath(SETTINGS_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ClientSettingsConfig config = GSON.fromJson(reader, ClientSettingsConfig.class);
				if (config != null) {
					return config;
				}
			} catch (IOException ignored) {
			}
		}
		return new ClientSettingsConfig();
	}

	public static void saveClientSettings(ClientSettingsConfig config) {
		if (config == null) {
			return;
		}
		Path path = configPath(SETTINGS_FILE);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static SavedItemsConfig loadSavedItems() {
		Path path = configPath(SAVED_ITEMS_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				SavedItemsConfig config = GSON.fromJson(reader, SavedItemsConfig.class);
				if (config != null) {
					if (config.entries == null) {
						config.entries = new java.util.ArrayList<>();
					}
					return config;
				}
			} catch (IOException ignored) {
			}
		}
		return new SavedItemsConfig();
	}

	public static void saveSavedItems(SavedItemsConfig config) {
		if (config == null) {
			return;
		}
		Path path = configPath(SAVED_ITEMS_FILE);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static ContainerCacheConfig loadContainerCache() {
		Path path = configPath(CONTAINER_CACHE_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ContainerCacheConfig config = GSON.fromJson(reader, ContainerCacheConfig.class);
				if (config != null) {
					if (config.entries == null) {
						config.entries = new java.util.ArrayList<>();
					}
					return config;
				}
			} catch (IOException ignored) {
			}
		}
		return new ContainerCacheConfig();
	}

	public static void saveContainerCache(ContainerCacheConfig config) {
		if (config == null) {
			return;
		}
		Path path = configPath(CONTAINER_CACHE_FILE);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static SavedSlotsConfig loadSavedSlots() {
		Path path = configPath(SAVED_SLOTS_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				SavedSlotsConfig config = GSON.fromJson(reader, SavedSlotsConfig.class);
				if (config != null) {
					if (config.slots == null) {
						config.slots = new java.util.ArrayList<>();
					}
					return config;
				}
			} catch (IOException ignored) {
			}
		}
		return new SavedSlotsConfig();
	}

	public static void saveSavedSlots(SavedSlotsConfig config) {
		if (config == null) {
			return;
		}
		Path path = configPath(SAVED_SLOTS_FILE);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException ignored) {
		}
	}

	private static Path configPath(String fileName) {
		return FabricLoader.getInstance().getConfigDir().resolve(DIRECTORY).resolve(fileName);
	}

	private static String[] normalizeLines(String[] input, int expected) {
		String[] normalized = new String[Math.max(expected, 0)];
		if (input != null) {
			int count = Math.min(input.length, normalized.length);
			for (int i = 0; i < count; i++) {
				String value = input[i];
				normalized[i] = value == null ? "" : value;
			}
		}
		for (int i = 0; i < normalized.length; i++) {
			if (normalized[i] == null) {
				normalized[i] = "";
			}
		}
		return normalized;
	}

	private static long[] normalizeCooldowns(long[] input, int expected) {
		long[] normalized = new long[Math.max(expected, 0)];
		if (input != null) {
			int count = Math.min(input.length, normalized.length);
			for (int i = 0; i < count; i++) {
				long value = input[i];
				normalized[i] = value < 0L ? 0L : value;
			}
		}
		return normalized;
	}

	private static ChatInputsConfig trimChatInputs(ChatInputsConfig config) {
		String[] lines = config.lines == null ? new String[0] : config.lines;
		long[] cooldowns = config.cooldownEnds == null ? new long[0] : config.cooldownEnds;
		int last = -1;
		for (int i = 0; i < lines.length; i++) {
			String value = lines[i];
			if (value != null && !value.isEmpty()) {
				last = i;
			}
		}
		if (last < 0) {
			return new ChatInputsConfig(new String[0], new long[0]);
		}
		int length = last + 1;
		String[] trimmedLines = new String[length];
		long[] trimmedCooldowns = new long[length];
		for (int i = 0; i < length; i++) {
			String value = lines[i];
			trimmedLines[i] = value == null ? "" : value;
			if (i < cooldowns.length) {
				long cooldown = cooldowns[i];
				trimmedCooldowns[i] = cooldown < 0L ? 0L : cooldown;
			}
		}
		return new ChatInputsConfig(trimmedLines, trimmedCooldowns);
	}
}
