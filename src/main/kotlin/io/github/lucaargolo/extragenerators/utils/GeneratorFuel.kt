package io.github.lucaargolo.extragenerators.utils

import com.google.gson.JsonObject
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.potion.PotionUtil
import net.minecraft.potion.Potions
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import kotlin.math.*

data class GeneratorFuel(val burnTime: Int, var currentBurnTime: Int, val energyOutput: Double) {

    constructor(burnTime: Int, energyOutput: Double): this(burnTime, burnTime, energyOutput)

    fun toTag(): CompoundTag = CompoundTag().also {
        it.putInt("burnTime", burnTime)
        it.putInt("currentBurnTime", currentBurnTime)
        it.putDouble("energyOutput", energyOutput)
    }

    fun toBuf(buf: PacketByteBuf) {
        buf.writeInt(burnTime)
        buf.writeInt(currentBurnTime)
        buf.writeDouble(energyOutput)
    }

    companion object {

        fun fromTag(tag: CompoundTag): GeneratorFuel? {
            val burnTime = tag.getInt("burnTime")
            val currentBurnTime = tag.getInt("currentBurnTime")
            val energyOutput = tag.getDouble("energyOutput")
            return if(energyOutput == 0.0) null else GeneratorFuel(burnTime, currentBurnTime, energyOutput)
        }

        fun fromBuf(buf: PacketByteBuf): GeneratorFuel? {
            val burnTime = buf.readInt()
            val currentBurnTime = buf.readInt()
            val energyOutput = buf.readDouble()
            return if(energyOutput == 0.0) null else GeneratorFuel(burnTime, currentBurnTime, energyOutput)
        }

        fun fromJson(jsonObject: JsonObject): GeneratorFuel? {
            val burnTime = jsonObject.get("burnTime").asInt
            val energyOutput = jsonObject.get("energyOutput").asDouble
            return if (energyOutput == 0.0) null else GeneratorFuel(burnTime, energyOutput)
        }

        fun fromItemResource(id: String, itemStack: ItemStack): GeneratorFuel? {
            return ResourceCompendium.ITEM_GENERATORS.test(id, itemStack)
        }

        fun fromBurnableGeneratorFuel(item: Item): GeneratorFuel? {
            val burnTicks = FuelRegistryImpl.INSTANCE.get(item) ?: return null
            return GeneratorFuel(burnTicks/4, burnTicks*10.0)
        }

        fun fromGluttonyGeneratorFuel(item: Item): GeneratorFuel? {
            val foodComponent = item.foodComponent ?: return null
            val energyOutput = foodComponent.hunger * foodComponent.saturationModifier * 8000.0
            val energyPerTick = max(foodComponent.hunger * 8, 128)
            val burnTime = energyOutput/energyPerTick
            return GeneratorFuel(MathHelper.floor(burnTime), floor(energyOutput))
        }

        fun fromEnchantedGeneratorFuel(itemStack: ItemStack): GeneratorFuel? {
            val energyOutput = EnchantmentHelper.get(itemStack).map {
                val a = sqrt(min((it.value + 1.0), it.key.maxLevel+0.0) / it.key.maxLevel+0.0)
                val b = it.key.maxLevel * it.key.maxLevel
                val c = (it.value + 1)
                val d = (max(1.0, it.key.getMinPower(it.value)+0.0) / sqrt(it.key.rarity.weight+0.0))
                ceil(a*b*c*d) * 400
            }.sum()
            val energyTick = when(energyOutput) {
                in (0.0..3200.0) -> 8.0
                in (3200.0..12800.0) -> 16.0
                in (12800.0..38400.0) -> 32.0
                in (38400.0..102400.0) -> 64.0
                in (102400.0..256000.0) -> 128.0
                else -> 256.0
            }
            val burnTime = MathHelper.ceil(energyOutput/energyTick)
            return if(energyOutput <= 0.0) null else GeneratorFuel(burnTime, energyOutput)
        }

        fun fromBrewGeneratorFuel(itemStack: ItemStack): GeneratorFuel? {
            val potion = PotionUtil.getPotion(itemStack)
            var level = 0
            if(itemStack.item is SplashPotionItem) level = 1
            if(itemStack.item is LingeringPotionItem) level = 2
            val steps = when {
                potion == Potions.EMPTY -> return null
                listOf(Potions.WATER, Potions.MUNDANE, Potions.THICK, Potions.AWKWARD).contains(potion) -> 0
                else -> {
                    val potionPath = Registry.POTION.getId(potion).path
                    when {
                        potionPath.startsWith("long") -> 2
                        potionPath.startsWith("strong") -> 2
                        else -> 1
                    }
                }
            } + level
            val energyTick = 8 * 2.0.pow(steps)
            val burnTime = 400*(steps+1)
            val energyOutput = energyTick * burnTime
            return if(energyOutput <= 0.0) null else GeneratorFuel(burnTime, energyOutput)
        }

    }

}
