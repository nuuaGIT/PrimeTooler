package de.nuua.primetooler.core.lifecycle;

/**
 * Module lifecycle: deterministic, side-effect free constructors.
 */
public interface Module {
	String id();

	void preInit(Bootstrap ctx);

	void init(Bootstrap ctx);

	void postInit(Bootstrap ctx);
}
