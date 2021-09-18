package io.github.lucaargolo.extragenerators.common.containers

import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.SimpleSidedInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot

class ItemGeneratorScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: ItemGeneratorBlockEntity, context: ScreenHandlerContext): AbstractGeneratorScreenHandler<ItemGeneratorScreenHandler, ItemGeneratorBlockEntity>(ScreenHandlerCompendium.ITEM_GENERATOR, syncId, playerInventory, entity, context, PacketCompendium.UPDATE_ITEM_GENERATOR_SCREEN)  {

    var burningFuel: GeneratorFuel? = null

    init {
        addSlot(SimpleSidedInventory.SimpleSlot(entity.itemInv, 0,80, 53))
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[index]
        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (slot.inventory is SimpleSidedInventory) {
                if (!insertItem(itemStack2, 0, this.slots.size-1, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, this.slots.size-1, this.slots.size, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return itemStack
    }

    override fun shouldSync() = entity.burningFuel != burningFuel || super.shouldSync()

    override fun postSync() {
        super.postSync()
        burningFuel = entity.burningFuel?.copy()
    }

    override fun writeToBuf(buf: PacketByteBuf) {
        super.writeToBuf(buf)
        (entity.burningFuel ?: GeneratorFuel(0, 0.0)).toBuf(buf)
    }

}