package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import net.minecraft.block.Blocks
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Direction
import kotlin.math.abs

class ThermoelectricGeneratorBlockEntity: AbstractGeneratorBlockEntity<ThermoelectricGeneratorBlockEntity>(BlockEntityCompendium.THERMOELECTRIC_GENERATOR_TYPE) {

    val axisTemperatureDifferenceCache = linkedMapOf<Direction.Axis, Int>()
    var clientGeneration = 0

    override fun isServerRunning() = getGeneration() > 0 && storedPower + getGeneration() <= maxStoredPower

    override fun getCogWheelRotation(): Float = clientGeneration/10f

    fun getGeneration(): Int = axisTemperatureDifferenceCache.map { it.value }.sum()/100

    override fun tick() {
        super.tick()
        if(world?.isClient == false) {
            Direction.Axis.values().forEach {
                if(axisTemperatureDifferenceCache[it] == null) {
                    val b = when(it) {
                        Direction.Axis.X -> Pair(pos.east(), pos.west())
                        Direction.Axis.Y -> Pair(pos.up(), pos.down())
                        Direction.Axis.Z -> Pair(pos.north(), pos.south())
                    }
                    val firstTemperature = ResourceCompendium.BLOCK_TEMPERATURE.test(world?.getBlockState(b.first)?.block ?: Blocks.AIR)
                    val secondTemperature = ResourceCompendium.BLOCK_TEMPERATURE.test(world?.getBlockState(b.second)?.block ?: Blocks.AIR)
                    if(firstTemperature == null || secondTemperature == null) {
                        axisTemperatureDifferenceCache[it] = 0
                    }else{
                        axisTemperatureDifferenceCache[it] = abs(firstTemperature - secondTemperature)
                    }
                }
            }
            getGeneration().let { generation ->
                if(clientGeneration != generation) {
                    clientGeneration = generation
                    markDirtyAndSync()
                }
                if(storedPower + generation <= maxStoredPower) {
                    storedPower += generation
                }
            }
        }
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.putInt("clientGeneration", clientGeneration)
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag) {
        super.fromClientTag(tag)
        clientGeneration = tag.getInt("clientGeneration")
    }

}