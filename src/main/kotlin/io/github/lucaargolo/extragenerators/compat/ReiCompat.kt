package io.github.lucaargolo.extragenerators.compat

import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.plugins.REIPluginV0
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl

object ReiCompat: REIPluginV0 {

    private val BURNABLE_GENERATOR = ItemGeneratorCategory("burnable_generator", BlockCompendium.BURNABLE_GENERATOR)

    override fun getPluginIdentifier() = ModIdentifier("rei_compat")

    override fun registerPluginCategories(recipeHelper: RecipeHelper) {
        ItemGeneratorCategory.registerCategories(recipeHelper)
    }

    override fun registerRecipeDisplays(recipeHelper: RecipeHelper) {
        FuelRegistryImpl.INSTANCE.fuelTimes.forEach { (burnableItem, _) ->
            recipeHelper.registerDisplay(GeneratorFuel.fromBurnableGeneratorFuel(burnableItem)?.run {
                BURNABLE_GENERATOR.createDisplay(mutableListOf(EntryStack.create(burnableItem)), this)
            })
        }
    }

    override fun registerOthers(recipeHelper: RecipeHelper) {
        ItemGeneratorCategory.registerOthers(recipeHelper)
    }


}