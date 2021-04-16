package io.github.lucaargolo.extragenerators.common.blockentity

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidContainerRegistry
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv
import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.FluidGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag

class FluidGeneratorBlockEntity: AbstractGeneratorBlockEntity<FluidGeneratorBlockEntity>(BlockEntityCompendium.FLUID_GENERATOR_TYPE) {

    private var fluidFuelMap: ((FluidKey) -> FluidGeneratorFuel?)? = null

    val itemInv = object: FullFixedItemInv(2) {
        override fun getFilterForSlot(slot: Int): ItemFilter {
            return when(slot) {
                0 -> ItemFilter { initialized && (it.isEmpty || FluidContainerRegistry.getContainedFluid(it.item).let { volume -> !volume.isEmpty && fluidFuelMap?.invoke(volume.fluidKey) != null }) }
                1 -> ItemFilter { it.isEmpty || it.item == Items.BUCKET }
                else -> ItemFilter { true }
            }
        }
        override fun isItemValidForSlot(slot: Int, item: ItemStack) = getFilterForSlot(slot).matches(item)
    }

    val fluidInv = object: SimpleFixedFluidInv(1, FluidAmount.ofWhole(4)) {
        override fun getFilterForTank(tank: Int): FluidFilter = FluidFilter { initialized && (it.isEmpty || fluidFuelMap?.invoke(it) != null) }
        override fun isFluidValidForTank(tank: Int, fluid: FluidKey?) = getFilterForTank(tank).matches(fluid)
    }

    var burningFuel: FluidGeneratorFuel? = null

    override fun isServerRunning() = burningFuel?.let { storedPower + (it.energyOutput/it.burnTime) <= maxStoredPower } ?: false

    override fun getCogWheelRotation(): Float = burningFuel?.let { (it.energyOutput.toFloat()/it.burnTime)/10f } ?: 0f

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
                val energyPerTick = it.energyOutput / it.burnTime
                if (storedPower + energyPerTick <= maxStoredPower) {
                    storedPower += energyPerTick
                    it.currentBurnTime--
                }
                if (it.currentBurnTime <= 0) {
                    burningFuel = null
                    markDirtyAndSync()
                }
            }
            if (burningFuel == null) {
                val fluidKey = fluidInv.getInvFluid(0).fluidKey
                fluidFuelMap?.invoke(fluidKey)?.copy()?.let {
                    if(fluidInv.attemptAnyExtraction(it.fluidInput.amount(), Simulation.SIMULATE) == it.fluidInput) {
                        fluidInv.extract(it.fluidInput.amount())
                        burningFuel = it
                        markDirtyAndSync()
                    }
                }
            }
            if(!itemInv.getInvStack(0).isEmpty) {
                val inputStack = itemInv.getSlot(0).get()
                val stackFluid = FluidContainerRegistry.getContainedFluid(inputStack.item)
                if(fluidInv.getTank(0).attemptInsertion(stackFluid.copy(), Simulation.SIMULATE).isEmpty) {
                    val outputStack = itemInv.getSlot(1).get()
                    if (outputStack.isEmpty) {
                        itemInv.getSlot(0).set(inputStack.also { it.decrement(1) })
                        itemInv.getSlot(1).set(ItemStack(Items.BUCKET))
                        fluidInv.getTank(0).insert(stackFluid.copy())
                        markDirtyAndSync()
                    } else if (outputStack.isItemEqual(ItemStack(Items.BUCKET)) && outputStack.count + 1 < outputStack.maxCount) {
                        itemInv.getSlot(0).set(inputStack.also { it.decrement(1) })
                        itemInv.getSlot(1).set(outputStack.also { it.increment(1) })
                        fluidInv.getTank(0).insert(stackFluid.copy())
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