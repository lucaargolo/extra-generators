package io.github.lucaargolo.extragenerators.common.containers

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import alexiil.mc.lib.attributes.item.compat.SlotFixedItemInv
import io.github.lucaargolo.extragenerators.common.blockentity.FluidGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.FluidItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext

class FluidItemGeneratorScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: FluidItemGeneratorBlockEntity, context: ScreenHandlerContext): AbstractGeneratorScreenHandler<FluidItemGeneratorScreenHandler, FluidItemGeneratorBlockEntity>(ScreenHandlerCompendium.FLUID_ITEM_GENERATOR, syncId, playerInventory, entity, context, PacketCompendium.UPDATE_FLUID_ITEM_GENERATOR_SCREEN)  {

    var burningFuel: FluidGeneratorFuel? = null
    var fluidVolume: FluidVolume? = null

    init {
        addSlot(SlotFixedItemInv(this, entity.itemInv, server, 0,80, 53))
        addSlot(SlotFixedItemInv(this, entity.itemInv, server, 1,116, 17))
        addSlot(object: SlotFixedItemInv(this, entity.itemInv, server, 2,116, 53) {
            override fun canInsert(stack: ItemStack?) = false
        })
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun shouldSync() = entity.burningFuel != burningFuel || entity.fluidInv.getInvFluid(0) != fluidVolume || super.shouldSync()

    override fun postSync() {
        super.postSync()
        burningFuel = entity.burningFuel?.copy()
        fluidVolume = entity.fluidInv.getInvFluid(0).copy()
    }

    override fun writeToBuf(buf: PacketByteBuf) {
        super.writeToBuf(buf)
        (entity.burningFuel ?: FluidGeneratorFuel(0, FluidKeys.EMPTY.withAmount(FluidAmount.ZERO), 0.0)).toBuf(buf)
        (entity.fluidInv.getInvFluid(0) ?: FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)).toMcBuffer(buf)
    }

}