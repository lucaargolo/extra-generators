@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.FluidGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.*
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluid
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

class FluidGeneratorBlockEntity(pos: BlockPos, state: BlockState): AbstractGeneratorBlockEntity<FluidGeneratorBlockEntity>(BlockEntityCompendium.FLUID_GENERATOR_TYPE, pos, state) {

    private var fluidFuelMap: ((Fluid) -> FluidGeneratorFuel?)? = null

    val itemInv = SimpleSidedInventory(2, { slot, stack ->
        when(slot) {
            0 -> { initialized && (stack.isEmpty || InventoryUtils.getExtractableFluid(stack).let { r -> r != null && !r.resource.isBlank && r.amount > 0L && fluidFuelMap?.invoke(r.resource.fluid) != null }) }
            1 -> { stack.isEmpty || InventoryUtils.canInsertFluid(stack) }
            else -> { true }
        }
    }, { slot, _ -> slot == 2 }, { intArrayOf(0, 1) })

    val fluidInv = object: SingleVariantStorage<FluidVariant>() {
        override fun getCapacity(variant: FluidVariant?) = FluidConstants.BUCKET*4
        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        override fun canInsert(variant: FluidVariant?): Boolean {
            return initialized && variant != null && (variant.isBlank || fluidFuelMap?.invoke(variant.fluid) != null)
        }
        override fun onFinalCommit() {
            markDirtyAndSync()
            super.onFinalCommit()
        }
    }

    var burningFuel: FluidGeneratorFuel? = null

    override fun isServerRunning() = burningFuel?.let { energyStorage.amount + MathHelper.floor(it.energyOutput/it.burnTime) <= energyStorage.getCapacity() } ?: false

    override fun getCogWheelRotation(): Float = burningFuel?.let { MathHelper.floor(it.energyOutput/it.burnTime)/10f } ?: 0f

    override fun initialize(block: AbstractGeneratorBlock): Boolean {
        val superInitialized = super.initialize(block)
        (block as? FluidGeneratorBlock)?.let {
            fluidFuelMap = it.fluidFuelMap
        }
        return fluidFuelMap != null && superInitialized
    }

    override fun tick() {
        super.tick()
        if(world?.isClient == false) {
            burningFuel?.let {
                val energyPerTick = MathHelper.floor(it.energyOutput/it.burnTime)
                if (energyStorage.amount + energyPerTick <= energyStorage.getCapacity()) {
                    energyStorage.amount += energyPerTick
                    it.currentBurnTime--
                }
                if (it.currentBurnTime <= 0) {
                    burningFuel = null
                    markDirtyAndSync()
                }
            }
            if (burningFuel == null) {
                val fluidKey = fluidInv.variant.fluid
                fluidFuelMap?.invoke(fluidKey)?.copy()?.let {
                    if(fluidInv.amount >= it.fluidInput.amount) {
                        fluidInv.amount -= it.fluidInput.amount
                        burningFuel = it
                        markDirtyAndSync()
                    }
                }
            }
            val inputStack = itemInv.getStack(0)
            val transactionInventory = InventoryStorage.of(itemInv, null)
            if(!inputStack.isEmpty) {
                val fluidStorage = ContainerItemContext.ofSingleSlot(transactionInventory.getSlot(0)).find(FluidStorage.ITEM)
                if(fluidStorage != null) {
                    StorageUtil.move(fluidStorage, fluidInv, { true }, FluidConstants.BUCKET, null)
                    val storedFluid = StorageUtil.findExtractableContent(fluidStorage, null)
                    if(storedFluid == null || storedFluid.amount == 0L || storedFluid.resource.isBlank) {
                        StorageUtil.move(transactionInventory.getSlot(0), transactionInventory.getSlot(1), { true }, 1, null)
                    }
                }else{
                    StorageUtil.move(transactionInventory.getSlot(0), transactionInventory.getSlot(1), { true }, 1, null)
                }
            }
        }
    }

    override fun writeNbt(tag: NbtCompound) {
        tag.put("itemInv", itemInv.toNbt())
        tag.put("fluidInv", fluidInv.toNbt())
        burningFuel?.let { tag.put("burningFuel", it.toTag()) }
        super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        itemInv.fromNbt(tag.get("itemInv"))
        fluidInv.fromNbt(tag.getCompound("fluidInv"))
        burningFuel = FluidGeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }


}