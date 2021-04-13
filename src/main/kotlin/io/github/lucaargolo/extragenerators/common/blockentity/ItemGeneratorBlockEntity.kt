package io.github.lucaargolo.extragenerators.common.blockentity

import alexiil.mc.lib.attributes.item.filter.ItemFilter
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv
import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.ItemGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag

class ItemGeneratorBlockEntity: AbstractGeneratorBlockEntity<ItemGeneratorBlockEntity>(BlockEntityCompendium.ITEM_GENERATOR_TYPE) {

    private var itemFuelMap: ((itemStack: ItemStack) -> GeneratorFuel?)? = null

    val itemInv = object: FullFixedItemInv(1) {
        override fun getFilterForSlot(slot: Int): ItemFilter = ItemFilter { initialized && (it.isEmpty || itemFuelMap?.invoke(it) != null) }
        override fun isItemValidForSlot(slot: Int, item: ItemStack) = getFilterForSlot(slot).matches(item)
    }

    var burningFuel: GeneratorFuel? = null

    override fun isRunning() = burningFuel?.let { storedPower + (it.energyOutput/it.totalBurnTime) <= maxStoredPower } ?: false

    override fun initialize(block: AbstractGeneratorBlock): Boolean {
        val superInitialized = super.initialize(block)
        itemFuelMap = (block as? ItemGeneratorBlock)?.itemFuelMap
        return itemFuelMap != null && superInitialized
    }

    override fun tick() {
        super.tick()
        if(!initialized) return
        burningFuel?.let {
            val energyPerTick = it.energyOutput/it.totalBurnTime
            if(storedPower + energyPerTick <= maxStoredPower) {
                storedPower += energyPerTick
                it.burnTime--
            }
            if(it.burnTime <= 0) {
                burningFuel = null
                markDirtyAndSync()
            }
        }
        if(burningFuel == null) {
            val stack = itemInv.extract(1)
            if(!stack.isEmpty) {
                burningFuel = itemFuelMap?.invoke(stack)?.copy()
                markDirtyAndSync()
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
        return tag
    }

    override fun fromClientTag(tag: CompoundTag) {
        itemInv.fromTag(tag.getCompound("itemInv"))
        burningFuel = GeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }

}