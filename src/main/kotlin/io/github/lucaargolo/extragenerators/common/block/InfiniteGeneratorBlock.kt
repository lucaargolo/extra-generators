package io.github.lucaargolo.extragenerators.common.block

import alexiil.mc.lib.attributes.AttributeList
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class InfiniteGeneratorBlock(settings: Settings, generatorConfig: ModConfig.Generator): AbstractGeneratorBlock(settings, generatorConfig) {

    override fun addAllAttributes(world: World?, pos: BlockPos?, state: BlockState?, to: AttributeList<*>?) {}

    override fun createBlockEntity(world: BlockView?): BlockEntity? {
        TODO("Not yet implemented")
    }

}