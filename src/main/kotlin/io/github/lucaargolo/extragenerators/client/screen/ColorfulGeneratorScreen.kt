package io.github.lucaargolo.extragenerators.client.screen

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.extragenerators.common.blockentity.ColorfulGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.ColorfulGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper

class ColorfulGeneratorScreen(handler: ColorfulGeneratorScreenHandler, inventory: PlayerInventory, title: Text): AbstractGeneratorScreen<ColorfulGeneratorScreenHandler, ColorfulGeneratorBlockEntity>(handler, inventory, title) {

    private val texture = ModIdentifier("textures/gui/colorful_generator.png")

    override fun init() {
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
        if((x+25..x+33).contains(mouseX) && (y+17..y+69).contains(mouseY)) {
            val a = TranslatableText("screen.extragenerators.common.stored_energy").append(": ").formatted(Formatting.RED)
            val b = LiteralText("%.0f/%.0f E".format(handler.energyStored, handler.entity.maxStoredPower)).formatted(Formatting.GRAY)
            renderTooltip(matrices, listOf(a, b), mouseX, mouseY)
        }
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderTexture(0, texture)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        val energyPercentage = handler.energyStored/handler.entity.maxStoredPower
        val energyOffset = MathHelper.lerp(energyPercentage, 0.0, 52.0).toInt()
        drawTexture(matrices, x+25, y+17+(52-energyOffset), 176, 52-energyOffset, 8, energyOffset)
        handler.burningFuel?.let {
            val p = (it.currentBurnTime * 13f /it.burnTime).toInt()
            drawTexture(matrices, x+45, y+37+(12-p), 184, 12-p, 14, p+1)
            drawTexture(matrices, x+81, y+37+(12-p), 198, 12-p, 14, p+1)
            drawTexture(matrices, x+117, y+37+(12-p), 212, 12-p, 14, p+1)
        }

    }

}