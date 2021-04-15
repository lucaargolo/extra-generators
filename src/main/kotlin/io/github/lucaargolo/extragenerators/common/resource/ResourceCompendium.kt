package io.github.lucaargolo.extragenerators.common.resource

import io.github.lucaargolo.extragenerators.utils.GenericCompendium
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceType

object ResourceCompendium: GenericCompendium<SimpleSynchronousResourceReloadListener>() {

    val ITEM_GENERATORS = register("item_generators", ItemGeneratorFuelResource())
    val FLUID_GENERATORS = register("fluid_generators", FluidGeneratorFuelResource())

    override fun initialize() {
        map.forEach { (_, resource) ->
            ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(resource)
        }
    }
    
}