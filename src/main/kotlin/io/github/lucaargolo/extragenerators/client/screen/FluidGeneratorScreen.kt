package io.github.lucaargolo.extragenerators.client.screen

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import io.github.lucaargolo.extragenerators.common.blockentity.FluidGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.FluidGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.common.containers.ItemGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper

class FluidGeneratorScreen(handler: FluidGeneratorScreenHandler, inventory: PlayerInventory, title: Text): AbstractGeneratorScreen<FluidGeneratorScreenHandler, FluidGeneratorBlockEntity>(handler, inventory, title) {

    private val texture = ModIdentifier("textures/gui/fluid_generator.png")

    override fun init() {
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
        if((x+25..x+33).contains(mouseX) && (y+17..y+69).contains(mouseY)) {
            val a = TranslatableText("screen.extragenerators.common.stored_energy").append(": ")
            val b = LiteralText("${handler.energyStored}/${handler.entity.maxStoredPower} E")
            renderTooltip(matrices, listOf(a, b), mouseX, mouseY)
        }
        if((x+134..x+150).contains(mouseX) && (y+17..y+69).contains(mouseY)) {
            val tank = handler.entity.fluidInv.getTank(0)
            val volume = tank.get()
            val stored = volume.amount()
            val capacity = tank.maxAmount_F
            renderTooltip(matrices, listOf(if(volume.isEmpty) TranslatableText("tooltip.extragenerators.empty") else volume.name, LiteralText("${stored.asInt(1000)} / ${capacity.asInt(1000)} mB").formatted(Formatting.GRAY)), mouseX, mouseY)
        }
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client?.textureManager?.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        val energyPercentage = handler.energyStored/handler.entity.maxStoredPower
        val energyOffset = MathHelper.lerp(energyPercentage, 0.0, 52.0).toInt()
        drawTexture(matrices, x+25, y+17+(52-energyOffset), 176, 0, 8, energyOffset)
        handler.burningFuel?.let {
            val p = (it.burnTime * 13f /it.totalBurnTime).toInt()
            drawTexture(matrices, x+81, y+37+(12-p), 184, 12-p, 14, p+1)
        }
        handler.fluidVolume?.let {
            if(!it.isEmpty) {
                val a = it.amount().asInt(1000).toDouble()
                val b = handler.entity.fluidInv.getMaxAmount_F(0).asInt(1000).toDouble()
                val fluidPercentage = a/b
                val fluidOffset = MathHelper.lerp(fluidPercentage, 0.0, 52.0)
                it.renderGuiRect(x + 134.0, y + 17.0 + 52.0 - fluidOffset, x+150.0, y + 69.0)
            }
        }
        client?.textureManager?.bindTexture(texture)
        drawTexture(matrices, x+134, y+17, 198, 0, 16, 52)
    }

}