package io.github.lucaargolo.extragenerators.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import kotlin.math.floor

data class GeneratorFuel(val totalBurnTime: Int, var burnTime: Int, val energyOutput: Double) {

    fun toTag(): CompoundTag = CompoundTag().also {
        it.putInt("totalBurnTime", totalBurnTime)
        it.putInt("burnTime", burnTime)
        it.putDouble("energyOutput", energyOutput)
    }

    fun toBuf(buf: PacketByteBuf) {
        buf.writeInt(totalBurnTime)
        buf.writeInt(burnTime)
        buf.writeDouble(energyOutput)
    }

    companion object {

        fun fromTag(tag: CompoundTag): GeneratorFuel? {
            val totalBurnTime = tag.getInt("totalBurnTime")
            val burnTime = tag.getInt("burnTime")
            val energyOutput = tag.getDouble("energyOutput")
            return if(energyOutput == 0.0) null else GeneratorFuel(totalBurnTime, burnTime, energyOutput)
        }

        fun fromBuf(buf: PacketByteBuf): GeneratorFuel? {
            val totalBurnTime = buf.readInt()
            val burnTime = buf.readInt()
            val energyOutput = buf.readDouble()
            return if(energyOutput == 0.0) null else GeneratorFuel(totalBurnTime, burnTime, energyOutput)
        }

        fun fromJson(jsonObject: JsonObject): GeneratorFuel? {
            val burnTime = jsonObject.get("burnTime").asInt
            val energyOutput = jsonObject.get("energyOutput").asDouble
            return if(energyOutput == 0.0) null else GeneratorFuel(burnTime, burnTime, energyOutput)
        }

        fun fromResource(id: String, itemStack: ItemStack): GeneratorFuel? {
            return ResourceCompendium.ITEM_GENERATORS.test(id, itemStack)
        }

        fun fromFurnaceGeneratorFuel(item: Item): GeneratorFuel? {
            val burnTicks = FuelRegistryImpl.INSTANCE.get(item) ?: return null
            return GeneratorFuel(burnTicks/4, burnTicks/4, burnTicks*10.0)
        }

        fun fromCulinaryGeneratorFuel(item: Item): GeneratorFuel? {
            val foodComponent = item.foodComponent ?: return null
            val energyOutput = foodComponent.hunger * foodComponent.saturationModifier * 8000.0
            val energyPerTick = foodComponent.hunger * 8
            val burnTime = energyOutput/energyPerTick
            return GeneratorFuel(MathHelper.floor(burnTime), MathHelper.floor(burnTime), floor(energyOutput))
        }

        fun fromDisenchantmentGeneratorFuel(itemStack: ItemStack): GeneratorFuel? {
            val enchantments = EnchantedBookItem.getEnchantmentTag(itemStack)
            enchantments?.forEach {
                val compoundTag = it as? CompoundTag ?: return@forEach
                Registry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(compoundTag.getString("id"))).ifPresent { enchantment: Enchantment ->

                }
                return null
            }
            return null
        }

    }

}
