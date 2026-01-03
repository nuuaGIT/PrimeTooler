package de.nuua.primetooler.core.config;

public final class ChatInputsConfig {
	public String[] lines;
	public long[] cooldownEnds;

	public ChatInputsConfig() {
		this.lines = new String[0];
		this.cooldownEnds = new long[0];
	}

	public ChatInputsConfig(String[] lines) {
		this.lines = lines;
		this.cooldownEnds = new long[0];
	}

	public ChatInputsConfig(String[] lines, long[] cooldownEnds) {
		this.lines = lines;
		this.cooldownEnds = cooldownEnds;
	}
}
