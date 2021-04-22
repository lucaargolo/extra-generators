package io.github.lucaargolo.extragenerators.compat

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeCategory
import me.shedaniel.rei.api.RecipeDisplay
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.widgets.Widgets
import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.client.resource.language.I18n
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class ItemGeneratorCategory(private val id: String, private val block: Block): RecipeCategory<ItemGeneratorCategory.Display> {

    init { set.add(this) }

    override fun getIdentifier() = ModIdentifier(id)

    override fun getLogo(): EntryStack = EntryStack.create(block)

    override fun getCategoryName(): String = I18n.translate(block.translationKey)

    @Suppress("deprecation")
    override fun setupDisplay(display: Display, bounds: Rectangle): MutableList<Widget> {
        val widgets = mutableListOf<Widget>()

        widgets.add(Widgets.createCategoryBase(bounds))

        widgets.add(Widgets.createDrawableWidget { _, _, _, _, _ ->
            RenderSystem.scalef(2f, 2f, 1f)
            MinecraftClient.getInstance().itemRenderer.renderInGui(ItemStack(block), bounds.x/2 + 3, bounds.y/2 + 3)
            RenderSystem.scalef(0.5f, 0.5f, 1f)
        })

        widgets.add(Widgets.createBurningFire(Point(bounds.x+44, bounds.y+4)).animationDurationTicks(display.output.burnTime.toDouble()))
        widgets.add(Widgets.createSlot(Point(bounds.x+44, bounds.y+22)).entries(display.input))
        widgets.add(Widgets.createLabel(Point(bounds.x+67, bounds.y+8), TranslatableText("screen.extragenerators.rei.energy_output")).leftAligned())
        widgets.add(Widgets.createLabel(Point(bounds.x+145, bounds.y+26), Text.of("${display.output.energyOutput} E")).rightAligned())

        widgets.add(Widgets.createDrawableWidget { _, matrices, mouseX, mouseY, _ ->
            val tooltip = listOf(
                TranslatableText("screen.extragenerators.rei.burn_time", display.output.burnTime),
                TranslatableText("screen.extragenerators.rei.burn_rate", display.output.energyOutput/display.output.burnTime)
            )
            if(mouseX in (bounds.x+44..bounds.x+62) && mouseY in (bounds.y+4..bounds.y+22)) {
                MinecraftClient.getInstance().currentScreen?.renderTooltip(matrices, tooltip, mouseX, mouseY)
            }
        })

        return widgets
    }

    override fun getDisplayHeight() = 44

    fun createDisplay(input: MutableList<EntryStack>, output: GeneratorFuel) = Display(identifier, input, output)

    class Display(private val category: Identifier, val input: MutableList<EntryStack>, val output: GeneratorFuel): RecipeDisplay {

        override fun getInputEntries() = mutableListOf(input)

        override fun getRecipeCategory() = category

    }

    companion object {
        private val set = linkedSetOf<ItemGeneratorCategory>()

        fun registerCategories(recipeHelper: RecipeHelper) = set.forEach { recipeHelper.registerCategory(it) }

        fun registerOthers(recipeHelper: RecipeHelper) = set.forEach {
            recipeHelper.removeAutoCraftButton(it.identifier)
            recipeHelper.registerWorkingStations(it.identifier, EntryStack.create(it.block))
        }
    }


}