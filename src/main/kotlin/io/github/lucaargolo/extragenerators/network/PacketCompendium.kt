package io.github.lucaargolo.extragenerators.network

import io.github.lucaargolo.extragenerators.client.screen.ItemGeneratorScreen
import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.entity.GeneratorAreaEffectCloudEntity
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object PacketCompendium {

    val UPDATE_ITEM_GENERATOR_SCREEN = ModIdentifier("update_item_generator_screen")
    val SPAWN_GENERATOR_AREA_EFFECT_CLOUD = ModIdentifier("spawn_generator_area_effect_cloud")

    fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_ITEM_GENERATOR_SCREEN) { client, _, buf, _ ->
            val double = buf.readDouble()
            val burningFuel = GeneratorFuel.fromBuf(buf)
            client.execute {
                (client.currentScreen as? ItemGeneratorScreen)?.screenHandler?.let {
                    it.energyStored = double
                    it.burningFuel = burningFuel
                }
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(SPAWN_GENERATOR_AREA_EFFECT_CLOUD) { client, handler, buf, _ ->
            val id = buf.readVarInt()
            val uuid = buf.readUuid()
            val x = buf.readDouble()
            val y = buf.readDouble()
            val z = buf.readDouble()
            val pitch = buf.readByte().toInt()
            val yaw = buf.readByte().toInt()
            val blockEntityPos = buf.readBlockPos()

            client.execute {
                val world = handler.world
                val entity = GeneratorAreaEffectCloudEntity(world, x, y, z)

                entity.updateTrackedPosition(x, y, z)
                entity.refreshPositionAfterTeleport(x, y, z)
                entity.pitch = (pitch * 360f) / 256.0f
                entity.yaw = (yaw * 360f) / 256.0f
                entity.entityId = id
                entity.uuid = uuid
                entity.blockEntityPos = blockEntityPos
                entity.generatorBlockEntity = world.getBlockEntity(blockEntityPos) as? ItemGeneratorBlockEntity
                world.addEntity(id, entity)
            }
        }
    }

    fun onInitialize() {

    }

}