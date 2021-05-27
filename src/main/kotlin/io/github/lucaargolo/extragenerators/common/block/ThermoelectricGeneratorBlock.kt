package io.github.lucaargolo.extragenerators.common.block

import alexiil.mc.lib.attributes.AttributeList
import io.github.lucaargolo.extragenerators.common.blockentity.AbstractGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.extragenerators.common.blockentity.ThermoelectricGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ThermoelectricGeneratorBlock(settings: Settings, generatorConfig: ModConfig.Generator): AbstractGeneratorBlock(settings, generatorConfig) {

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) { }

    @Suppress("deprecation")
    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify)
        (world.getBlockEntity(pos) as? ThermoelectricGeneratorBlockEntity)?.axisTemperatureDifferenceCache?.clear()
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = ThermoelectricGeneratorBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, type: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return checkType(type, BlockEntityCompendium.THERMOELECTRIC_GENERATOR_TYPE, AbstractGeneratorBlockEntity.Companion::commonTick)
    }


}