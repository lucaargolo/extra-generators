package io.github.lucaargolo.extragenerators.common.containers

import io.github.lucaargolo.extragenerators.common.blockentity.AbstractGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class AbstractGeneratorScreenHandler<S: AbstractGeneratorScreenHandler<S, B>, B: AbstractGeneratorBlockEntity<B>>(type: ScreenHandlerType<S>, syncId: Int, val playerInventory: PlayerInventory, val entity: B, private val context: ScreenHandlerContext, private val packetId: Identifier): ScreenHandler(type, syncId)  {

    val server: Boolean
        get() = !playerInventory.player.world.isClient

    var energyStored = 0.0

    init {
        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 84 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 142))
        }
    }

    open fun shouldSync() = entity.getStored(null) != energyStored

    open fun postSync() {
        energyStored = entity.getStored(null)
    }

    open fun writeToBuf(buf: PacketByteBuf) {
        buf.writeDouble(entity.getStored(null))
    }

    override fun sendContentUpdates() {
        (playerInventory.player as? ServerPlayerEntity)?.let { player ->
            if(shouldSync()) {
                ServerPlayNetworking.send(player, packetId, PacketByteBufs.create().also { writeToBuf(it) })
                postSync()
            }
        }
        super.sendContentUpdates()
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return context.run({ world: World, blockPos: BlockPos ->
            if (world.getBlockEntity(blockPos) != entity) false
            else player.squaredDistanceTo(
                blockPos.x + .5,
                blockPos.y + .5,
                blockPos.z + .5
            ) < 64.0
        }, true)
    }

}
