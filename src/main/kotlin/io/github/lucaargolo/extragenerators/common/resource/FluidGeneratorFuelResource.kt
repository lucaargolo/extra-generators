package io.github.lucaargolo.extragenerators.common.resource

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import java.io.InputStreamReader

class FluidGeneratorFuelResource: SimpleSynchronousResourceReloadListener {

    private val fluidKeysMap = linkedMapOf<String, LinkedHashMap<FluidKey, FluidGeneratorFuel>>()

    fun test(id: String, fluidKey: FluidKey): FluidGeneratorFuel? {
        fluidKeysMap[id]?.forEach { (key, fuel) ->
            if(key == fluidKey) return fuel
        }
        return null
    }

    override fun getFabricId() = ModIdentifier("fluid_generators")

    override fun apply(manager: ResourceManager) {
        fluidKeysMap.clear()
        ExtraGenerators.LOGGER.info("Loading fluid generators resource.")
        manager.findResources("fluid_generators") { r -> r.endsWith(".json") }.forEach { fluidsResource ->
            val id = fluidsResource.path.split("/").lastOrNull()?.replace(".json", "") ?: return@forEach
            val resource = manager.getResource(fluidsResource)
            ExtraGenerators.LOGGER.info("Loading $id fluid generators resource at $fluidsResource.")
            try {
                val json = ExtraGenerators.PARSER.parse(InputStreamReader(resource.inputStream, "UTF-8"))
                val jsonArray = json.asJsonArray
                jsonArray.forEach { jsonElement ->
                    val jsonObject = jsonElement.asJsonObject
                    val generatorFuel = FluidGeneratorFuel.fromJson(jsonObject.get("fuel").asJsonObject)
                    generatorFuel?.let {
                        fluidKeysMap.getOrPut(id) { linkedMapOf() }[it.fluidInput.fluidKey] = it
                    }
                }
            }catch (e: Exception) {
                ExtraGenerators.LOGGER.error("Unknown error while trying to read $id fluid generators resource at $fluidsResource", e)
            }
        }
        ExtraGenerators.LOGGER.info("Finished loading fluid generators resource (${fluidKeysMap.map { it.value.size }.sum()} entries loaded).")
    }

}