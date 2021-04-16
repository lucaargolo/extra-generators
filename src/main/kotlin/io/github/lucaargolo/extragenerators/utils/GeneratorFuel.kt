package io.github.lucaargolo.extragenerators.utils

import com.google.gson.JsonObject
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.PotionItem
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.potion.PotionUtil
import net.minecraft.util.math.MathHelper
import kotlin.math.*

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
            return if (energyOutput == 0.0) null else GeneratorFuel(burnTime, burnTime, energyOutput)
        }

        fun fromItemResource(id: String, itemStack: ItemStack): GeneratorFuel? {
            return ResourceCompendium.ITEM_GENERATORS.test(id, itemStack)
        }

        fun fromBurnableGeneratorFuel(item: Item): GeneratorFuel? {
            val burnTicks = FuelRegistryImpl.INSTANCE.get(item) ?: return null
            return GeneratorFuel(burnTicks/4, burnTicks/4, burnTicks*10.0)
        }

        fun fromGluttonyGeneratorFuel(item: Item): GeneratorFuel? {
            val foodComponent = item.foodComponent ?: return null
            val energyOutput = foodComponent.hunger * foodComponent.saturationModifier * 8000.0
            val energyPerTick = foodComponent.hunger * 8
            val burnTime = energyOutput/energyPerTick
            return GeneratorFuel(MathHelper.floor(burnTime), MathHelper.floor(burnTime), floor(energyOutput))
        }

        fun fromEnchantedGeneratorFuel(itemStack: ItemStack): GeneratorFuel? {
            val energyOutput = ceil(EnchantmentHelper.get(itemStack).map { (sqrt(min((it.value + 1), it.key.maxLevel) / it.key.maxLevel+0.0) * it.key.maxLevel * it.key.maxLevel * (it.value + 1)) / sqrt(it.key.rarity.weight+0.0) * max(1, it.key.getMinPower(it.value)) }.sum()) * 400
            val burnTime = MathHelper.ceil(energyOutput/40)
            return if(energyOutput <= 0.0) null else GeneratorFuel(burnTime, burnTime, energyOutput)
        }

        fun fromBrewGeneratorFuel(itemStack: ItemStack): GeneratorFuel? {
            val potionItem = itemStack.item as? PotionItem ?: return null
            val potion = PotionUtil.getPotion(itemStack)
            return null
        }

    }

}
