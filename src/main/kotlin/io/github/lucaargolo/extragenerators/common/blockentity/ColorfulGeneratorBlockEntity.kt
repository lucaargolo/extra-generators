package io.github.lucaargolo.extragenerators.common.blockentity

import alexiil.mc.lib.attributes.item.filter.ItemFilter
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv
import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.MathHelper

class ColorfulGeneratorBlockEntity: AbstractGeneratorBlockEntity<ColorfulGeneratorBlockEntity>(BlockEntityCompendium.COLORFUL_GENERATOR_TYPE) {

    val itemInv = object: FullFixedItemInv(3) {
        override fun getFilterForSlot(slot: Int): ItemFilter = when(slot) {
            0 -> ItemFilter { initialized && (it.isEmpty || ExtraGenerators.RED_ITEMS.contains(it.item)) }
            1 -> ItemFilter { initialized && (it.isEmpty || ExtraGenerators.GREEN_ITEMS.contains(it.item)) }
            2 -> ItemFilter { initialized && (it.isEmpty || ExtraGenerators.BLUE_ITEMS.contains(it.item)) }
            else -> ItemFilter { true }
        }
        override fun isItemValidForSlot(slot: Int, item: ItemStack) = getFilterForSlot(slot).matches(item)
    }

    var burningFuel: GeneratorFuel? = null

    override fun isServerRunning() = burningFuel?.let { storedPower + MathHelper.floor(it.energyOutput/it.burnTime) <= maxStoredPower } ?: false

    override fun getCogWheelRotation(): Float = burningFuel?.let { MathHelper.floor(it.energyOutput/it.burnTime)/10f } ?: 0f

    override fun tick() {
        super.tick()
        if(world?.isClient == false) {
            burningFuel?.let {
                val energyPerTick = MathHelper.floor(it.energyOutput/it.burnTime)
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
                val redStack = itemInv.getInvStack(0)
                val greenStack = itemInv.getInvStack(1)
                val blueStack = itemInv.getInvStack(2)
                if (!redStack.isEmpty && !greenStack.isEmpty && !blueStack.isEmpty) {
                    itemInv.getSlot(0).extract(1)
                    itemInv.getSlot(1).extract(1)
                    itemInv.getSlot(2).extract(1)
                    burningFuel = GeneratorFuel(400, 100000.0)
                    markDirtyAndSync()
                }
            }
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.put("itemInv", itemInv.toTag())
        burningFuel?.let { tag.put("burningFuel", it.toTag()) }
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        itemInv.fromTag(tag.getCompound("itemInv"))
        burningFuel = GeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.put("itemInv", itemInv.toTag())
        burningFuel?.let { tag.put("burningFuel", it.toTag()) }
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        super.fromClientTag(tag)
        itemInv.fromTag(tag.getCompound("itemInv"))
        burningFuel = GeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }

}