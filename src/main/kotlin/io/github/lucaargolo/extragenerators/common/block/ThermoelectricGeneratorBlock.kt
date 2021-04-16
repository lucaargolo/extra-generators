package io.github.lucaargolo.extragenerators.common.block

import alexiil.mc.lib.attributes.AttributeList
import io.github.lucaargolo.extragenerators.common.blockentity.ThermoelectricGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ThermoelectricGeneratorBlock(settings: Settings, generatorConfig: ModConfig.Generator): AbstractGeneratorBlock(settings, generatorConfig) {

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) { }

    override fun createBlockEntity(world: BlockView?) = ThermoelectricGeneratorBlockEntity()

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify)
        (world.getBlockEntity(pos) as? ThermoelectricGeneratorBlockEntity)?.let {
            it.axisTemperatureDifferenceCache.clear()
        }
    }

}