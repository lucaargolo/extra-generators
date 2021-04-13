package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.common.block.ItemGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.ModConfig
import io.github.lucaargolo.extragenerators.utils.SynchronizeableBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.Tickable
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

abstract class AbstractGeneratorBlockEntity<B: AbstractGeneratorBlockEntity<B>>(type: BlockEntityType<B>): SynchronizeableBlockEntity(type), Tickable, EnergyStorage {

    var initialized = false
        private set
    private var generatorConfig: ModConfig.Generator? = null

    var cogWheelRotationDegree = 0
    abstract fun isRunning(): Boolean

    var storedPower = 0.0
    override fun getMaxStoredPower() = generatorConfig?.maxStoredPower ?: 0.0
    override fun getTier(): EnergyTier = generatorConfig?.getEnumTier() ?: EnergyTier.MICRO
    override fun getMaxInput(side: EnergySide?) = 0.0
    override fun getStored(face: EnergySide?) = storedPower
    override fun setStored(amount: Double) { storedPower = amount }

    open fun initialize(block: AbstractGeneratorBlock): Boolean {
        generatorConfig = (world?.getBlockState(pos)?.block as? ItemGeneratorBlock)?.generatorConfig
        return generatorConfig != null
    }

    override fun tick() {
        if(isRunning()) {
            if(cogWheelRotationDegree++ >= 360) {
                cogWheelRotationDegree = 0
            }
            world?.addParticle(ParticleTypes.SMOKE, pos.x+0.5, pos.y+0.825, pos.z+0.5, 0.0, 0.1, 0.0)
        }
        if(!initialized) {
            initialized = (world?.getBlockState(pos)?.block as? AbstractGeneratorBlock)?.let { initialize(it) } ?: false
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putDouble("storedPower", storedPower)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        storedPower = tag.getDouble("storedPower")
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.putDouble("storedPower", storedPower)
        return tag
    }

    override fun fromClientTag(tag: CompoundTag) {
        storedPower = tag.getDouble("storedPower")
    }

}