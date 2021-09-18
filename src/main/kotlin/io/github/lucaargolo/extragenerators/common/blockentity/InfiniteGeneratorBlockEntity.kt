package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.utils.ActiveGenerators
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class InfiniteGeneratorBlockEntity(pos: BlockPos, state: BlockState): AbstractGeneratorBlockEntity<InfiniteGeneratorBlockEntity>(BlockEntityCompendium.INFINITE_GENERATOR_TYPE, pos, state) {

    private var isInfinite = false

    override fun isServerRunning() = isInfinite && energyStorage.amount + 102400.0 <= energyStorage.getCapacity()

    override fun getCogWheelRotation(): Float = 102400/10f

    override fun tick() {
        super.tick()
        if(world?.isClient == false) {
            isInfinite = ownerUUID?.let { ActiveGenerators.test(it) } ?: false
            if(isInfinite && energyStorage.amount + 102400 <= energyStorage.getCapacity()) {
                energyStorage.amount += 102400
            }
            if(ownerUUID != null && generatorIdentifier != null) {
                ActiveGenerators.add(ownerUUID!!, generatorIdentifier!!)
            }
        }

    }

}