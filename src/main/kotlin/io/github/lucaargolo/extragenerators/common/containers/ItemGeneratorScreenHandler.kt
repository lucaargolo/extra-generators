package io.github.lucaargolo.extragenerators.common.containers

import alexiil.mc.lib.attributes.item.compat.SlotFixedItemInv
import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext

class ItemGeneratorScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: ItemGeneratorBlockEntity, context: ScreenHandlerContext): AbstractGeneratorScreenHandler<ItemGeneratorScreenHandler, ItemGeneratorBlockEntity>(ScreenHandlerCompendium.ITEM_GENERATOR, syncId, playerInventory, entity, context, PacketCompendium.UPDATE_ITEM_GENERATOR_SCREEN)  {

    var burningFuel: GeneratorFuel? = null

    init {
        addSlot(SlotFixedItemInv(this, entity.itemInv, server, 0,8+18*4, 53))
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun shouldSync() = entity.burningFuel != burningFuel || super.shouldSync()

    override fun postSync() {
        burningFuel = entity.burningFuel?.copy()
    }

    override fun writeToBuf(buf: PacketByteBuf) {
        super.writeToBuf(buf)
        (burningFuel ?: GeneratorFuel(0, 0, 0.0)).toBuf(buf)
    }

}