package de.nuua.primetooler.platform.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.nuua.primetooler.core.config.ChatInputsConfig;
import de.nuua.primetooler.core.config.ClientSettingsConfig;
import de.nuua.primetooler.core.config.ContainerCacheConfig;
import de.nuua.primetooler.core.config.SavedItemsConfig;
import de.nuua.primetooler.core.config.SavedSlotsConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

public final class ClientConfigIO {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String DIRECTORY = "primetooler";
	private static final String CHAT_INPUTS_FILE = "chat_inputs.json";
	private static final String CHAT_INPUTS_FILE_BIN = "chat_inputs.bin";
	private static final String SETTINGS_FILE = "settings.json";
	private static final String SETTINGS_FILE_BIN = "settings.bin";
	private static final String SAVED_ITEMS_FILE = "saved_items.json";
	private static final String SAVED_ITEMS_FILE_BIN = "saved_items.bin";
	private static final String CONTAINER_CACHE_FILE = "container_cache.json";
	private static final String CONTAINER_CACHE_FILE_BIN = "container_cache.bin";
	private static final String SAVED_SLOTS_FILE = "saved_slots.json";
	private static final String SAVED_SLOTS_FILE_BIN = "saved_slots.bin";
	private static final byte[] SETTINGS_MAGIC = new byte[] { 'P', 'T', 'C', '1' };
	private static final int SETTINGS_VERSION = 3;
	private static final byte[] CHAT_MAGIC = new byte[] { 'P', 'T', 'C', '2' };
	private static final int CHAT_VERSION = 1;
	private static final byte[] ITEMS_MAGIC = new byte[] { 'P', 'T', 'C', '3' };
	private static final int ITEMS_VERSION = 1;
	private static final byte[] CACHE_MAGIC = new byte[] { 'P', 'T', 'C', '4' };
	private static final int CACHE_VERSION = 1;
	private static final byte[] SLOTS_MAGIC = new byte[] { 'P', 'T', 'C', '5' };
	private static final int SLOTS_VERSION = 1;
	private static final byte[] SETTINGS_XOR_KEY = new byte[] {
		0x3A, 0x2F, 0x11, 0x55, 0x6E, 0x09, 0x71, 0x28, 0x44, 0x5B, 0x19, 0x2C
	};

	private ClientConfigIO() {
	}

