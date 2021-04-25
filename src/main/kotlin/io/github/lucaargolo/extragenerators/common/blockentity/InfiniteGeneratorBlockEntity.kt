package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.utils.ActiveGenerators

class InfiniteGeneratorBlockEntity: AbstractGeneratorBlockEntity<InfiniteGeneratorBlockEntity>(BlockEntityCompendium.INFINITE_GENERATOR_TYPE) {

    private var isInfinite = false

    override fun isServerRunning() = isInfinite && storedPower + 102400.0 <= maxStoredPower

    override fun getCogWheelRotation(): Float = 102400/10f

    override fun tick() {
        super.tick()
        if(world?.isClient == false) {
            isInfinite = ownerUUID?.let { ActiveGenerators.test(it) } ?: false
            if(isInfinite && storedPower + 102400.0 <= maxStoredPower) {
                storedPower += 102400.0
            }
            if(ownerUUID != null && generatorIdentifier != null) {
                ActiveGenerators.add(ownerUUID!!, generatorIdentifier!!)
            }
        }

    }

}