package de.nuua.primetooler.mixin.client;

import de.nuua.primetooler.features.inventorycalc.client.Calculator;
import de.nuua.primetooler.features.inventorycalc.client.InventoryCalculatorState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class InventoryScreenMixin {
	@Shadow
	protected int leftPos;
	@Shadow
	protected int topPos;
	@Shadow
	protected int imageWidth;
	@Shadow
	protected int imageHeight;

	@Unique
	private EditBox primetooler$calcInput;
	@Unique
	private String primetooler$calcResult;
	@Unique
	private int primetooler$calcX;
	@Unique
	private int primetooler$calcY;

	@Inject(method = "init", at = @At("TAIL"))
	private void primetooler$initCalculator(CallbackInfo ci) {
		if (!((Object) this instanceof InventoryScreen)) {
			return;
		}
		if (!InventoryCalculatorState.isEnabled()) {
			return;
		}
		primetooler$calcX = leftPos;
		primetooler$calcY = topPos + imageHeight + 4;
		Font font = ((ScreenAccessor) (Object) this).primetooler$getFont();
		primetooler$calcInput = new EditBox(
			font,
			primetooler$calcX,
			primetooler$calcY,
			imageWidth,
			18,
			Component.empty()
		);
		primetooler$calcInput.setMaxLength(64);
		primetooler$calcInput.setHint(Component.literal("Type Calculation...").withStyle(ChatFormatting.GRAY));
		primetooler$calcInput.setResponder(this::primetooler$onCalcChanged);
		primetooler$calcResult = null;
		((ScreenAccessor) (Object) this).primetooler$addRenderableWidget(primetooler$calcInput);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void primetooler$renderCalcResult(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (!((Object) this instanceof InventoryScreen)) {
			return;
		}
		if (!InventoryCalculatorState.isEnabled()) {
			return;
		}
		primetooler$updateCalcLayout();
		if (primetooler$calcInput == null || primetooler$calcResult == null) {
			return;
		}
		Font font = ((ScreenAccessor) (Object) this).primetooler$getFont();
		String text = primetooler$calcInput.getValue();
		int eqIndex = text.indexOf('=');
		if (eqIndex < 0) {
			return;
		}
		String prefix = text.substring(0, eqIndex + 1);
		int x = primetooler$calcX + 4 + font.width(prefix);
		int y = primetooler$calcY + (18 - font.lineHeight) / 2;
		graphics.drawString(font, primetooler$calcResult, x, y, 0xFF00FF5A, false);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void primetooler$acceptCalc(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!((Object) this instanceof InventoryScreen)) {
			return;
		}
		if (!InventoryCalculatorState.isEnabled()) {
			return;
		}
		if (primetooler$calcInput == null || !primetooler$calcInput.isFocused()) {
			return;
		}
		if (event.key() != GLFW.GLFW_KEY_ENTER && event.key() != GLFW.GLFW_KEY_KP_ENTER) {
			return;
		}
		if (primetooler$calcResult == null) {
			String raw = primetooler$calcInput.getValue().trim();
			if (raw.isEmpty()) {
				return;
			}
			Double result = Calculator.tryEvaluate(raw);
			if (result == null) {
				return;
			}
			primetooler$calcInput.setValue(Calculator.format(result));
			primetooler$calcInput.moveCursorToEnd(false);
			cir.setReturnValue(true);
			return;
		}
		primetooler$calcInput.setValue(primetooler$calcResult);
		primetooler$calcInput.moveCursorToEnd(false);
		cir.setReturnValue(true);
	}

	@Unique
	private void primetooler$onCalcChanged(String value) {
		int eqIndex = value.indexOf('=');
		if (eqIndex < 0) {
			primetooler$calcResult = null;
			return;
		}
		String expr = value.substring(0, eqIndex).trim();
		if (expr.isEmpty()) {
			primetooler$calcResult = null;
			return;
		}
		Double result = Calculator.tryEvaluate(expr);
		if (result == null) {
			primetooler$calcResult = null;
			return;
		}
		primetooler$calcResult = Calculator.format(result);
	}

	@Unique
	private void primetooler$updateCalcLayout() {
		if (primetooler$calcInput == null) {
			return;
		}
		primetooler$calcX = leftPos;
		primetooler$calcY = topPos + imageHeight + 4;
		primetooler$calcInput.setX(primetooler$calcX);
		primetooler$calcInput.setY(primetooler$calcY);
	}
}
