package io.github.lucaargolo.extragenerators.common.entity

import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.AreaEffectCloudEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class GeneratorAreaEffectCloudEntity(entityType: EntityType<GeneratorAreaEffectCloudEntity>, world: World): AreaEffectCloudEntity(entityType, world) {

    var blockEntityPos: BlockPos? = null
    var generatorBlockEntity: ItemGeneratorBlockEntity? = null

    constructor(world: World, x: Double, y: Double, z: Double): this(EntityCompendium.GENERATOR_AREA_EFFECT_CLOUD, world) {
        updatePosition(x, y, z)
    }

    override fun tick() {
        val gbe = generatorBlockEntity ?: blockEntityPos?.let{ world.getBlockEntity(it) as? ItemGeneratorBlockEntity}?.also { generatorBlockEntity = it } ?: return this.remove()
        when {
            gbe.isRemoved -> this.remove()
            gbe.isRunning() -> super.tick()
            else -> --this.age
        }
    }

    override fun writeCustomDataToTag(tag: CompoundTag) {
        super.writeCustomDataToTag(tag)
        blockEntityPos?.let { tag.putLong("blockEntityPos", it.asLong()) }
    }

    override fun readCustomDataFromTag(tag: CompoundTag) {
        super.readCustomDataFromTag(tag)
        if(tag.contains("blockEntityPos")) {
            blockEntityPos = BlockPos.fromLong(tag.getLong("blockEntityPos"))
        }
    }

    override fun createSpawnPacket(): Packet<*> {
        val buf = PacketByteBufs.create()
        buf.writeVarInt(entityId)
        buf.writeUuid(getUuid())
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeByte(MathHelper.floor(pitch * 256.0f / 360.0f))
        buf.writeByte(MathHelper.floor(yaw * 256.0f / 360.0f))
        buf.writeBlockPos(blockEntityPos!!)

        return ServerPlayNetworking.createS2CPacket(PacketCompendium.SPAWN_GENERATOR_AREA_EFFECT_CLOUD, buf)
    }

    companion object {

        fun createAndSpawn(world: World, generatorBlockEntity: ItemGeneratorBlockEntity, statusEffect: StatusEffect) {
            val cloud = GeneratorAreaEffectCloudEntity(world, generatorBlockEntity.pos.x+0.5, generatorBlockEntity.pos.y+0.0, generatorBlockEntity.pos.z+0.5)
            cloud.generatorBlockEntity = generatorBlockEntity
            cloud.blockEntityPos = generatorBlockEntity.pos
            cloud.radius = 3.0f
            cloud.duration = generatorBlockEntity.burningFuel?.burnTime ?: 0
            cloud.addEffect(StatusEffectInstance(StatusEffectInstance(statusEffect, 200)))
            world.spawnEntity(cloud)
        }

    }

}