package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.math.abs

class ThermoelectricGeneratorBlockEntity(pos: BlockPos, state: BlockState): AbstractGeneratorBlockEntity<ThermoelectricGeneratorBlockEntity>(BlockEntityCompendium.THERMOELECTRIC_GENERATOR_TYPE, pos, state) {

    val axisTemperatureDifferenceCache = linkedMapOf<Direction.Axis, Int>()
    var clientGeneration = 0

    override fun isServerRunning() = getGeneration() > 0 && energyStorage.amount + getGeneration() <= energyStorage.getCapacity()

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
                if(energyStorage.amount + generation <= energyStorage.getCapacity()) {
                    energyStorage.amount += generation
                }
            }
        }
    }

    override fun writeNbt(tag: NbtCompound) {
        tag.putInt("clientGeneration", clientGeneration)
        super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        clientGeneration = tag.getInt("clientGeneration")
    }

}