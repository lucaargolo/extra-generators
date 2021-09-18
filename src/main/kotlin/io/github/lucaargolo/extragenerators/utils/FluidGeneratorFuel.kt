@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.utils

import com.google.gson.JsonObject
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import kotlin.math.round

data class FluidGeneratorFuel(val burnTime: Int, var currentBurnTime: Int, val fluidInput: ResourceAmount<FluidVariant>, val energyOutput: Double) {

    constructor(burnTime: Int, fluidInput: ResourceAmount<FluidVariant>, energyOutput: Double): this(burnTime, burnTime, fluidInput, energyOutput)

    fun toTag(): NbtCompound = NbtCompound().also {
        it.putInt("burnTime", burnTime)
        it.putInt("currentBurnTime", currentBurnTime)
        it.put("fluidInput", fluidInput.toNbt())
        it.putDouble("energyOutput", energyOutput)
    }

    fun toBuf(buf: PacketByteBuf) {
        buf.writeInt(burnTime)
        buf.writeInt(currentBurnTime)
        fluidInput.toMcBuffer(buf)
        buf.writeDouble(energyOutput)
    }

    companion object {

        fun fromTag(tag: NbtCompound): FluidGeneratorFuel? {
            val burnTime = tag.getInt("burnTime")
            val currentBurnTime = tag.getInt("currentBurnTime")
            val fluidInput = InventoryUtils.fluidResourceFromNbt(tag.getCompound("fluidInput"))
            val energyOutput = tag.getDouble("energyOutput")
            return if (energyOutput == 0.0) null else FluidGeneratorFuel(burnTime, currentBurnTime, fluidInput, energyOutput)
        }

        fun fromBuf(buf: PacketByteBuf): FluidGeneratorFuel? {
            val burnTime = buf.readInt()
            val currentBurnTime = buf.readInt()
            val fluidInput = InventoryUtils.fluidResourceFromMcBuffer(buf)
            val energyOutput = buf.readDouble()
            return if (energyOutput == 0.0) null else FluidGeneratorFuel(burnTime, currentBurnTime, fluidInput, energyOutput)
        }

        fun fromJson(jsonObject: JsonObject): FluidGeneratorFuel? {
            val burnTime = jsonObject.get("burnTime").asInt
            val fluidInput = InventoryUtils.fluidResourceFromJson(jsonObject.get("fluidInput").asJsonObject)
            val energyOutput = jsonObject.get("energyOutput").asDouble
            return if (energyOutput == 0.0) null else FluidGeneratorFuel(burnTime, fluidInput, energyOutput)
        }

        fun fromFluidResource(id: String, fluid: Fluid): FluidGeneratorFuel? {
            return ResourceCompendium.FLUID_GENERATORS.test(id, fluid)
        }

        fun fromSteamGeneratorFuel(itemStack: ItemStack): FluidGeneratorFuel? {
            val burnTicks = FuelRegistryImpl.INSTANCE.get(itemStack.item) ?: return null
            return FluidGeneratorFuel(round(burnTicks/4.0).toInt(), ResourceAmount(FluidVariant.of(Fluids.WATER), burnTicks.toLong()*81), burnTicks*16.0)
        }

        fun fromRedstoneGeneratorFuel(itemStack: ItemStack): FluidGeneratorFuel? {
            return when(itemStack.item) {
                Items.REDSTONE -> FluidGeneratorFuel(100, ResourceAmount(FluidVariant.of(Fluids.LAVA), 50*81), 12800.0)
                Items.REDSTONE_BLOCK -> FluidGeneratorFuel(900, ResourceAmount(FluidVariant.of(Fluids.LAVA), 450*81), 115200.0)
                else -> null
            }
        }

    }

}