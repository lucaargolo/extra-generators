package io.github.lucaargolo.extragenerators.compat

import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.common.blockentity.ColorfulGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.fractions.Fraction
import me.shedaniel.rei.api.plugins.REIPluginV0
import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

@Suppress("unused")
object ReiCompat: REIPluginV0 {

    private val BURNABLE_GENERATOR = ItemGeneratorCategory("burnable_generator", BlockCompendium.BURNABLE_GENERATOR)
    private val GLUTTONY_GENERATOR = ItemGeneratorCategory("gluttony_generator", BlockCompendium.GLUTTONY_GENERATOR)
    private val BLAST_GENERATOR = ItemGeneratorCategory("blast_generator", BlockCompendium.BLAST_GENERATOR)
    private val DEMISE_GENERATOR = ItemGeneratorCategory("demise_generator", BlockCompendium.DEMISE_GENERATOR)
    private val DRAGON_GENERATOR = ItemGeneratorCategory("dragon_generator", BlockCompendium.DRAGON_GENERATOR)
    private val ICY_GENERATOR = ItemGeneratorCategory("icy_generator", BlockCompendium.ICY_GENERATOR)
    private val SLUDGY_GENERATOR = ItemGeneratorCategory("sludgy_generator", BlockCompendium.SLUDGY_GENERATOR)
    private val TELEPORT_GENERATOR = ItemGeneratorCategory("teleport_generator", BlockCompendium.TELEPORT_GENERATOR)
    private val WITHERED_GENERATOR = ItemGeneratorCategory("withered_generator", BlockCompendium.WITHERED_GENERATOR)
    private val ENCHANTED_GENERATOR = ItemGeneratorCategory("enchanted_generator", BlockCompendium.ENCHANTED_GENERATOR)
    private val BREW_GENERATOR = ItemGeneratorCategory("brew_generator", BlockCompendium.BREW_GENERATOR)
    private val SCALDING_GENERATOR = FluidGeneratorCategory("scalding_generator", BlockCompendium.SCALDING_GENERATOR)
    private val REDSTONE_GENERATOR = FluidItemGeneratorCategory("redstone_generator", BlockCompendium.REDSTONE_GENERATOR)
    private val STEAM_GENERATOR = FluidItemGeneratorCategory("steam_generator", BlockCompendium.STEAM_GENERATOR)
    private val THERMOELECTRIC_GENERATOR = ThermoelectricGeneratorCategory("thermoelectric_generator", BlockCompendium.THERMOELECTRIC_GENERATOR)
    private val COLORFUL_GENERATOR = ColorfulGeneratorCategory("colorful_generator", BlockCompendium.COLORFUL_GENERATOR)

    override fun getPluginIdentifier() = ModIdentifier("rei_compat")

    override fun registerPluginCategories(recipeHelper: RecipeHelper) {
        ItemGeneratorCategory.registerCategories(recipeHelper)
        FluidGeneratorCategory.registerCategories(recipeHelper)
        FluidItemGeneratorCategory.registerCategories(recipeHelper)
        ThermoelectricGeneratorCategory.registerCategories(recipeHelper)
        ColorfulGeneratorCategory.registerCategories(recipeHelper)
    }

    override fun registerRecipeDisplays(recipeHelper: RecipeHelper) {
        ResourceCompendium.ITEM_GENERATORS.clientIngredientsMap.forEach { (id, ingredientMap) ->
            val category = ItemGeneratorCategory.getMatching(id) ?: return@forEach
            ingredientMap.forEach { (ingredient, fuel) ->
                recipeHelper.registerDisplay(category.createDisplay(EntryStack.ofIngredient(ingredient), fuel))
            }
        }
        ResourceCompendium.FLUID_GENERATORS.clientFluidKeysMap.forEach { (id, fluidKeyMap) ->
            val category = FluidGeneratorCategory.getMatching(id) ?: return@forEach
            fluidKeyMap.forEach { (fluidKey, fuel) ->
                recipeHelper.registerDisplay(category.createDisplay(EntryStack.create(fluidKey.rawFluid, fuel.fluidInput.amount().run { Fraction.of(whole, numerator, denominator) }), fuel))
            }
        }
        ResourceCompendium.BLOCK_TEMPERATURE.clientTemperatureMap.forEach { (block, temperature) ->
            THERMOELECTRIC_GENERATOR.createDisplay(block, temperature).let { recipeHelper.registerDisplay(it) }
        }
        val items = DefaultedList.of<ItemStack>()
        ItemGroup.SEARCH.appendStacks(items)
        items.forEach {
            GeneratorFuel.fromBurnableGeneratorFuel(it.item)?.run {
                BURNABLE_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it.item)), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
            GeneratorFuel.fromGluttonyGeneratorFuel(it.item)?.run {
                GLUTTONY_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it.item)), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
            GeneratorFuel.fromEnchantedGeneratorFuel(it)?.run {
                ENCHANTED_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it)), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
            GeneratorFuel.fromBrewGeneratorFuel(it)?.run {
                BREW_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it)), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
            FluidGeneratorFuel.fromRedstoneGeneratorFuel(it)?.run {
                REDSTONE_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it)), EntryStack.create(fluidInput.rawFluid, fluidInput.amount().run { Fraction.of(whole, numerator, denominator) }), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
            FluidGeneratorFuel.fromSteamGeneratorFuel(it)?.run {
                STEAM_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it)), EntryStack.create(fluidInput.rawFluid, fluidInput.amount().run { Fraction.of(whole, numerator, denominator) }), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
        }
        val redInput = TagRegistry.item(ModIdentifier("red_items")).values().map { EntryStack.create(it) }
        val blueInput = TagRegistry.item(ModIdentifier("green_items")).values().map { EntryStack.create(it) }
        val greenInput = TagRegistry.item(ModIdentifier("blue_items")).values().map { EntryStack.create(it) }
        COLORFUL_GENERATOR.createDisplay(redInput, blueInput, greenInput, ColorfulGeneratorBlockEntity.getFuel()).let { recipeHelper.registerDisplay(it) }
    }

    override fun registerOthers(recipeHelper: RecipeHelper) {
        ItemGeneratorCategory.registerOthers(recipeHelper)
        FluidGeneratorCategory.registerOthers(recipeHelper)
        FluidItemGeneratorCategory.registerOthers(recipeHelper)
        ThermoelectricGeneratorCategory.registerOthers(recipeHelper)
        ColorfulGeneratorCategory.registerOthers(recipeHelper)
    }


}