package io.github.lucaargolo.extragenerators.compat

import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryStacks
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.text.LiteralText
import net.minecraft.text.TextColor
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3f
import java.awt.Color

class ThermoelectricGeneratorCategory(private val id: String, private val block: Block): DisplayCategory<ThermoelectricGeneratorCategory.RecipeDisplay> {

    init { set.add(this) }

    override fun getCategoryIdentifier(): CategoryIdentifier<RecipeDisplay> = CategoryIdentifier.of(ModIdentifier(id))

    override fun getIcon(): Renderer = EntryStacks.of(block)

    override fun getTitle() = TranslatableText(block.translationKey)

    @Suppress("DEPRECATION")
    override fun setupDisplay(display: RecipeDisplay, bounds: Rectangle): MutableList<Widget> {
        val widgets = mutableListOf<Widget>()

        widgets.add(Widgets.createCategoryBase(bounds))

        widgets.add(Widgets.createDrawableWidget { _, m, _, _, _ -> m.scale(2f, 2f, 1f)})
        widgets.add(Widgets.createSlot(Point(bounds.x/2 + 3, bounds.y/2 + 3)).entry(EntryStacks.of(block)).disableBackground().disableHighlight().disableTooltips())
        widgets.add(Widgets.createDrawableWidget { _, m, _, _, _ -> m.scale(0.5f, 0.5f, 1f)})

        val baseColor = Triple(1f, 1f, 1f)

        val coldColor = Triple(0f, 1f, 1f)
        val hotColor = Triple(1f, 0.4f, 0f)

        val finalColor = if(display.temperature < 0) coldColor else hotColor
        val delta = MathHelper.clamp(if(display.temperature < 0) MathHelper.abs(display.temperature)/50f else display.temperature/1200f, 0f, 1f)

        val red = MathHelper.lerp(delta, baseColor.first, finalColor.first)
        val green = MathHelper.lerp(delta, baseColor.second, finalColor.second)
        val blue = MathHelper.lerp(delta, baseColor.third, finalColor.third)

        val text = LiteralText("${display.temperature} ºC")
        val textColor = TextColor.fromRgb(Color(red, green, blue).rgb)
        val style = text.style.withColor(textColor)
        text.style = style

        widgets.add(Widgets.createLabel(Point(bounds.x+75, bounds.y+18), text))

        widgets.add(Widgets.createDrawableWidget { draw, matrices, _, _, _ ->
            matrices.push()
            matrices.translate(bounds.x + 143.0, bounds.y + 31.0, draw.zOffset + 100.0)
            matrices.scale(33f, -33f, 1f)
            val client = MinecraftClient.getInstance()
            val immediate = client.bufferBuilders.entityVertexConsumers
            val model = client.bakedModelManager.getModel(ModelIdentifier("minecraft:stone#"))
            model.transformation.gui.apply(false, matrices)
            val fluidState = display.block.getFluidState(display.block.defaultState)
            if(fluidState.isEmpty) {
                client.blockRenderManager.renderBlockAsEntity(display.block.defaultState, matrices, immediate, 15728880, OverlayTexture.DEFAULT_UV)
            }else{
                val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.fluid)
                val fluidColor = fluidRenderHandler.getFluidColor(client.world, client.player?.blockPos ?: BlockPos.ORIGIN, fluidState)
                val sprite = fluidRenderHandler.getFluidSprites(client.world, client.player?.blockPos ?: BlockPos.ORIGIN, fluidState)[0]
                val entry = matrices.peek()
                val normal = Direction.UP.unitVector
                val vertexConsumer = immediate.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))
                renderFluidVertices(vertexConsumer, entry, normal, fluidColor, sprite, 0f, 1f, 1f, 0f, 0f, 0f, 0f, 0f)
                renderFluidVertices(vertexConsumer, entry, normal, fluidColor, sprite, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f)
                renderFluidVertices(vertexConsumer, entry, normal, fluidColor, sprite, 0f, 1f, 1f, 1f, 1f, 1f, 0f, 0f)
            }
            client.bufferBuilders.entityVertexConsumers.draw()
            matrices.pop()
        })

        return widgets
    }

    @Suppress("SameParameterValue")
    private fun renderFluidVertices(bb: VertexConsumer, entry: MatrixStack.Entry, normal: Vec3f, fluidColor: Int, fluidSprite: Sprite, f: Float, g: Float, h: Float, i: Float, j: Float, k: Float, l: Float, m: Float) {
        bb.vertex(entry.model, f, h, j).color((fluidColor shr 16 and 255)/255f, (fluidColor shr 8 and 255)/255f, (fluidColor and 255)/255f, 1f).texture(fluidSprite.maxU, fluidSprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, g, h, k).color((fluidColor shr 16 and 255)/255f, (fluidColor shr 8 and 255)/255f, (fluidColor and 255)/255f, 1f).texture(fluidSprite.minU, fluidSprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, g, i, l).color((fluidColor shr 16 and 255)/255f, (fluidColor shr 8 and 255)/255f, (fluidColor and 255)/255f, 1f).texture(fluidSprite.minU, fluidSprite.maxV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(entry.normal, normal.x, normal.y, normal.z).next()
        bb.vertex(entry.model, f, i, m).color((fluidColor shr 16 and 255)/255f, (fluidColor shr 8 and 255)/255f, (fluidColor and 255)/255f, 1f).texture(fluidSprite.maxU, fluidSprite.maxV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(entry.normal, normal.x, normal.y, normal.z).next()
    }

    override fun getDisplayHeight() = 44

    fun createDisplay(block: Block, temperature: Int) = RecipeDisplay(categoryIdentifier, block, temperature)

    class RecipeDisplay(private val category: CategoryIdentifier<RecipeDisplay>, val block: Block, val temperature: Int): Display {

        override fun getInputEntries() = mutableListOf(EntryIngredient.of(EntryStacks.of(block)))

        override fun getOutputEntries() = mutableListOf<EntryIngredient>()

        override fun getCategoryIdentifier() = category

    }

    companion object {
        private val set = linkedSetOf<ThermoelectricGeneratorCategory>()

        fun register(registry: CategoryRegistry) = set.forEach {
            registry.add(it)
            registry.addWorkstations(it.categoryIdentifier, EntryStacks.of(it.block))
            registry.removePlusButton(it.categoryIdentifier)
        }

    }


}