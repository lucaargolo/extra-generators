@file:Suppress("unused", "DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.compat

import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.common.blockentity.ColorfulGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.registry.Registry

class ReiCompat: REIClientPlugin {

    override fun registerCategories(registry: CategoryRegistry) {
        ItemGeneratorCategory.register(registry)
        FluidGeneratorCategory.register(registry)
        FluidItemGeneratorCategory.register(registry)
        ThermoelectricGeneratorCategory.register(registry)
        ColorfulGeneratorCategory.register(registry)
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        ResourceCompendium.ITEM_GENERATORS.clientIngredientsMap.forEach { (id, ingredientMap) ->
            val category = ItemGeneratorCategory.getMatching(id) ?: return@forEach
            ingredientMap.forEach { (ingredient, fuel) ->
                registry.add(category.createDisplay(EntryIngredients.ofIngredient(ingredient), fuel))
            }
        }
        ResourceCompendium.FLUID_GENERATORS.clientFluidKeysMap.forEach { (id, fluidKeyMap) ->
            val category = FluidGeneratorCategory.getMatching(id) ?: return@forEach
            fluidKeyMap.forEach { (fluidKey, fuel) ->
                registry.add(category.createDisplay(EntryIngredients.of(fluidKey, fuel.fluidInput.amount/81), fuel))
            }
        }
        ResourceCompendium.BLOCK_TEMPERATURE.clientTemperatureMap.forEach { (block, temperature) ->
            THERMOELECTRIC_GENERATOR.createDisplay(block, temperature).let { registry.add(it) }
        }
        val items = DefaultedList.of<ItemStack>()
        ItemGroup.SEARCH.appendStacks(items)
        items.forEach {
            GeneratorFuel.fromBurnableGeneratorFuel(it.item)?.run {
                BURNABLE_GENERATOR.createDisplay(EntryIngredients.of(it.item), this)
            }?.let { display -> registry.add(display) }
            GeneratorFuel.fromGluttonyGeneratorFuel(it.item)?.run {
                GLUTTONY_GENERATOR.createDisplay(EntryIngredients.of(it.item), this)
            }?.let { display -> registry.add(display) }
            GeneratorFuel.fromEnchantedGeneratorFuel(it)?.run {
                ENCHANTED_GENERATOR.createDisplay(EntryIngredients.of(it), this)
            }?.let { display -> registry.add(display) }
            GeneratorFuel.fromBrewGeneratorFuel(it)?.run {
                BREW_GENERATOR.createDisplay(EntryIngredients.of(it), this)
            }?.let { display -> registry.add(display) }
            FluidGeneratorFuel.fromRedstoneGeneratorFuel(it)?.run {
                REDSTONE_GENERATOR.createDisplay(EntryIngredients.of(it), EntryIngredients.of(fluidInput.resource.fluid, fluidInput.amount/81), this)
            }?.let { display -> registry.add(display) }
            FluidGeneratorFuel.fromSteamGeneratorFuel(it)?.run {
                STEAM_GENERATOR.createDisplay(EntryIngredients.of(it), EntryIngredients.of(fluidInput.resource.fluid, fluidInput.amount/81), this)
            }?.let { display -> registry.add(display) }
        }
        val redInput = Registry.ITEM.indexedEntries.filter { it.isIn(ExtraGenerators.RED_ITEMS) }.map { EntryStacks.of(it.value()) }
        val blueInput = Registry.ITEM.indexedEntries.filter { it.isIn(ExtraGenerators.GREEN_ITEMS) }.map { EntryStacks.of(it.value()) }
        val greenInput = Registry.ITEM.indexedEntries.filter { it.isIn(ExtraGenerators.BLUE_ITEMS) }.map { EntryStacks.of(it.value()) }
        COLORFUL_GENERATOR.createDisplay(redInput, blueInput, greenInput, ColorfulGeneratorBlockEntity.getFuel()).let { registry.add(it) }
    }

    companion object {
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
    }

}