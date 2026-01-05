package de.nuua.primetooler.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * WHY: Read the current bound key for input simulation (AutoAngelSystem).
 * PERF: Accessor only; avoids reflection per trigger.
 */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
	@Accessor("key")
	InputConstants.Key primetooler$getKey();
}

