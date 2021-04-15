package io.github.lucaargolo.extragenerators.common.block

import alexiil.mc.lib.attributes.AttributeProvider
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction

abstract class AbstractGeneratorBlock(settings: Settings, val generatorConfig: ModConfig.Generator): Block(settings), BlockEntityProvider, AttributeProvider {

    init {
        defaultState = stateManager.defaultState.with(Properties.HORIZONTAL_FACING, Direction.SOUTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
        super.appendProperties(builder)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing)
    }

    override fun hasSidedTransparency(state: BlockState?) = true

}