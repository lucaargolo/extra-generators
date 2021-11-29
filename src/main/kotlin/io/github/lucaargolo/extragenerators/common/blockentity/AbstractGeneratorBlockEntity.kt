@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.ActiveGenerators
import io.github.lucaargolo.extragenerators.utils.ModConfig
import io.github.lucaargolo.extragenerators.utils.SynchronizeableBlockEntity
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.EnergyStorageUtil
import team.reborn.energy.api.base.SimpleEnergyStorage
import java.util.*

abstract class AbstractGeneratorBlockEntity<B: AbstractGeneratorBlockEntity<B>>(type: BlockEntityType<B>, pos: BlockPos, state: BlockState): SynchronizeableBlockEntity(type, pos, state) {

    var ownerUUID: UUID? = null
    var initialized = false
        private set

    var generatorIdentifier: Identifier? = null
    private var generatorConfig: ModConfig.Generator? = null

    var lastCogWheelRotationDegree = 0f
    var cogWheelRotationDegree = 0f
    var isClientRunning = false

    open fun isRunning() = if(world?.isClient == true) isClientRunning else isServerRunning()
    abstract fun isServerRunning(): Boolean
    abstract fun getCogWheelRotation(): Float

    val energyStorage = object: SimpleEnergyStorage(0L, 0L, 0L) {
        private fun getMaxExtract(): Long {
            return generatorConfig?.output ?: super.maxExtract
        }

        override fun getCapacity(): Long {
            return generatorConfig?.storage ?: super.getCapacity()
        }

        override fun supportsExtraction(): Boolean {
            return getMaxExtract() > 0
        }

        @Suppress("DEPRECATION", "UnstableApiUsage")
        override fun extract(maxAmount: Long, transaction: TransactionContext?): Long {
            StoragePreconditions.notNegative(maxAmount)
            val extracted = getMaxExtract().coerceAtMost(maxAmount.coerceAtMost(amount))
            if (extracted > 0) {
                updateSnapshots(transaction)
                amount -= extracted
                return extracted
            }
            return 0
        }

        override fun onFinalCommit() {
            markDirtyAndSync()
            super.onFinalCommit()
        }
    }

    open fun initialize(block: AbstractGeneratorBlock): Boolean {
        (world?.getBlockState(pos)?.block as? AbstractGeneratorBlock)?.let {
            generatorConfig = it.generatorConfig
            generatorIdentifier = Registry.BLOCK.getId(it)
        }
        return generatorIdentifier != null && generatorConfig != null && ownerUUID != null
    }

    open fun tick() {
        //Check if generator was properly initialized
        if(!initialized) {
            initialized = (world?.getBlockState(pos)?.block as? AbstractGeneratorBlock)?.let { initialize(it) } ?: false
            if(!initialized) {
                ExtraGenerators.LOGGER.error("Failed to initialize generator! This is a bug.")
                (world as? ServerWorld)?.let { serverWorld ->
                    val stacks = Block.getDroppedStacks(cachedState, serverWorld, pos, this)
                    stacks.forEach {
                        ItemScatterer.spawn(serverWorld, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it)
                    }
                    world?.setBlockState(pos, Blocks.AIR.defaultState)
                }
            }
        }

        //If generator running state changed, sync block entity
        if(isClientRunning != isRunning()) {
            isClientRunning = isRunning()
            markDirtyAndSync()
        }

        //If generator is running on client, rotate cogwheel
        if(isRunning() && world?.isClient == true) {
            cogWheelRotationDegree += getCogWheelRotation()
            if(cogWheelRotationDegree >= 360f) {
                cogWheelRotationDegree %= 360f
                lastCogWheelRotationDegree -= 360f
            }
            world?.addParticle(ParticleTypes.SMOKE, pos.x+0.5, pos.y+0.825, pos.z+0.5, 0.0, 0.1, 0.0)
        }

        //If generator is on server move energy and if its running add it to active generators
        if(world?.isClient == false) {
            moveEnergy()
            if(isRunning() && ownerUUID != null && generatorIdentifier != null && this !is InfiniteGeneratorBlockEntity) {
                ActiveGenerators.add(ownerUUID!!, generatorIdentifier!!)
            }
        }
    }

    override fun writeNbt(tag: NbtCompound) {
        ownerUUID?.let { tag.putUuid("ownerUUID", it) }
        tag.putLong("storedEnergy", energyStorage.amount)
        tag.putBoolean("isClientRunning", isClientRunning)
        super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        if(tag.contains("storedPower")) {
            //Found a generator using the old energy system
            energyStorage.amount = MathHelper.floor(tag.getDouble("storedPower")).toLong()
        }else{
            energyStorage.amount = tag.getLong("storedEnergy")
        }
        if(tag.contains("ownerUUID")) {
            ownerUUID = tag.getUuid("ownerUUID")
        }
        isClientRunning = tag.getBoolean("isClientRunning")
    }


    private fun moveEnergy() {
        val targets = linkedSetOf<EnergyStorage>()
        Direction.values().forEach { direction ->
            val targetPos = pos.offset(direction)
            EnergyStorage.SIDED.find(world, targetPos, direction.opposite)?.let { target ->
                if(target.supportsInsertion() && target.amount < target.capacity) {
                    targets.add(target)
                }
            }
        }
        if(targets.size > 0) {
            val transferAmount = energyStorage.amount.coerceAtMost(generatorConfig?.output ?: energyStorage.maxExtract) / targets.size
            targets.forEach { target ->
                EnergyStorageUtil.move(energyStorage, target, transferAmount, null)
            }
        }
    }

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun <G: AbstractGeneratorBlockEntity<G>> commonTick(world: World, pos: BlockPos, state: BlockState, entity: G) {
            entity.tick()
        }
    }

}