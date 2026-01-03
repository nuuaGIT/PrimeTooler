package de.nuua.primetooler.features.resourcepackguard.client;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.server.DownloadedPackSource;

/**
 * WHY: Session toggle + cache for server pack data so we can block and reapply.
 * PERF: Simple list + linear scans, only touched on pack events/toggles.
 */
public final class ResourcePackGuardState {
	private static final List<CachedPack> cachedPacks = new ArrayList<>();
	private static boolean enabled;
	private static boolean guardUnloading;
	private static boolean replaying;

	private ResourcePackGuardState() {
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static boolean toggleEnabled() {
		enabled = !enabled;
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}

	public static void applyClientState(boolean enabled) {
		Minecraft client = Minecraft.getInstance();
		DownloadedPackSource source = client.getDownloadedPackSource();
		if (source == null) {
			return;
		}
		if (enabled) {
			beginGuardUnload();
			source.rejectServerPacks();
			source.popAll();
			endGuardUnload();
		} else {
			source.allowServerPacks();
			replayCached(source);
		}
	}

	public static void cacheRemote(UUID id, URL url, String sha1) {
		if (id == null || url == null) {
			return;
		}
		remove(id);
		cachedPacks.add(CachedPack.remote(id, url, sha1));
	}

	public static void cacheLocal(UUID id, Path path) {
		if (id == null || path == null) {
			return;
		}
		remove(id);
		cachedPacks.add(CachedPack.local(id, path));
	}

	public static void remove(UUID id) {
		if (id == null || cachedPacks.isEmpty()) {
			return;
		}
		for (int i = cachedPacks.size() - 1; i >= 0; i--) {
			if (id.equals(cachedPacks.get(i).id)) {
				cachedPacks.remove(i);
				return;
			}
		}
	}

	public static void clear() {
		cachedPacks.clear();
	}

	public static boolean isGuardUnloading() {
		return guardUnloading;
	}

	public static void beginGuardUnload() {
		guardUnloading = true;
	}

	public static void endGuardUnload() {
		guardUnloading = false;
	}

	public static boolean isReplaying() {
		return replaying;
	}

	private static void beginReplay() {
		replaying = true;
	}

	private static void endReplay() {
		replaying = false;
	}

	private static void replayCached(DownloadedPackSource source) {
		if (cachedPacks.isEmpty()) {
			return;
		}
		beginReplay();
		for (int i = 0; i < cachedPacks.size(); i++) {
			CachedPack pack = cachedPacks.get(i);
			if (pack.local) {
				source.pushLocalPack(pack.id, pack.path);
			} else {
				source.pushPack(pack.id, pack.url, pack.sha1);
			}
		}
		endReplay();
	}

	private static final class CachedPack {
		private final UUID id;
		private final URL url;
		private final String sha1;
		private final Path path;
		private final boolean local;

		private CachedPack(UUID id, URL url, String sha1, Path path, boolean local) {
			this.id = id;
			this.url = url;
			this.sha1 = sha1;
			this.path = path;
			this.local = local;
		}

		private static CachedPack remote(UUID id, URL url, String sha1) {
			return new CachedPack(id, url, sha1, null, false);
		}

		private static CachedPack local(UUID id, Path path) {
			return new CachedPack(id, null, null, path, true);
		}
	}
}
