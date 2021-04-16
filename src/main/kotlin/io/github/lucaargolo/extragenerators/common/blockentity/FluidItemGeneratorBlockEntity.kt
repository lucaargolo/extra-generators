package io.github.lucaargolo.extragenerators.common.blockentity

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv
import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.FluidGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.FluidItemGeneratorBlock
import io.github.lucaargolo.extragenerators.mixin.BucketItemAccessor
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import net.minecraft.block.BlockState
import net.minecraft.item.BucketItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag

class FluidItemGeneratorBlockEntity: AbstractGeneratorBlockEntity<FluidItemGeneratorBlockEntity>(BlockEntityCompendium.FLUID_ITEM_GENERATOR_TYPE) {

    private var fluidKey: FluidKey? = null
    private var fluidItemFuelMap: ((ItemStack) -> FluidGeneratorFuel?)? = null

    val itemInv = object: FullFixedItemInv(3) {
        override fun getFilterForSlot(slot: Int): ItemFilter {
            return when(slot) {
                0 -> ItemFilter { initialized && (it.isEmpty || fluidItemFuelMap?.invoke(it) != null) }
                1 -> ItemFilter { initialized && (it.isEmpty || (it.item as? BucketItemAccessor)?.fluid?.let { fluid -> FluidKeys.get(fluid) }?.let{ key -> key == fluidKey } == true) }
                2 -> ItemFilter { it.isEmpty || it.item == Items.BUCKET }
                else -> ItemFilter { true }
            }
        }
        override fun isItemValidForSlot(slot: Int, item: ItemStack) = getFilterForSlot(slot).matches(item)
    }

    val fluidInv = object: SimpleFixedFluidInv(1, FluidAmount.ofWhole(4)) {
        override fun getFilterForTank(tank: Int): FluidFilter = FluidFilter { initialized && (it.isEmpty || it == fluidKey) }
        override fun isFluidValidForTank(tank: Int, fluid: FluidKey?) = getFilterForTank(tank).matches(fluid)
    }

    var burningFuel: FluidGeneratorFuel? = null

    override fun isServerRunning() = burningFuel?.let {
        val volume = fluidInv.attemptAnyExtraction(FluidAmount.ABSOLUTE_MAXIMUM, Simulation.SIMULATE)
        val fluidPerTick = it.fluidInput.amount().div(it.burnTime.toLong())
        val energyPerTick = it.energyOutput / it.burnTime
        storedPower + energyPerTick <= maxStoredPower && !volume.split(fluidPerTick).isEmpty
    } ?: false

    override fun getCogWheelRotation(): Float = burningFuel?.let { (it.energyOutput.toFloat()/it.burnTime)/10f } ?: 0f

    override fun initialize(block: AbstractGeneratorBlock): Boolean {
        val superInitialized = super.initialize(block)
        (block as? FluidItemGeneratorBlock)?.let {
            fluidKey = it.fluidKey
            fluidItemFuelMap = it.fluidItemFuelMap
        }
        return fluidKey != null && fluidItemFuelMap != null && superInitialized
    }

    override fun tick() {
        super.tick()
        if(world?.isClient == false) {
            burningFuel?.let {
                val volume = fluidInv.attemptAnyExtraction(FluidAmount.ABSOLUTE_MAXIMUM, Simulation.SIMULATE)
                val fluidPerTick = it.fluidInput.amount().div(it.burnTime.toLong())
                val energyPerTick = it.energyOutput / it.burnTime
                if (storedPower + energyPerTick <= maxStoredPower && !volume.split(fluidPerTick).isEmpty) {
                    fluidInv.extract(fluidPerTick)
                    storedPower += energyPerTick
                    it.currentBurnTime--
                }
                if (it.currentBurnTime <= 0) {
                    burningFuel = null
                    markDirtyAndSync()
                }
            }
            if (burningFuel == null) {
                val stack = itemInv.getSlot(0).attemptAnyExtraction(1, Simulation.SIMULATE)
                if (!stack.isEmpty) {
                    fluidItemFuelMap?.invoke(stack)?.copy()?.let {
                        val volume = fluidInv.attemptAnyExtraction(FluidAmount.ABSOLUTE_MAXIMUM, Simulation.SIMULATE)
                        val fluidPerTick = it.fluidInput.amount().div(it.burnTime.toLong())
                        if(!volume.split(fluidPerTick).isEmpty) {
                            burningFuel = it
                            itemInv.getSlot(0).extract(1)
                        }
                    }
                }
            }
            if(!itemInv.getInvStack(1).isEmpty) {
                val inputStack = itemInv.getSlot(1).get()
                val bucketItem = inputStack.item as? BucketItem ?: return
                val bucketFluid = (bucketItem as? BucketItemAccessor)?.fluid?.let { FluidKeys.get(it).withAmount(FluidAmount.BUCKET) } ?: return
                if(fluidInv.getTank(0).attemptInsertion(bucketFluid.copy(), Simulation.SIMULATE).isEmpty) {
                    val outputStack = itemInv.getSlot(2).get()
                    if (outputStack.isEmpty) {
                        itemInv.getSlot(1).set(inputStack.also { it.decrement(1) })
                        itemInv.getSlot(2).set(ItemStack(Items.BUCKET))
                        fluidInv.getTank(0).insert(bucketFluid.copy())
                        markDirtyAndSync()
                    } else if (outputStack.isItemEqual(ItemStack(Items.BUCKET)) && outputStack.count + 1 < outputStack.maxCount) {
                        itemInv.getSlot(1).set(inputStack.also { it.decrement(1) })
                        itemInv.getSlot(2).set(outputStack.also { it.increment(1) })
                        fluidInv.getTank(0).insert(bucketFluid.copy())
                        markDirtyAndSync()
                    }
                }
            }
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.put("itemInv", itemInv.toTag())
        tag.put("fluidInv", fluidInv.toTag())
        burningFuel?.let { tag.put("burningFuel", it.toTag()) }
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        itemInv.fromTag(tag.getCompound("itemInv"))
        fluidInv.fromTag(tag.getCompound("fluidInv"))
        burningFuel = FluidGeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.put("itemInv", itemInv.toTag())
        tag.put("fluidInv", fluidInv.toTag())
        burningFuel?.let { tag.put("burningFuel", it.toTag()) }
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        super.fromClientTag(tag)
        itemInv.fromTag(tag.getCompound("itemInv"))
        fluidInv.fromTag(tag.getCompound("fluidInv"))
        burningFuel = FluidGeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }

}