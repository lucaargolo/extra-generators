package io.github.lucaargolo.extragenerators.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import com.google.gson.JsonObject
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf

data class FluidGeneratorFuel(val burnTime: Int, val fluidInput: FluidVolume, val energyOutput: Double) {

    var currentBurnTime = burnTime

    fun toTag(): CompoundTag = CompoundTag().also {
        it.putInt("burnTime", burnTime)
        it.putInt("currentBurnTime", currentBurnTime)
        it.put("fluidInput", fluidInput.toTag())
        it.putDouble("energyOutput", energyOutput)
    }

    fun toBuf(buf: PacketByteBuf) {
        buf.writeInt(burnTime)
        buf.writeInt(currentBurnTime)
        fluidInput.toMcBuffer(buf)
        buf.writeDouble(energyOutput)
    }

    companion object {

        fun fromTag(tag: CompoundTag): FluidGeneratorFuel? {
            val burnTime = tag.getInt("burnTime")
            val currentBurnTime = tag.getInt("currentBurnTime")
            val fluidInput = FluidVolume.fromTag(tag.getCompound("fluidInput"))
            val energyOutput = tag.getDouble("energyOutput")
            return if (energyOutput == 0.0) null else FluidGeneratorFuel(burnTime, fluidInput, energyOutput).also {
                it.currentBurnTime = currentBurnTime
            }
        }

        fun fromBuf(buf: PacketByteBuf): FluidGeneratorFuel? {
            val burnTime = buf.readInt()
            val currentBurnTime = buf.readInt()
            val fluidInput = FluidVolume.fromMcBuffer(buf)
            val energyOutput = buf.readDouble()
            return if (energyOutput == 0.0) null else FluidGeneratorFuel(burnTime, fluidInput, energyOutput).also {
                it.currentBurnTime = currentBurnTime
            }
        }

        fun fromJson(jsonObject: JsonObject): FluidGeneratorFuel? {
            val burnTime = jsonObject.get("burnTime").asInt
            val fluidInput = FluidVolume.fromJson(jsonObject.get("fluidInput").asJsonObject)
            val energyOutput = jsonObject.get("energyOutput").asDouble
            return if (energyOutput == 0.0) null else FluidGeneratorFuel(burnTime, fluidInput, energyOutput)
        }

        fun fromFluidResource(id: String, fluidKey: FluidKey): FluidGeneratorFuel? {
            return ResourceCompendium.FLUID_GENERATORS.test(id, fluidKey)
        }

        fun fromSteamGeneratorFuel(itemStack: ItemStack): FluidGeneratorFuel? {
            val burnTicks = FuelRegistryImpl.INSTANCE.get(itemStack.item) ?: return null
            return FluidGeneratorFuel(burnTicks/4, FluidKeys.WATER.withAmount(FluidAmount.of(burnTicks.toLong(), 1000)), burnTicks*40.0)
        }

    }

}