package io.github.lucaargolo.extragenerators.client.screen

import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.ItemGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.MathHelper

class ItemGeneratorScreen(handler: ItemGeneratorScreenHandler, inventory: PlayerInventory, title: Text): AbstractGeneratorScreen<ItemGeneratorScreenHandler, ItemGeneratorBlockEntity>(handler, inventory, title) {

    private val texture = ModIdentifier("textures/gui/item_generator.png")

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
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client?.textureManager?.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        val energyPercentage = handler.energyStored/handler.entity.maxStoredPower
        val energyOffset = MathHelper.lerp(energyPercentage, 0.0, 52.0).toInt()
        drawTexture(matrices, x+25, y+17+(52-energyOffset), 176, 52-energyOffset, 8, energyOffset)
        handler.burningFuel?.let {
            val p = (it.burnTime * 13f /it.totalBurnTime).toInt()
            drawTexture(matrices, x+81, y+37+(12-p), 184, 12-p, 14, p+1)
        }

    }

}