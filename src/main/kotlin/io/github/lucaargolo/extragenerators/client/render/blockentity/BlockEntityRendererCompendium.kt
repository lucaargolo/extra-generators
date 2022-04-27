package io.github.lucaargolo.extragenerators.client.render.blockentity

import io.github.lucaargolo.extragenerators.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.extragenerators.utils.GenericCompendium
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory

object BlockEntityRendererCompendium: GenericCompendium<BlockEntityRendererFactory<*>>() {

    init {
        register("item_generator", BlockEntityRendererFactory { GeneratorBlockEntityRenderer() })
        register("fluid_generator", BlockEntityRendererFactory { GeneratorBlockEntityRenderer() })
        register("fluid_item_generator", BlockEntityRendererFactory { GeneratorBlockEntityRenderer() })
        register("colorful_generator", BlockEntityRendererFactory { GeneratorBlockEntityRenderer() })
        register("thermoelectric_generator", BlockEntityRendererFactory  { GeneratorBlockEntityRenderer() })
        register("infinite_generator", BlockEntityRendererFactory { GeneratorBlockEntityRenderer() })
    }

    @Suppress("UNCHECKED_CAST")
    override fun initialize() {
        map.forEach { (entityIdentifier, renderFactory) ->
            BlockEntityRendererRegistry.register(BlockEntityCompendium.get(entityIdentifier) as BlockEntityType<BlockEntity>, renderFactory as BlockEntityRendererFactory<BlockEntity>)
        }
    }

}