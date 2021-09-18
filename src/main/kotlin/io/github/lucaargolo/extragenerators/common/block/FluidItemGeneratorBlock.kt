@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.common.block

import io.github.lucaargolo.extragenerators.common.blockentity.AbstractGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.extragenerators.common.blockentity.FluidItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.FluidItemGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.InventoryUtils
import io.github.lucaargolo.extragenerators.utils.ModConfig
import io.github.lucaargolo.extragenerators.utils.SimpleSidedInventory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.fluid.Fluid
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

class FluidItemGeneratorBlock(settings: Settings, generatorConfig: ModConfig.Generator, val fluid: Fluid, val fluidItemFuelMap: (ItemStack) -> FluidGeneratorFuel?): AbstractGeneratorBlock(settings, generatorConfig), InventoryProvider {

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory {
        return (world?.getBlockEntity(pos) as? FluidItemGeneratorBlockEntity)?.itemInv ?: SimpleSidedInventory(0, { _, _ -> false }, { _, _ -> false }, { intArrayOf(0) })
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? FluidItemGeneratorBlockEntity)?.let{
                repeat(it.itemInv.size()) { slot ->
                    ItemScatterer.spawn(world, pos.x+0.0, pos.y+0.0, pos.z+0.0, it.itemInv.getStack(slot))
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, notify)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        (world.getBlockEntity(pos) as? FluidItemGeneratorBlockEntity)?.let{ InventoryUtils.interactPlayerHand(it.fluidInv, player, hand, canExtract = false) }?.let {
            if(it.isAccepted) return it
        }
        player.openHandledScreen(object: ExtendedScreenHandlerFactory {
            override fun getDisplayName() = TranslatableText(translationKey)

            override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return FluidItemGeneratorScreenHandler(syncId, inv, world.getBlockEntity(pos) as FluidItemGeneratorBlockEntity, ScreenHandlerContext.create(world, pos))
            }

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        return ActionResult.SUCCESS
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = FluidItemGeneratorBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, type: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return checkType(type, BlockEntityCompendium.FLUID_ITEM_GENERATOR_TYPE, AbstractGeneratorBlockEntity.Companion::commonTick)
    }

}