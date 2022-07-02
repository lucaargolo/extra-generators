package io.github.lucaargolo.extragenerators.client.screen

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.common.blockentity.InfiniteGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.InfiniteGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper
import kotlin.math.max

class InfiniteGeneratorScreen(handler: InfiniteGeneratorScreenHandler, inventory: PlayerInventory, title: Text): AbstractGeneratorScreen<InfiniteGeneratorScreenHandler, InfiniteGeneratorBlockEntity>(handler, inventory, title) {

    private val texture = ModIdentifier("textures/gui/infinite_generator.png")

    override fun init() {
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
        if((x+25..x+33).contains(mouseX) && (y+17..y+69).contains(mouseY)) {
            val a = Text.translatable("screen.extragenerators.common.stored_energy").append(": ").formatted(Formatting.RED)
            val b = Text.literal("%d/%d E".format(handler.energyStored, handler.entity.energyStorage.getCapacity())).formatted(Formatting.GRAY)
            renderTooltip(matrices, listOf(a, b), mouseX, mouseY)
        }
        var itemX = 39
        var itemY = 16
        BlockCompendium.generatorIdentifierMap().forEach { (identifier, block) ->
            val qnt = handler.activeGenerators.getOrDefault(identifier, 0)
            val stack = ItemStack(block, max(1, qnt))
            if(qnt == 0) {
                itemRenderer.renderInGui(stack, x+itemX, y+itemY)
                matrices.translate(0.0, 0.0, 200.0)
                fill(matrices, x+itemX, y+itemY, x+itemX + 18, y+itemY + 18, 0xC6C6C6C6.toInt())
                matrices.translate(0.0, 0.0, -200.0)
            }else{
                itemRenderer.renderInGui(stack, x+itemX, y+itemY)
                itemRenderer.renderGuiItemOverlay(textRenderer, stack, x+itemX, y+itemY)
            }
            if(mouseX in (x+itemX..x+itemX+17) && mouseY in (y+itemY..y+itemY+17)) {
                renderTooltip(matrices, stack, mouseX, mouseY)
            }
            itemX += 18
            if(itemX >= 147) {
                itemX = 39
                itemY += 18
            }
        }
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderTexture(0, texture)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        val energyPercentage = handler.energyStored/handler.entity.energyStorage.getCapacity().toFloat()
        val energyOffset = MathHelper.lerp(energyPercentage, 0F, 52F).toInt()
        drawTexture(matrices, x+25, y+17+(52-energyOffset), 176, 52-energyOffset, 8, energyOffset)
    }

}