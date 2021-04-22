package io.github.lucaargolo.extragenerators.compat

import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.plugins.REIPluginV0
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

object ReiCompat: REIPluginV0 {

    private val BURNABLE_GENERATOR = ItemGeneratorCategory("burnable_generator", BlockCompendium.BURNABLE_GENERATOR)
    private val BLAST_GENERATOR = ItemGeneratorCategory("blast_generator", BlockCompendium.BLAST_GENERATOR)
    private val DEMISE_GENERATOR = ItemGeneratorCategory("demise_generator", BlockCompendium.DEMISE_GENERATOR)
    private val DRAGON_GENERATOR = ItemGeneratorCategory("dragon_generator", BlockCompendium.DRAGON_GENERATOR)
    private val ICY_GENERATOR = ItemGeneratorCategory("icy_generator", BlockCompendium.ICY_GENERATOR)
    private val SLUDGY_GENERATOR = ItemGeneratorCategory("sludgy_generator", BlockCompendium.SLUDGY_GENERATOR)
    private val TELEPORT_GENERATOR = ItemGeneratorCategory("teleport_generator", BlockCompendium.TELEPORT_GENERATOR)
    private val WITHERED_GENERATOR = ItemGeneratorCategory("withered_generator", BlockCompendium.WITHERED_GENERATOR)
    private val ENCHANTED_GENERATOR = ItemGeneratorCategory("enchanted_generator", BlockCompendium.ENCHANTED_GENERATOR)
    private val BREW_GENERATOR = ItemGeneratorCategory("brew_generator", BlockCompendium.BREW_GENERATOR)


    override fun getPluginIdentifier() = ModIdentifier("rei_compat")

    override fun registerPluginCategories(recipeHelper: RecipeHelper) {
        ItemGeneratorCategory.registerCategories(recipeHelper)
    }

    override fun registerRecipeDisplays(recipeHelper: RecipeHelper) {
        ResourceCompendium.ITEM_GENERATORS.clientIngredientMap.forEach { (id, ingredientMap) ->
            val category = ItemGeneratorCategory.getMatching(id) ?: return@forEach
            ingredientMap.forEach { (ingredient, fuel) ->
                recipeHelper.registerDisplay(category.createDisplay(EntryStack.ofIngredient(ingredient), fuel))
            }
        }
        val items = DefaultedList.of<ItemStack>()
        ItemGroup.SEARCH.appendStacks(items)
        items.forEach { 
            GeneratorFuel.fromBurnableGeneratorFuel(it.item)?.run {
                BURNABLE_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it.item)), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
            GeneratorFuel.fromEnchantedGeneratorFuel(it)?.run {
                ENCHANTED_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it)), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
            GeneratorFuel.fromBrewGeneratorFuel(it)?.run {
                BREW_GENERATOR.createDisplay(mutableListOf(EntryStack.create(it)), this)
            }?.let { display -> recipeHelper.registerDisplay(display) }
        }
    }

    override fun registerOthers(recipeHelper: RecipeHelper) {
        ItemGeneratorCategory.registerOthers(recipeHelper)
    }


}