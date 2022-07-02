package io.github.lucaargolo.extragenerators.compat

import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
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
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

class ColorfulGeneratorCategory(private val id: String, private val block: Block): DisplayCategory<ColorfulGeneratorCategory.RecipeDisplay> {

    init { set.add(this) }

    override fun getCategoryIdentifier(): CategoryIdentifier<RecipeDisplay> = CategoryIdentifier.of(ModIdentifier(id))

    override fun getIcon(): Renderer = EntryStacks.of(block)

    override fun getTitle() = Text.translatable(block.translationKey)

    override fun setupDisplay(display: RecipeDisplay, bounds: Rectangle): MutableList<Widget> {
        val widgets = mutableListOf<Widget>()

        widgets.add(Widgets.createCategoryBase(bounds))

        widgets.add(Widgets.createDrawableWidget { _, m, _, _, _ -> m.scale(2f, 2f, 1f)})
        widgets.add(Widgets.createSlot(Point(bounds.x/2 + 3, bounds.y/2 + 3)).entry(EntryStacks.of(block)).disableBackground().disableHighlight().disableTooltips())
        widgets.add(Widgets.createDrawableWidget { _, m, _, _, _ -> m.scale(0.5f, 0.5f, 1f)})

        widgets.add(Widgets.createBurningFire(Point(bounds.x+44, bounds.y+4)).animationDurationTicks(display.output.burnTime.toDouble()))
        widgets.add(Widgets.createSlot(Point(bounds.x+44, bounds.y+22)).entries(display.redInput))
        widgets.add(Widgets.createSlot(Point(bounds.x+62, bounds.y+22)).entries(display.greenInput))
        widgets.add(Widgets.createSlot(Point(bounds.x+80, bounds.y+22)).entries(display.blueInput))

        widgets.add(Widgets.createLabel(Point(bounds.x+67, bounds.y+8), Text.translatable("screen.extragenerators.rei.energy_output")).leftAligned())
        widgets.add(Widgets.createLabel(Point(bounds.x+145, bounds.y+26), Text.of("${display.output.energyOutput} E")).rightAligned())

        widgets.add(Widgets.createDrawableWidget { _, matrices, mouseX, mouseY, _ ->
            val tooltip = listOf(
                Text.translatable("screen.extragenerators.rei.burn_time", display.output.burnTime),
                Text.translatable("screen.extragenerators.rei.burn_rate", MathHelper.floor(display.output.energyOutput/display.output.burnTime))
            )
            if(mouseX in (bounds.x+44..bounds.x+62) && mouseY in (bounds.y+4..bounds.y+22)) {
                MinecraftClient.getInstance().currentScreen?.renderTooltip(matrices, tooltip, mouseX, mouseY)
            }
        })

        return widgets
    }

    override fun getDisplayHeight() = 44

    fun createDisplay(redInput: List<EntryStack<ItemStack>>, greenInput: List<EntryStack<ItemStack>>, blueInput: List<EntryStack<ItemStack>>, output: GeneratorFuel) = RecipeDisplay(categoryIdentifier, redInput, greenInput, blueInput, output)

    class RecipeDisplay(private val category: CategoryIdentifier<RecipeDisplay>, val redInput: List<EntryStack<ItemStack>>, val greenInput: List<EntryStack<ItemStack>>, val blueInput: List<EntryStack<ItemStack>>, val output: GeneratorFuel): Display {

        override fun getInputEntries() = mutableListOf<EntryIngredient>().also {
            it.add(EntryIngredient.of(redInput))
            it.add(EntryIngredient.of(greenInput))
            it.add(EntryIngredient.of(blueInput))
        }

        override fun getOutputEntries() = mutableListOf<EntryIngredient>()

        override fun getCategoryIdentifier(): CategoryIdentifier<*> = category

    }

    companion object {
        private val set = linkedSetOf<ColorfulGeneratorCategory>()

        fun getMatching(id: String) = set.firstOrNull { id == it.identifier.toString().split(":")[1].replace("_generator", "") }

        fun register(registry: CategoryRegistry) = set.forEach {
            registry.add(it)
            registry.addWorkstations(it.categoryIdentifier, EntryStacks.of(it.block))
            registry.removePlusButton(it.categoryIdentifier)
        }
    }

}