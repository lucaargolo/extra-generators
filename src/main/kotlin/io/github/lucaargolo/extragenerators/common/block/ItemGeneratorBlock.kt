package io.github.lucaargolo.extragenerators.common.block

import alexiil.mc.lib.attributes.AttributeList
import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.ItemGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ItemGeneratorBlock(settings: Settings, val generatorConfig: ModConfig.Generator, val itemFuelMap: (ItemStack) -> GeneratorFuel?): AbstractGeneratorBlock(settings) {

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        (world.getBlockEntity(pos) as? ItemGeneratorBlockEntity)?.let{
            to.offer(it.itemInv)
        }
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        player.openHandledScreen(object: ExtendedScreenHandlerFactory {
            override fun getDisplayName() = name

            override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return ItemGeneratorScreenHandler(syncId, inv, world.getBlockEntity(pos) as ItemGeneratorBlockEntity, ScreenHandlerContext.create(world, pos))
            }

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        return ActionResult.SUCCESS
    }

    override fun createBlockEntity(world: BlockView?) = ItemGeneratorBlockEntity()

}