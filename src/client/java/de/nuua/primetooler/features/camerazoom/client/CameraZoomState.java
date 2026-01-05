package de.nuua.primetooler.features.camerazoom.client;

/**
 * WHY: Centralize third-person zoom state and tuning.
 * PERF: Static state, updated only on scroll input.
 */
public final class CameraZoomState {
	private static final float DEFAULT_DISTANCE = 4.0f;
	private static final float MIN_DISTANCE = DEFAULT_DISTANCE * 0.5f;
	private static final float MAX_DISTANCE = DEFAULT_DISTANCE * 10.0f;
	private static final float SCROLL_STEP = 0.6f;
	private static final float DISTANCE_ACCEL = 0.07f;

	private static float distance = DEFAULT_DISTANCE;
	private static boolean enabled;

	private CameraZoomState() {
	}

	public static boolean applyScroll(double scrollY) {
		if (scrollY == 0.0) {
			return false;
		}
		float step = SCROLL_STEP + (distance * DISTANCE_ACCEL);
		float next = distance - (float) (scrollY * step);
		if (next < MIN_DISTANCE) {
			next = MIN_DISTANCE;
		}
		if (next > MAX_DISTANCE) {
			next = MAX_DISTANCE;
		}
		distance = next;
		return true;
	}

	public static boolean isAtMin() {
		return distance <= MIN_DISTANCE + 0.0001f;
	}

	public static float getMinDistance() {
		return MIN_DISTANCE;
	}

	public static float getMaxDistance() {
		return MAX_DISTANCE;
	}

	public static float getDistance() {
		return distance;
	}

	public static void reset() {
		distance = DEFAULT_DISTANCE;
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
}
