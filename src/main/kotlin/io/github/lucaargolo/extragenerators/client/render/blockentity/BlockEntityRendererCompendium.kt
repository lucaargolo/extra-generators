package io.github.lucaargolo.extragenerators.client.render.blockentity

import io.github.lucaargolo.extragenerators.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.extragenerators.utils.GenericCompendium
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import java.util.function.Function

object BlockEntityRendererCompendium: GenericCompendium<Function<BlockEntityRenderDispatcher, BlockEntityRenderer<*>>>() {

    //val SCREEN_FACTORY = register("screen") { ScreenBlockEntityRenderer(it) }

    @Suppress("UNCHECKED_CAST")
    override fun initialize() {
        map.forEach { (entityIdentifier, renderFactory) ->
            BlockEntityRendererRegistry.INSTANCE.register(BlockEntityCompendium.get(entityIdentifier) as BlockEntityType<BlockEntity>, renderFactory as Function<BlockEntityRenderDispatcher, BlockEntityRenderer<BlockEntity>>)
        }
    }

}