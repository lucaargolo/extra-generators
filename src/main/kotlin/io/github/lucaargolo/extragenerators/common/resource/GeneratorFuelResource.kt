package io.github.lucaargolo.extragenerators.common.resource

import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.recipe.Ingredient
import net.minecraft.resource.ResourceManager
import java.io.InputStreamReader

class GeneratorFuelResource(private val id: String): SimpleSynchronousResourceReloadListener {

    val ingredientsMap = linkedMapOf<Ingredient, GeneratorFuel>()

    override fun getFabricId() = ModIdentifier("item_generators")

    override fun apply(manager: ResourceManager) {
        ingredientsMap.clear()
        ExtraGenerators.LOGGER.info("Loading $id items resource.")
        manager.findResources("item_generators") { r -> r.equals("$id.json")}.forEach { itemsResource ->
            val resource = manager.getResource(itemsResource)
            ExtraGenerators.LOGGER.info("Found $id items resource at $itemsResource. Trying to read...")
            try {
                val json = ExtraGenerators.PARSER.parse(InputStreamReader(resource.inputStream, "UTF-8"))
                val jsonArray = json.asJsonArray
                jsonArray.forEach { jsonElement ->
                    val jsonObject = jsonElement.asJsonObject
                    val generatorFuel = GeneratorFuel.fromJson(jsonObject.get("fuel").asJsonObject)
                    generatorFuel?.let {
                        ingredientsMap[Ingredient.fromJson(jsonObject.get("ingredient"))] = it
                    }
                }
            }catch (e: Exception) {
                ExtraGenerators.LOGGER.error("Unknown error while trying to read $id items resource ($itemsResource)", e)
            }
        }
        ExtraGenerators.LOGGER.info("Finished loading $id items resource (${ingredientsMap.size} entries loaded).")
    }

}