package io.github.lucaargolo.extragenerators.common.block

import io.github.lucaargolo.extragenerators.common.blockentity.AbstractGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.extragenerators.common.blockentity.InfiniteGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.InfiniteGeneratorScreenHandler
import io.github.lucaargolo.extragenerators.utils.ModConfig
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class InfiniteGeneratorBlock(settings: Settings, generatorConfig: ModConfig.Generator): AbstractGeneratorBlock(settings, generatorConfig) {

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        player.openHandledScreen(object: ExtendedScreenHandlerFactory {
            override fun getDisplayName() = TranslatableText(translationKey)

            override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return InfiniteGeneratorScreenHandler(syncId, inv, world.getBlockEntity(pos) as InfiniteGeneratorBlockEntity, ScreenHandlerContext.create(world, pos))
            }

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        return ActionResult.SUCCESS
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = InfiniteGeneratorBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, type: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return checkType(type, BlockEntityCompendium.INFINITE_GENERATOR_TYPE, AbstractGeneratorBlockEntity.Companion::commonTick)
    }

}