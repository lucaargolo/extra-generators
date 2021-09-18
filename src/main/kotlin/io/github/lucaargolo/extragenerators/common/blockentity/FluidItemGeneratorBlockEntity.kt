@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.FluidItemGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.*
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluid
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper

class FluidItemGeneratorBlockEntity(pos: BlockPos, state: BlockState): AbstractGeneratorBlockEntity<FluidItemGeneratorBlockEntity>(BlockEntityCompendium.FLUID_ITEM_GENERATOR_TYPE, pos, state) {

    private var fluidKey: Fluid? = null
    private var fluidItemFuelMap: ((ItemStack) -> FluidGeneratorFuel?)? = null

    val itemInv = SimpleSidedInventory(3, { slot, stack ->
        when(slot) {
            0 -> initialized && (stack.isEmpty || fluidItemFuelMap?.invoke(stack) != null)
            1 -> initialized && (stack.isEmpty || InventoryUtils.getExtractableFluid(stack)?.resource?.fluid == fluidKey)
            2 -> stack.isEmpty || InventoryUtils.canInsertFluid(stack)
            else -> true
        }
    }, { slot, _ -> slot == 2 }, { intArrayOf(0, 1, 2) })

    val fluidInv = object: SingleVariantStorage<FluidVariant>() {
        override fun getCapacity(variant: FluidVariant?) = FluidConstants.BUCKET*4
        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
        override fun canInsert(variant: FluidVariant?): Boolean {
            return initialized && variant != null && (variant.isBlank || variant.fluid == fluidKey)
        }
        override fun onFinalCommit() {
            markDirtyAndSync()
            super.onFinalCommit()
        }
    }

    var burningFuel: FluidGeneratorFuel? = null

    override fun isServerRunning() = burningFuel?.let {
        val fluidPerTick = it.fluidInput.amount().div(it.burnTime.toLong())
        val energyPerTick = MathHelper.floor(it.energyOutput / it.burnTime)
        energyStorage.amount + energyPerTick <= energyStorage.getCapacity() && fluidInv.amount >= fluidPerTick
    } ?: false

    override fun getCogWheelRotation(): Float = burningFuel?.let { MathHelper.floor(it.energyOutput/it.burnTime)/10f } ?: 0f

    override fun initialize(block: AbstractGeneratorBlock): Boolean {
        val superInitialized = super.initialize(block)
        (block as? FluidItemGeneratorBlock)?.let {
            fluidKey = it.fluid
            fluidItemFuelMap = it.fluidItemFuelMap
        }
        return fluidKey != null && fluidItemFuelMap != null && superInitialized
    }

    override fun tick() {
        super.tick()
        if(world?.isClient == false) {
            burningFuel?.let {
                val fluidPerTick = it.fluidInput.amount().div(it.burnTime.toLong())
                val energyPerTick = MathHelper.floor(it.energyOutput/it.burnTime)
                if (energyStorage.amount + energyPerTick <= energyStorage.getCapacity() && fluidInv.amount >= fluidPerTick) {
                    fluidInv.amount -= fluidPerTick
                    energyStorage.amount += energyPerTick
                    it.currentBurnTime--
                }
                if (it.currentBurnTime <= 0) {
                    burningFuel = null
                    markDirtyAndSync()
                }
            }
            if (burningFuel == null) {
                val stack = itemInv.getStack(0)
                if (!stack.isEmpty) {
                    fluidItemFuelMap?.invoke(stack)?.copy()?.let {
                        val fluidPerTick = it.fluidInput.amount().div(it.burnTime.toLong())
                        if(fluidInv.amount >= fluidPerTick) {
                            burningFuel = it
                            stack.decrement(1)
                        }
                    }
                }
            }
            val inputStack = itemInv.getStack(1)
            val transactionInventory = InventoryStorage.of(itemInv, null)
            if(!inputStack.isEmpty) {
                val fluidStorage = ContainerItemContext.ofSingleSlot(transactionInventory.getSlot(1)).find(FluidStorage.ITEM)
                if(fluidStorage != null) {
                    StorageUtil.move(fluidStorage, fluidInv, { true }, FluidConstants.BUCKET, null)
                    val storedFluid = StorageUtil.findExtractableContent(fluidStorage, null)
                    if(storedFluid == null || storedFluid.amount == 0L || storedFluid.resource.isBlank) {
                        StorageUtil.move(transactionInventory.getSlot(1), transactionInventory.getSlot(2), { true }, 1, null)
                    }
                }else{
                    StorageUtil.move(transactionInventory.getSlot(1), transactionInventory.getSlot(2), { true }, 1, null)
                }
            }
        }
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.put("itemInv", itemInv.toNbtList())
        tag.put("fluidInv", fluidInv.toNbt())
        burningFuel?.let { tag.put("burningFuel", it.toTag()) }
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        itemInv.readNbtList(tag.getList("itemInv", 10))
        fluidInv.fromNbt(tag.getCompound("fluidInv"))
        burningFuel = FluidGeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        tag.put("itemInv", itemInv.toNbtList())
        tag.put("fluidInv", fluidInv.toNbt())
        burningFuel?.let { tag.put("burningFuel", it.toTag()) }
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        super.fromClientTag(tag)
        itemInv.readNbtList(tag.getList("itemInv", 10))
        fluidInv.fromNbt(tag.getCompound("fluidInv"))
        burningFuel = FluidGeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }

}