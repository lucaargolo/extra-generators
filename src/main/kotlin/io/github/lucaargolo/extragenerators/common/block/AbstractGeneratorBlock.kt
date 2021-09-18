package io.github.lucaargolo.extragenerators.common.block

import io.github.lucaargolo.extragenerators.common.blockentity.AbstractGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.minecraft.block.*
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class AbstractGeneratorBlock(settings: Settings, val generatorConfig: ModConfig.Generator): BlockWithEntity(settings) {

    init {
        defaultState = stateManager.defaultState.with(Properties.HORIZONTAL_FACING, Direction.SOUTH)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        (world.getBlockEntity(pos) as? AbstractGeneratorBlockEntity<*>)?.let {
            it.ownerUUID = placer?.uuid ?: Util.NIL_UUID
        }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
        super.appendProperties(builder)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing)
    }

    override fun hasSidedTransparency(state: BlockState?) = true

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}