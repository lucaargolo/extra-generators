package io.github.lucaargolo.extragenerators.common.block

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.fluid.FluidInvUtil
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import io.github.lucaargolo.extragenerators.common.blockentity.FluidGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.FluidGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class FluidGeneratorBlock(settings: Settings, generatorConfig: ModConfig.Generator, val fluidFuelMap: (FluidKey) -> FluidGeneratorFuel?): AbstractGeneratorBlock(settings, generatorConfig) {

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        (world.getBlockEntity(pos) as? FluidGeneratorBlockEntity)?.let{
            to.offer(it.fluidInv.insertable)
            to.offer(it.itemInv.getSlot(0).pureInsertable)
            to.offer(it.itemInv.getSlot(1).pureExtractable)
        }
    }

    @Suppress("deprecation")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? FluidGeneratorBlockEntity)?.let{
                it.itemInv.stackIterable().forEach {
                    ItemScatterer.spawn(world, pos.x+0.0, pos.y+0.0, pos.z+0.0, it)
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, notify)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        (world.getBlockEntity(pos) as? FluidGeneratorBlockEntity)?.let{ FluidInvUtil.interactHandWithTank(it.fluidInv.transferable, player, hand).asActionResult() }?.let {
            if(it.isAccepted) return it
        }
        player.openHandledScreen(object: ExtendedScreenHandlerFactory {
            override fun getDisplayName() = name

            override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return FluidGeneratorScreenHandler(syncId, inv, world.getBlockEntity(pos) as FluidGeneratorBlockEntity, ScreenHandlerContext.create(world, pos))
            }

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        return ActionResult.SUCCESS
    }

    override fun createBlockEntity(world: BlockView?) = FluidGeneratorBlockEntity()

}