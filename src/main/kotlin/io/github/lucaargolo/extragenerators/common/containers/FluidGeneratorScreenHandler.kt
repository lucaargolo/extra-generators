package io.github.lucaargolo.extragenerators.common.containers

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import alexiil.mc.lib.attributes.item.compat.SlotFixedItemInv
import io.github.lucaargolo.extragenerators.common.blockentity.FluidGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext

class FluidGeneratorScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: FluidGeneratorBlockEntity, context: ScreenHandlerContext): AbstractGeneratorScreenHandler<FluidGeneratorScreenHandler, FluidGeneratorBlockEntity>(ScreenHandlerCompendium.FLUID_GENERATOR, syncId, playerInventory, entity, context, PacketCompendium.UPDATE_FLUID_GENERATOR_SCREEN)  {

    var burningFuel: FluidGeneratorFuel? = null
    var fluidVolume: FluidVolume? = null

    init {
        addSlot(SlotFixedItemInv(this, entity.itemInv, server, 0,116, 17))
        addSlot(object: SlotFixedItemInv(this, entity.itemInv, server, 1,116, 53) {
            override fun canInsert(stack: ItemStack?) = false
        })
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[index]
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (slot is SlotFixedItemInv) {
                if (!insertItem(itemStack2, 0, this.slots.size-2, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, this.slots.size-2, this.slots.size, false)) {
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

    override fun shouldSync() = entity.burningFuel != burningFuel || entity.fluidInv.getInvFluid(0) != fluidVolume || super.shouldSync()

    override fun postSync() {
        super.postSync()
        burningFuel = entity.burningFuel?.copy()
        fluidVolume = entity.fluidInv.getInvFluid(0)
    }

    override fun writeToBuf(buf: PacketByteBuf) {
        super.writeToBuf(buf)
        (entity.burningFuel ?: FluidGeneratorFuel(0, FluidKeys.EMPTY.withAmount(FluidAmount.ZERO), 0.0)).toBuf(buf)
        (entity.fluidInv.getInvFluid(0) ?: FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)).toMcBuffer(buf)
    }

}