package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.common.block.AbstractGeneratorBlock
import io.github.lucaargolo.extragenerators.utils.ActiveGenerators
import io.github.lucaargolo.extragenerators.utils.ModConfig
import io.github.lucaargolo.extragenerators.utils.SynchronizeableBlockEntity
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
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import team.reborn.energy.*
import java.util.*
import kotlin.math.floor

abstract class AbstractGeneratorBlockEntity<B: AbstractGeneratorBlockEntity<B>>(type: BlockEntityType<B>, pos: BlockPos, state: BlockState): SynchronizeableBlockEntity(type, pos, state), EnergyStorage {

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

    var storedPower = 0.0
    override fun getMaxStoredPower() = generatorConfig?.storage ?: 0.0
    override fun getTier(): EnergyTier = EnergyTier.INSANE
    override fun getMaxInput(side: EnergySide?) = 0.0
    override fun getMaxOutput(side: EnergySide?) = generatorConfig?.output ?: 0.0
    override fun getStored(face: EnergySide?) = storedPower
    override fun setStored(amount: Double) { storedPower = amount }

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
                }
                world?.setBlockState(pos, Blocks.AIR.defaultState)
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

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        ownerUUID?.let { tag.putUuid("ownerUUID", it) }
        tag.putDouble("storedPower", storedPower)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        storedPower = tag.getDouble("storedPower")
        if(tag.contains("ownerUUID")) {
            ownerUUID = tag.getUuid("ownerUUID")
        }
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        ownerUUID?.let { tag.putUuid("ownerUUID", it) }
        tag.putDouble("storedPower", storedPower)
        tag.putBoolean("isClientRunning", isClientRunning)
        return tag
    }

    override fun fromClientTag(tag: NbtCompound) {
        storedPower = tag.getDouble("storedPower")
        isClientRunning = tag.getBoolean("isClientRunning")
        if(tag.contains("ownerUUID")) {
            ownerUUID = tag.getUuid("ownerUUID")
        }
    }

    private fun moveEnergy() {
        val sourceHandler = Energy.of(this)
        val targets = linkedMapOf<Direction, EnergyHandler>()
        Direction.values().forEach { direction ->
            val targetPos = pos.offset(direction)
            world?.getBlockEntity(targetPos)?.let { target ->
                if(Energy.valid(target)) {
                    val targetHandler = Energy.of(target).side(direction.opposite)
                    if (targetHandler.maxInput > 0 && targetHandler.energy < targetHandler.maxStored) {
                        targets[direction] = targetHandler
                    }
                }
            }
        }
        val transferAmount = floor(sourceHandler.energy.coerceAtMost(sourceHandler.maxOutput)/targets.size)
        targets.forEach { (direction, targetHandler) ->
            val maxTransferAmount = transferAmount.coerceAtMost(targetHandler.maxInput)
            if (targetHandler.energy + maxTransferAmount <= targetHandler.maxStored) {
                sourceHandler.side(direction).into(targetHandler).move(maxTransferAmount)
            }else{
                sourceHandler.side(direction).into(targetHandler).move(targetHandler.maxStored-targetHandler.energy)
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