	public static ChatInputsConfig loadChatInputs(int expected) {
		Path binPath = configPath(CHAT_INPUTS_FILE_BIN);
		if (Files.exists(binPath)) {
			ChatInputsConfig binConfig = readChatInputsBin(binPath, expected);
			if (binConfig != null) {
				return binConfig;
			}
		}
		Path path = configPath(CHAT_INPUTS_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ChatInputsConfig config = GSON.fromJson(reader, ChatInputsConfig.class);
				if (config != null) {
					config.lines = normalizeLines(config.lines, expected);
					config.cooldownEnds = normalizeCooldowns(config.cooldownEnds, expected);
					writeChatInputsBin(binPath, config);
					try {
						Files.deleteIfExists(path);
					} catch (IOException ignored) {
					}
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
		Path path = configPath(CHAT_INPUTS_FILE_BIN);
		try {
			Files.createDirectories(path.getParent());
			ChatInputsConfig trimmed = trimChatInputs(config);
			writeChatInputsBin(path, trimmed);
		} catch (IOException ignored) {
		}
	}

	public static ClientSettingsConfig loadClientSettings() {
		Path binPath = configPath(SETTINGS_FILE_BIN);
		if (Files.exists(binPath)) {
			ClientSettingsConfig binConfig = readSettingsBin(binPath);
			if (binConfig != null) {
				return binConfig;
			}
		}
		Path path = configPath(SETTINGS_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ClientSettingsConfig config = GSON.fromJson(reader, ClientSettingsConfig.class);
				if (config != null) {
					writeSettingsBin(binPath, config);
					try {
						Files.deleteIfExists(path);
					} catch (IOException ignored) {
					}
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
		Path path = configPath(SETTINGS_FILE_BIN);
		try {
			Files.createDirectories(path.getParent());
			writeSettingsBin(path, config);
		} catch (IOException ignored) {
		}
	}

	public static SavedItemsConfig loadSavedItems() {
		Path binPath = configPath(SAVED_ITEMS_FILE_BIN);
		if (Files.exists(binPath)) {
			SavedItemsConfig binConfig = readSavedItemsBin(binPath);
			if (binConfig != null) {
				return binConfig;
			}
		}
		Path path = configPath(SAVED_ITEMS_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				SavedItemsConfig config = GSON.fromJson(reader, SavedItemsConfig.class);
				if (config != null) {
					if (config.entries == null) {
						config.entries = new java.util.ArrayList<>();
					}
					writeSavedItemsBin(binPath, config);
					try {
						Files.deleteIfExists(path);
					} catch (IOException ignored) {
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
		Path path = configPath(SAVED_ITEMS_FILE_BIN);
		try {
			Files.createDirectories(path.getParent());
			writeSavedItemsBin(path, config);
		} catch (IOException ignored) {
		}
	}

	public static ContainerCacheConfig loadContainerCache() {
		Path binPath = configPath(CONTAINER_CACHE_FILE_BIN);
		if (Files.exists(binPath)) {
			ContainerCacheConfig binConfig = readContainerCacheBin(binPath);
			if (binConfig != null) {
				return binConfig;
			}
		}
		Path path = configPath(CONTAINER_CACHE_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ContainerCacheConfig config = GSON.fromJson(reader, ContainerCacheConfig.class);
				if (config != null) {
					if (config.entries == null) {
						config.entries = new java.util.ArrayList<>();
					}
					writeContainerCacheBin(binPath, config);
					try {
						Files.deleteIfExists(path);
					} catch (IOException ignored) {
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
		Path path = configPath(CONTAINER_CACHE_FILE_BIN);
		try {
			Files.createDirectories(path.getParent());
			writeContainerCacheBin(path, config);
		} catch (IOException ignored) {
		}
	}

	public static SavedSlotsConfig loadSavedSlots() {
		Path binPath = configPath(SAVED_SLOTS_FILE_BIN);
		if (Files.exists(binPath)) {
			SavedSlotsConfig binConfig = readSavedSlotsBin(binPath);
			if (binConfig != null) {
				return binConfig;
			}
		}
		Path path = configPath(SAVED_SLOTS_FILE);
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				SavedSlotsConfig config = GSON.fromJson(reader, SavedSlotsConfig.class);
				if (config != null) {
					if (config.slots == null) {
						config.slots = new java.util.ArrayList<>();
					}
					writeSavedSlotsBin(binPath, config);
					try {
						Files.deleteIfExists(path);
					} catch (IOException ignored) {
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
		Path path = configPath(SAVED_SLOTS_FILE_BIN);
		try {
			Files.createDirectories(path.getParent());
			writeSavedSlotsBin(path, config);
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

	private static ClientSettingsConfig readSettingsBin(Path path) {
		try {
			byte[] encoded = Files.readAllBytes(path);
			if (encoded.length == 0) {
				return null;
			}
			byte[] decoded = xor(encoded, SETTINGS_XOR_KEY);
			try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(decoded))) {
				byte[] magic = new byte[SETTINGS_MAGIC.length];
				in.readFully(magic);
				if (!java.util.Arrays.equals(magic, SETTINGS_MAGIC)) {
					return null;
				}
				int version = in.readInt();
				if (version != 1 && version != 2 && version != SETTINGS_VERSION) {
					return null;
				}
				int payloadLength = in.readInt();
				if (payloadLength < 0 || payloadLength > decoded.length) {
					return null;
				}
				byte[] payload = new byte[payloadLength];
				in.readFully(payload);
				long crcStored = in.readLong();
				CRC32 crc32 = new CRC32();
				crc32.update(payload);
				if (crc32.getValue() != crcStored) {
					return null;
				}
				try (DataInputStream payloadIn = new DataInputStream(new ByteArrayInputStream(payload))) {
					ClientSettingsConfig config = new ClientSettingsConfig();
					config.unlimitedZoom = payloadIn.readBoolean();
					config.durabilityGuard = payloadIn.readBoolean();
					config.inventoryCalc = payloadIn.readBoolean();
					config.blockServerPacks = payloadIn.readBoolean();
					config.locatorBar = payloadIn.readBoolean();
					config.slotLocking = payloadIn.readBoolean();
					config.autoSpawnLowHealth = payloadIn.readBoolean();
					config.specialNames = payloadIn.readBoolean();
					config.disableFrontCamera = payloadIn.readBoolean();
					config.inventoryEffects = payloadIn.readBoolean();
					config.hudEffects = payloadIn.readBoolean();
					if (version >= 2) {
						config.hideClanTag = payloadIn.readBoolean();
					} else {
						config.hideClanTag = false;
					}
					if (version >= 3) {
						config.muteBeaconSound = payloadIn.readBoolean();
					} else {
						config.muteBeaconSound = false;
					}
					return config;
				}
			}
		} catch (IOException ignored) {
			return null;
		}
	}

	private static void writeSettingsBin(Path path, ClientSettingsConfig config) {
		try {
			ByteArrayOutputStream payloadOut = new ByteArrayOutputStream();
			try (DataOutputStream data = new DataOutputStream(payloadOut)) {
				data.writeBoolean(config.unlimitedZoom);
				data.writeBoolean(config.durabilityGuard);
				data.writeBoolean(config.inventoryCalc);
				data.writeBoolean(config.blockServerPacks);
				data.writeBoolean(config.locatorBar);
				data.writeBoolean(config.slotLocking);
				data.writeBoolean(config.autoSpawnLowHealth);
				data.writeBoolean(config.specialNames);
				data.writeBoolean(config.disableFrontCamera);
				data.writeBoolean(config.inventoryEffects);
				data.writeBoolean(config.hudEffects);
				data.writeBoolean(config.hideClanTag);
				data.writeBoolean(config.muteBeaconSound);
			}
			byte[] payload = payloadOut.toByteArray();
			CRC32 crc32 = new CRC32();
			crc32.update(payload);
			ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
			try (DataOutputStream out = new DataOutputStream(rawOut)) {
				out.write(SETTINGS_MAGIC);
				out.writeInt(SETTINGS_VERSION);
				out.writeInt(payload.length);
				out.write(payload);
				out.writeLong(crc32.getValue());
			}
			byte[] encoded = xor(rawOut.toByteArray(), SETTINGS_XOR_KEY);
			Files.write(path, encoded);
		} catch (IOException ignored) {
		}
	}

	private static byte[] xor(byte[] input, byte[] key) {
		byte[] out = new byte[input.length];
		for (int i = 0; i < input.length; i++) {
			out[i] = (byte) (input[i] ^ key[i % key.length]);
		}
		return out;
	}

	private static ChatInputsConfig readChatInputsBin(Path path, int expected) {
		try {
			byte[] encoded = Files.readAllBytes(path);
			if (encoded.length == 0) {
				return null;
			}
			byte[] decoded = xor(encoded, SETTINGS_XOR_KEY);
			try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(decoded))) {
				byte[] magic = new byte[CHAT_MAGIC.length];
				in.readFully(magic);
				if (!java.util.Arrays.equals(magic, CHAT_MAGIC)) {
					return null;
				}
				int version = in.readInt();
				if (version != CHAT_VERSION) {
					return null;
				}
				int payloadLength = in.readInt();
				if (payloadLength < 0 || payloadLength > decoded.length) {
					return null;
				}
				byte[] payload = new byte[payloadLength];
				in.readFully(payload);
				long crcStored = in.readLong();
				CRC32 crc32 = new CRC32();
				crc32.update(payload);
				if (crc32.getValue() != crcStored) {
					return null;
				}
				try (DataInputStream payloadIn = new DataInputStream(new ByteArrayInputStream(payload))) {
					int lineCount = payloadIn.readInt();
					if (lineCount < 0 || lineCount > 1024) {
						return null;
					}
					String[] lines = new String[lineCount];
					for (int i = 0; i < lineCount; i++) {
						lines[i] = payloadIn.readUTF();
					}
					int cooldownCount = payloadIn.readInt();
					if (cooldownCount < 0 || cooldownCount > 1024) {
						return null;
					}
					long[] cooldowns = new long[cooldownCount];
					for (int i = 0; i < cooldownCount; i++) {
						cooldowns[i] = payloadIn.readLong();
					}
					return new ChatInputsConfig(
						normalizeLines(lines, expected),
						normalizeCooldowns(cooldowns, expected)
					);
				}
			}
		} catch (IOException ignored) {
			return null;
		}
	}

	private static void writeChatInputsBin(Path path, ChatInputsConfig config) {
		try {
			ByteArrayOutputStream payloadOut = new ByteArrayOutputStream();
			try (DataOutputStream data = new DataOutputStream(payloadOut)) {
				String[] lines = config.lines == null ? new String[0] : config.lines;
				long[] cooldowns = config.cooldownEnds == null ? new long[0] : config.cooldownEnds;
				data.writeInt(lines.length);
				for (int i = 0; i < lines.length; i++) {
					data.writeUTF(lines[i] == null ? "" : lines[i]);
				}
				data.writeInt(cooldowns.length);
				for (int i = 0; i < cooldowns.length; i++) {
					data.writeLong(cooldowns[i]);
				}
			}
			byte[] payload = payloadOut.toByteArray();
			CRC32 crc32 = new CRC32();
			crc32.update(payload);
			ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
			try (DataOutputStream out = new DataOutputStream(rawOut)) {
				out.write(CHAT_MAGIC);
				out.writeInt(CHAT_VERSION);
				out.writeInt(payload.length);
				out.write(payload);
				out.writeLong(crc32.getValue());
			}
			byte[] encoded = xor(rawOut.toByteArray(), SETTINGS_XOR_KEY);
			Files.write(path, encoded);
		} catch (IOException ignored) {
		}
	}

	private static SavedItemsConfig readSavedItemsBin(Path path) {
		try {
			byte[] encoded = Files.readAllBytes(path);
			if (encoded.length == 0) {
				return null;
			}
			byte[] decoded = xor(encoded, SETTINGS_XOR_KEY);
			try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(decoded))) {
				byte[] magic = new byte[ITEMS_MAGIC.length];
				in.readFully(magic);
				if (!java.util.Arrays.equals(magic, ITEMS_MAGIC)) {
					return null;
				}
				int version = in.readInt();
				if (version != ITEMS_VERSION) {
					return null;
				}
				int payloadLength = in.readInt();
				if (payloadLength < 0 || payloadLength > decoded.length) {
					return null;
				}
				byte[] payload = new byte[payloadLength];
				in.readFully(payload);
				long crcStored = in.readLong();
				CRC32 crc32 = new CRC32();
				crc32.update(payload);
				if (crc32.getValue() != crcStored) {
					return null;
				}
				try (DataInputStream payloadIn = new DataInputStream(new ByteArrayInputStream(payload))) {
					int count = payloadIn.readInt();
					if (count < 0 || count > 2048) {
						return null;
					}
					SavedItemsConfig config = new SavedItemsConfig();
					config.entries = new java.util.ArrayList<>(count);
					for (int i = 0; i < count; i++) {
						SavedItemsConfig.SavedItemEntry entry = new SavedItemsConfig.SavedItemEntry();
						entry.id = payloadIn.readUTF();
						entry.itemId = payloadIn.readUTF();
						entry.strongSignature = payloadIn.readUTF();
						entry.weakSignature = payloadIn.readUTF();
						entry.portableSignature = payloadIn.readUTF();
						entry.lastSlot = payloadIn.readInt();
						entry.hasSlot = payloadIn.readBoolean();
						config.entries.add(entry);
					}
					return config;
				}
			}
		} catch (IOException ignored) {
			return null;
		}
	}

	private static void writeSavedItemsBin(Path path, SavedItemsConfig config) {
		try {
			ByteArrayOutputStream payloadOut = new ByteArrayOutputStream();
			try (DataOutputStream data = new DataOutputStream(payloadOut)) {
				java.util.List<SavedItemsConfig.SavedItemEntry> entries =
					config.entries == null ? java.util.List.of() : config.entries;
				data.writeInt(entries.size());
				for (int i = 0; i < entries.size(); i++) {
					SavedItemsConfig.SavedItemEntry entry = entries.get(i);
					data.writeUTF(entry.id == null ? "" : entry.id);
					data.writeUTF(entry.itemId == null ? "" : entry.itemId);
					data.writeUTF(entry.strongSignature == null ? "" : entry.strongSignature);
					data.writeUTF(entry.weakSignature == null ? "" : entry.weakSignature);
					data.writeUTF(entry.portableSignature == null ? "" : entry.portableSignature);
					data.writeInt(entry.lastSlot);
					data.writeBoolean(entry.hasSlot);
				}
			}
			byte[] payload = payloadOut.toByteArray();
			CRC32 crc32 = new CRC32();
			crc32.update(payload);
			ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
			try (DataOutputStream out = new DataOutputStream(rawOut)) {
				out.write(ITEMS_MAGIC);
				out.writeInt(ITEMS_VERSION);
				out.writeInt(payload.length);
				out.write(payload);
				out.writeLong(crc32.getValue());
			}
			byte[] encoded = xor(rawOut.toByteArray(), SETTINGS_XOR_KEY);
			Files.write(path, encoded);
		} catch (IOException ignored) {
		}
	}

	private static ContainerCacheConfig readContainerCacheBin(Path path) {
		try {
			byte[] encoded = Files.readAllBytes(path);
			if (encoded.length == 0) {
				return null;
			}
			byte[] decoded = xor(encoded, SETTINGS_XOR_KEY);
			try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(decoded))) {
				byte[] magic = new byte[CACHE_MAGIC.length];
				in.readFully(magic);
				if (!java.util.Arrays.equals(magic, CACHE_MAGIC)) {
					return null;
				}
				int version = in.readInt();
				if (version != CACHE_VERSION) {
					return null;
				}
				int payloadLength = in.readInt();
				if (payloadLength < 0 || payloadLength > decoded.length) {
					return null;
				}
				byte[] payload = new byte[payloadLength];
				in.readFully(payload);
				long crcStored = in.readLong();
				CRC32 crc32 = new CRC32();
				crc32.update(payload);
				if (crc32.getValue() != crcStored) {
					return null;
				}
				try (DataInputStream payloadIn = new DataInputStream(new ByteArrayInputStream(payload))) {
					int count = payloadIn.readInt();
					if (count < 0 || count > 4096) {
						return null;
					}
					ContainerCacheConfig config = new ContainerCacheConfig();
					config.entries = new java.util.ArrayList<>(count);
					for (int i = 0; i < count; i++) {
						ContainerCacheConfig.ContainerCacheEntry entry = new ContainerCacheConfig.ContainerCacheEntry();
						entry.itemId = payloadIn.readUTF();
						entry.signature = payloadIn.readUTF();
						entry.savedId = payloadIn.readUTF();
						entry.lastSeenMs = payloadIn.readLong();
						config.entries.add(entry);
					}
					return config;
				}
			}
		} catch (IOException ignored) {
			return null;
		}
	}

	private static void writeContainerCacheBin(Path path, ContainerCacheConfig config) {
		try {
			ByteArrayOutputStream payloadOut = new ByteArrayOutputStream();
			try (DataOutputStream data = new DataOutputStream(payloadOut)) {
				java.util.List<ContainerCacheConfig.ContainerCacheEntry> entries =
					config.entries == null ? java.util.List.of() : config.entries;
				data.writeInt(entries.size());
				for (int i = 0; i < entries.size(); i++) {
					ContainerCacheConfig.ContainerCacheEntry entry = entries.get(i);
					data.writeUTF(entry.itemId == null ? "" : entry.itemId);
					data.writeUTF(entry.signature == null ? "" : entry.signature);
					data.writeUTF(entry.savedId == null ? "" : entry.savedId);
					data.writeLong(entry.lastSeenMs);
				}
			}
			byte[] payload = payloadOut.toByteArray();
			CRC32 crc32 = new CRC32();
			crc32.update(payload);
			ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
			try (DataOutputStream out = new DataOutputStream(rawOut)) {
				out.write(CACHE_MAGIC);
				out.writeInt(CACHE_VERSION);
				out.writeInt(payload.length);
				out.write(payload);
				out.writeLong(crc32.getValue());
			}
			byte[] encoded = xor(rawOut.toByteArray(), SETTINGS_XOR_KEY);
			Files.write(path, encoded);
		} catch (IOException ignored) {
		}
	}

	private static SavedSlotsConfig readSavedSlotsBin(Path path) {
		try {
			byte[] encoded = Files.readAllBytes(path);
			if (encoded.length == 0) {
				return null;
			}
			byte[] decoded = xor(encoded, SETTINGS_XOR_KEY);
			try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(decoded))) {
				byte[] magic = new byte[SLOTS_MAGIC.length];
				in.readFully(magic);
				if (!java.util.Arrays.equals(magic, SLOTS_MAGIC)) {
					return null;
				}
				int version = in.readInt();
				if (version != SLOTS_VERSION) {
					return null;
				}
				int payloadLength = in.readInt();
				if (payloadLength < 0 || payloadLength > decoded.length) {
					return null;
				}
				byte[] payload = new byte[payloadLength];
				in.readFully(payload);
				long crcStored = in.readLong();
				CRC32 crc32 = new CRC32();
				crc32.update(payload);
				if (crc32.getValue() != crcStored) {
					return null;
				}
				try (DataInputStream payloadIn = new DataInputStream(new ByteArrayInputStream(payload))) {
					int count = payloadIn.readInt();
					if (count < 0 || count > 1024) {
						return null;
					}
					SavedSlotsConfig config = new SavedSlotsConfig();
					config.slots = new java.util.ArrayList<>(count);
					for (int i = 0; i < count; i++) {
						config.slots.add(payloadIn.readInt());
					}
					return config;
				}
			}
		} catch (IOException ignored) {
			return null;
		}
	}

	private static void writeSavedSlotsBin(Path path, SavedSlotsConfig config) {
		try {
			ByteArrayOutputStream payloadOut = new ByteArrayOutputStream();
			try (DataOutputStream data = new DataOutputStream(payloadOut)) {
				java.util.List<Integer> slots = config.slots == null ? java.util.List.of() : config.slots;
				data.writeInt(slots.size());
				for (int i = 0; i < slots.size(); i++) {
					data.writeInt(slots.get(i));
				}
			}
			byte[] payload = payloadOut.toByteArray();
			CRC32 crc32 = new CRC32();
			crc32.update(payload);
			ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
			try (DataOutputStream out = new DataOutputStream(rawOut)) {
				out.write(SLOTS_MAGIC);
				out.writeInt(SLOTS_VERSION);
				out.writeInt(payload.length);
				out.write(payload);
				out.writeLong(crc32.getValue());
			}
			byte[] encoded = xor(rawOut.toByteArray(), SETTINGS_XOR_KEY);
			Files.write(path, encoded);
		} catch (IOException ignored) {
		}
	}
}
