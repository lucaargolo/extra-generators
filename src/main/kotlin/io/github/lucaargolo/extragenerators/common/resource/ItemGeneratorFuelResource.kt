package io.github.lucaargolo.extragenerators.common.resource

import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.resource.ResourceManager
import java.io.InputStreamReader

class ItemGeneratorFuelResource: SimpleSynchronousResourceReloadListener {

    private val ingredientsMap = linkedMapOf<String, LinkedHashMap<Ingredient, GeneratorFuel>>()
    val clientIngredientsMap = linkedMapOf<String, LinkedHashMap<Ingredient, GeneratorFuel>>()

    fun test(id: String, itemStack: ItemStack): GeneratorFuel? {
        ingredientsMap[id]?.forEach { (ingredient, fuel) ->
            if(ingredient.test(itemStack)) return fuel
        }
        return null
    }

    fun toBuf(buf: PacketByteBuf) {
        buf.writeInt(ingredientsMap.size)
        ingredientsMap.forEach { (id, ingredientMap) ->
            buf.writeString(id)
            buf.writeInt(ingredientMap.size)
            ingredientMap.forEach { (ingredient, fuel) ->
                ingredient.write(buf)
                fuel.toBuf(buf)
            }
        }
    }

    fun fromBuf(buf: PacketByteBuf) {
        clientIngredientsMap.clear()
        val ingredientsMapSize = buf.readInt()
        repeat(ingredientsMapSize) {
            val ingredientsMapId = buf.readString()
            val ingredientMapSize = buf.readInt()
            repeat(ingredientMapSize) {
                val ingredient = Ingredient.fromPacket(buf)
                val fuel = GeneratorFuel.fromBuf(buf) ?: GeneratorFuel(0, 0.0)
                clientIngredientsMap.getOrPut(ingredientsMapId) { linkedMapOf() } [ingredient] = fuel
            }
        }
    }

    override fun getFabricId() = ModIdentifier("item_generators")

    override fun apply(manager: ResourceManager) {
        ingredientsMap.clear()
        ExtraGenerators.LOGGER.info("Loading item generators resource.")
        manager.findResources("item_generators") { r -> r.endsWith(".json") }.forEach { itemsResource ->
            val id = itemsResource.path.split("/").lastOrNull()?.replace(".json", "") ?: return@forEach
            val resource = manager.getResource(itemsResource)
            ExtraGenerators.LOGGER.info("Loading $id item generators resource at $itemsResource.")
            try {
                val json = ExtraGenerators.PARSER.parse(InputStreamReader(resource.inputStream, "UTF-8"))
                val jsonArray = json.asJsonArray
                jsonArray.forEach { jsonElement ->
                    val jsonObject = jsonElement.asJsonObject
                    val generatorFuel = GeneratorFuel.fromJson(jsonObject.get("fuel").asJsonObject)
                    generatorFuel?.let {
                        ingredientsMap.getOrPut(id) { linkedMapOf() }[Ingredient.fromJson(jsonObject.get("ingredient"))] = it
                    }
                }
            }catch (e: Exception) {
                ExtraGenerators.LOGGER.error("Unknown error while trying to read $id item generators resource at $itemsResource", e)
            }
        }
        ExtraGenerators.LOGGER.info("Finished loading item generators resource (${ingredientsMap.map { it.value.size }.sum()} entries loaded).")
    }

}