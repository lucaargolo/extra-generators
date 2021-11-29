@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.ItemGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.SimpleSidedInventory
import io.github.lucaargolo.extragenerators.utils.fromNbt
import io.github.lucaargolo.extragenerators.utils.toNbt
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

class ItemGeneratorBlockEntity(pos: BlockPos, state: BlockState): AbstractGeneratorBlockEntity<ItemGeneratorBlockEntity>(BlockEntityCompendium.ITEM_GENERATOR_TYPE, pos, state) {

    private var itemFuelMap: ((ItemStack) -> GeneratorFuel?)? = null
    private var burnCallback: ((ItemGeneratorBlockEntity) -> Unit)? = null

    val itemInv = SimpleSidedInventory(1, { _, stack ->
        initialized && (stack.isEmpty || itemFuelMap?.invoke(stack) != null)
    }, { _, _ ->  false }, { intArrayOf(0) })

    var burningFuel: GeneratorFuel? = null

    override fun isServerRunning() = burningFuel?.let { energyStorage.amount + MathHelper.floor(it.energyOutput/it.burnTime) <= energyStorage.getCapacity() } ?: false

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
                val stack = itemInv.getStack(0)
                if (!stack.isEmpty) {
                    burningFuel = itemFuelMap?.invoke(stack)?.copy()
                    burningFuel?.let { burnCallback?.invoke(this) }
                    stack.decrement(1)
                    markDirtyAndSync()
                }
            }
        }
    }

    override fun writeNbt(tag: NbtCompound) {
        tag.put("itemInv", itemInv.toNbt())
        burningFuel?.let { tag.put("burningFuel", it.toTag()) }
        super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        itemInv.fromNbt(tag.get("itemInv"))
        burningFuel = GeneratorFuel.fromTag(tag.getCompound("burningFuel"))
    }

}