package de.nuua.primetooler.api.v1.client.text;

/**
 * WHY: Bundle rainbow text parameters for easy reuse and tuning.
 * PERF: Plain record, no allocations beyond caller-owned instance.
 */
public record RainbowTextStyle(
	float speed,
	float saturation,
	float brightness,
	float alpha,
	float wavelengthPx,
	boolean shadow
) {
	public static RainbowTextStyle defaults() {
		return new RainbowTextStyle(0.12f, 0.95f, 1.0f, 1.0f, 140.0f, true);
	}
}
