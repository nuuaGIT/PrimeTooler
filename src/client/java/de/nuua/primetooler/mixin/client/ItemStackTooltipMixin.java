package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.checkitem.client.CheckItemClientModule;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WHY: Show saved marker at top of tooltip while keeping marker lines hidden when enabled.
 * PERF: Small list copy only when tooltip is requested.
 * ALT: Lore rendering order is fixed in vanilla; tooltip injection is required.
 */
@Mixin(ItemStack.class)
public class ItemStackTooltipMixin {
	@Inject(
		method = "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
		at = @At("RETURN"),
		cancellable = true
	)
	private void primetooler$injectSavedTooltip(Item.TooltipContext context, Player player, TooltipFlag flag,
		CallbackInfoReturnable<List<Component>> cir) {
		if (!CheckItemClientModule.isItemSaveEnabled()) {
			return;
		}
		List<Component> original = cir.getReturnValue();
		if (original == null) {
			return;
		}
		ItemStack stack = (ItemStack) (Object) this;
		boolean saved = CheckItemClientModule.isItemSaved(stack);
		ArrayList<Component> filtered = new ArrayList<>(original.size() + (saved ? 1 : 0));
		for (int i = 0; i < original.size(); i++) {
			Component line = original.get(i);
			if (CheckItemClientModule.isLocalMarkerLine(line)) {
				continue;
			}
			if (CheckItemClientModule.isSavedTooltipLine(line)) {
				continue;
			}
			filtered.add(line);
		}
		if (saved) {
			Component savedLine = CheckItemClientModule.savedTooltipLine();
			int insertIndex = filtered.isEmpty() ? 0 : 1;
			filtered.add(insertIndex, savedLine);
		}
		cir.setReturnValue(filtered);
	}
}
