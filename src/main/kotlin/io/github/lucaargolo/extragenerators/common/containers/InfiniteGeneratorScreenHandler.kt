package io.github.lucaargolo.extragenerators.common.containers

import io.github.lucaargolo.extragenerators.common.blockentity.InfiniteGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.ActiveGenerators
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class InfiniteGeneratorScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: InfiniteGeneratorBlockEntity, context: ScreenHandlerContext): AbstractGeneratorScreenHandler<InfiniteGeneratorScreenHandler, InfiniteGeneratorBlockEntity>(ScreenHandlerCompendium.INFINITE_GENERATOR, syncId, playerInventory, entity, context, PacketCompendium.UPDATE_INFINITE_GENERATOR_SCREEN)  {

    var activeGenerators: LinkedHashMap<Identifier, Int> = linkedMapOf()

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun shouldSync() = !ActiveGenerators.test(entity.ownerUUID, activeGenerators) || super.shouldSync()

    override fun postSync() {
        super.postSync()
        activeGenerators = ActiveGenerators.get(entity.ownerUUID)
    }

    override fun writeToBuf(buf: PacketByteBuf) {
        super.writeToBuf(buf)
        buf.writeInt(ActiveGenerators.get(entity.ownerUUID).size)
        ActiveGenerators.get(entity.ownerUUID).forEach { (identifier, qnt) ->
            buf.writeIdentifier(identifier)
            buf.writeInt(qnt)
        }
    }

}