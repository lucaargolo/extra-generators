package io.github.lucaargolo.extragenerators.common.blockentity

import alexiil.mc.lib.attributes.item.filter.ItemFilter
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv
import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.ItemGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.MathHelper

class ItemGeneratorBlockEntity: AbstractGeneratorBlockEntity<ItemGeneratorBlockEntity>(BlockEntityCompendium.ITEM_GENERATOR_TYPE) {

    private var itemFuelMap: ((ItemStack) -> GeneratorFuel?)? = null
    private var burnCallback: ((ItemGeneratorBlockEntity) -> Unit)? = null

    val itemInv = object: FullFixedItemInv(1) {
        override fun getFilterForSlot(slot: Int): ItemFilter = ItemFilter { initialized && (it.isEmpty || itemFuelMap?.invoke(it) != null) }
        override fun isItemValidForSlot(slot: Int, item: ItemStack) = getFilterForSlot(slot).matches(item)
    }

    var burningFuel: GeneratorFuel? = null

    override fun isServerRunning() = burningFuel?.let { storedPower + MathHelper.floor(it.energyOutput/it.burnTime) <= maxStoredPower } ?: false

    override fun getCogWheelRotation(): Float = burningFuel?.let { MathHelper.floor(it.energyOutput/it.burnTime)/10f } ?: 0f

    override fun initialize(block: AbstractGeneratorBlock): Boolean {
        val superInitialized = super.initialize(block)
        (block as? ItemGeneratorBlock)?.let {
            itemFuelMap = it.itemFuelMap
            burnCallback = it.burnCallback
        }
        return itemFuelMap != null && burnCallback != null && superInitialized
    }

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
                val stack = itemInv.extract(1)
                if (!stack.isEmpty) {
                    burningFuel = itemFuelMap?.invoke(stack)?.copy()
                    burningFuel?.let { burnCallback?.invoke(this) }
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