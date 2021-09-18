package io.github.lucaargolo.extragenerators.common.containers

import io.github.lucaargolo.extragenerators.common.blockentity.ColorfulGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.SimpleSidedInventory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext

class ColorfulGeneratorScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: ColorfulGeneratorBlockEntity, context: ScreenHandlerContext): AbstractGeneratorScreenHandler<ColorfulGeneratorScreenHandler, ColorfulGeneratorBlockEntity>(ScreenHandlerCompendium.COLORFUL_GENERATOR, syncId, playerInventory, entity, context, PacketCompendium.UPDATE_COLORFUL_GENERATOR_SCREEN)  {

    var burningFuel: GeneratorFuel? = null

    init {
        addSlot(SimpleSidedInventory.SimpleSlot(entity.itemInv, 0, 44, 53))
        addSlot(SimpleSidedInventory.SimpleSlot(entity.itemInv, 1, 80, 53))
        addSlot(SimpleSidedInventory.SimpleSlot(entity.itemInv, 2, 116, 53))
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[index]
        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (slot.inventory is SimpleSidedInventory) {
                if (!insertItem(itemStack2, 0, this.slots.size-3, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, this.slots.size-3, this.slots.size, false)) {
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