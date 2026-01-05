package de.nuua.primetooler.api.v1.client.tracking;

/**
 * WHY: Shared, pause-aware per-hour tracking for monotonically increasing (or wrapping) counters.
 * PERF: No allocations; manual arithmetic; intended for low-frequency polling (e.g., 0.5s).
 */
public final class PerHourTracker {
	public enum DecreaseMode {
		/**
		 * Treat a decrease as a full restart (clears gained value and resets the timer).
		 */
		RESTART,
		/**
		 * Treat a decrease as a baseline shift (keeps gained value, but doesn't add negative diffs).
		 */
		BASELINE,
		/**
		 * Treat a decrease as a wrap-around using {@code wrapAt} (e.g., progress bar "current/max").
		 * Falls back to {@link #BASELINE} if {@code wrapAt} is unavailable/invalid.
		 */
		WRAP
	}

	private static final long DEFAULT_IDLE_PAUSE_NANOS = 30_000_000_000L;
	private static final long DEFAULT_UI_UPDATE_NANOS = 3_000_000_000L;

	private final DecreaseMode decreaseMode;
	private final long idlePauseNanos;
	private final long uiUpdateNanos;

	private long startNanos;
	private long pausedSinceNanos;
	private long pausedAccumNanos;
	private long lastUiUpdateNanos;
	private long lastActivityNanos;

	private long observedValue;
	private long gainedValue;
	private long perHourValue;
	private boolean paused;

	public PerHourTracker(DecreaseMode decreaseMode) {
		this(decreaseMode, DEFAULT_IDLE_PAUSE_NANOS, DEFAULT_UI_UPDATE_NANOS);
	}

	public PerHourTracker(DecreaseMode decreaseMode, long idlePauseNanos, long uiUpdateNanos) {
		this.decreaseMode = decreaseMode == null ? DecreaseMode.RESTART : decreaseMode;
		this.idlePauseNanos = idlePauseNanos <= 0L ? DEFAULT_IDLE_PAUSE_NANOS : idlePauseNanos;
		this.uiUpdateNanos = uiUpdateNanos <= 0L ? DEFAULT_UI_UPDATE_NANOS : uiUpdateNanos;
	}

	public void reset() {
		startNanos = 0L;
		pausedSinceNanos = 0L;
		pausedAccumNanos = 0L;
		lastUiUpdateNanos = 0L;
		lastActivityNanos = 0L;
		observedValue = 0L;
		gainedValue = 0L;
		perHourValue = 0L;
		paused = false;
	}

	public boolean isStarted() {
		return startNanos != 0L;
	}

	public boolean isPaused() {
		return paused;
	}

	public long perHourValue() {
		return perHourValue;
	}

	/**
	 * Updates tracker state.
	 *
	 * @param wrapAt maximum value before wrap (only used for {@link DecreaseMode#WRAP})
	 * @param extraActivity additional activity signal (e.g., "fish count increased")
	 * @return {@code true} when UI text should be refreshed (start/restart, pause/resume, or rate recomputed)
	 */
	public boolean tick(long nowNanos, long currentValue, long wrapAt, boolean extraActivity) {
		if (nowNanos <= 0L) {
			return false;
		}
		if (startNanos == 0L) {
			startNanos = nowNanos;
			lastActivityNanos = nowNanos;
			observedValue = currentValue;
			gainedValue = 0L;
			perHourValue = 0L;
			pausedSinceNanos = 0L;
			pausedAccumNanos = 0L;
			lastUiUpdateNanos = 0L;
			paused = false;
			return true;
		}

		long diff = currentValue - observedValue;
		if (diff < 0L) {
			if (decreaseMode == DecreaseMode.RESTART) {
				reset();
				startNanos = nowNanos;
				lastActivityNanos = nowNanos;
				observedValue = currentValue;
				perHourValue = 0L;
				return true;
			}
			if (decreaseMode == DecreaseMode.WRAP) {
				long wrapDiff = computeWrapDiff(currentValue, wrapAt);
				if (wrapDiff > 0L) {
					diff = wrapDiff;
				} else {
					// Fall back to baseline shift if wrap isn't usable.
					observedValue = currentValue;
					diff = 0L;
				}
			} else {
				observedValue = currentValue;
				diff = 0L;
			}
		}

		boolean activity = extraActivity;
		if (diff > 0L) {
			activity = true;
			if (gainedValue <= Long.MAX_VALUE - diff) {
				gainedValue += diff;
			} else {
				gainedValue = Long.MAX_VALUE;
			}
			observedValue = currentValue;
		}

		if (activity) {
			lastActivityNanos = nowNanos;
			if (pausedSinceNanos != 0L) {
				pausedAccumNanos += nowNanos - pausedSinceNanos;
				pausedSinceNanos = 0L;
				lastUiUpdateNanos = 0L;
				paused = false;
				return true;
			}
		}

		if (pausedSinceNanos == 0L && nowNanos - lastActivityNanos >= idlePauseNanos) {
			pausedSinceNanos = nowNanos;
			paused = true;
			return true;
		}
		if (pausedSinceNanos != 0L) {
			return false;
		}

		if (lastUiUpdateNanos != 0L && nowNanos - lastUiUpdateNanos < uiUpdateNanos) {
			return false;
		}
		lastUiUpdateNanos = nowNanos;

		long elapsedActive = nowNanos - startNanos - pausedAccumNanos;
		if (elapsedActive <= 0L || gainedValue <= 0L) {
			perHourValue = 0L;
			return true;
		}
		double perHour = (double) gainedValue * 3_600_000_000_000.0 / (double) elapsedActive;
		if (!Double.isFinite(perHour) || perHour <= 0.0) {
			perHourValue = 0L;
			return true;
		}
		long rounded = Math.round(perHour);
		if (rounded < 0L) {
			rounded = 0L;
		}
		perHourValue = rounded;
		return true;
	}

	private long computeWrapDiff(long currentValue, long wrapAt) {
		if (wrapAt <= 0L) {
			return -1L;
		}
		if (observedValue < 0L || currentValue < 0L) {
			return -1L;
		}
		// Accept wrap only if both values appear to be within the same [0..wrapAt] range.
		if (observedValue > wrapAt || currentValue > wrapAt) {
			return -1L;
		}
		long remaining = wrapAt - observedValue;
		if (remaining < 0L) {
			return -1L;
		}
		if (remaining > Long.MAX_VALUE - currentValue) {
			return Long.MAX_VALUE;
		}
		return remaining + currentValue;
	}
}

