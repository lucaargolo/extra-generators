package io.github.lucaargolo.extragenerators.common.block

import alexiil.mc.lib.attributes.AttributeList
import io.github.lucaargolo.extragenerators.common.blockentity.ColorfulGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.ColorfulGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
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
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ColorfulGeneratorBlock(settings: Settings, generatorConfig: ModConfig.Generator): AbstractGeneratorBlock(settings, generatorConfig) {

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        (world.getBlockEntity(pos) as? ColorfulGeneratorBlockEntity)?.let{
            to.offer(it.itemInv.insertable)
        }
    }

    @Suppress("deprecation")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, notify: Boolean) {
        if (!state.isOf(newState.block)) {
            (world.getBlockEntity(pos) as? ColorfulGeneratorBlockEntity)?.let{
                it.itemInv.stackIterable().forEach {
                    ItemScatterer.spawn(world, pos.x+0.0, pos.y+0.0, pos.z+0.0, it)
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, notify)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        player.openHandledScreen(object: ExtendedScreenHandlerFactory {
            override fun getDisplayName() = TranslatableText(translationKey)

            override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return ColorfulGeneratorScreenHandler(syncId, inv, world.getBlockEntity(pos) as ColorfulGeneratorBlockEntity, ScreenHandlerContext.create(world, pos))
            }

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        return ActionResult.SUCCESS
    }

    override fun createBlockEntity(world: BlockView?) = ColorfulGeneratorBlockEntity()

}