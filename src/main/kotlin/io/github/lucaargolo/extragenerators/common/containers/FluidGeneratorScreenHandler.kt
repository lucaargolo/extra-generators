@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.common.containers

import io.github.lucaargolo.extragenerators.common.blockentity.FluidGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.SimpleSidedInventory
import io.github.lucaargolo.extragenerators.utils.toMcBuffer
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext

class FluidGeneratorScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: FluidGeneratorBlockEntity, context: ScreenHandlerContext): AbstractGeneratorScreenHandler<FluidGeneratorScreenHandler, FluidGeneratorBlockEntity>(ScreenHandlerCompendium.FLUID_GENERATOR, syncId, playerInventory, entity, context, PacketCompendium.UPDATE_FLUID_GENERATOR_SCREEN)  {

    var burningFuel: FluidGeneratorFuel? = null
    var fluidVolume: ResourceAmount<FluidVariant> = ResourceAmount(FluidVariant.blank(), 0)

    init {
        addSlot(SimpleSidedInventory.SimpleSlot(entity.itemInv, 0,116, 17))
        addSlot(SimpleSidedInventory.SimpleSlot(entity.itemInv, 1,116, 53))
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[index]
        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (slot.inventory is SimpleSidedInventory) {
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

    override fun shouldSync() = entity.burningFuel != burningFuel || entity.fluidInv.variant != fluidVolume.resource || entity.fluidInv.amount != fluidVolume.amount || super.shouldSync()

    override fun postSync() {
        super.postSync()
        burningFuel = entity.burningFuel?.copy()
        fluidVolume = ResourceAmount(entity.fluidInv.variant, entity.fluidInv.amount)
    }

    override fun writeToBuf(buf: PacketByteBuf) {
        super.writeToBuf(buf)
        (entity.burningFuel ?: FluidGeneratorFuel(0, ResourceAmount(FluidVariant.blank(), 0), 0.0)).toBuf(buf)
        ResourceAmount(entity.fluidInv.variant, entity.fluidInv.amount).toMcBuffer(buf)
    }

}