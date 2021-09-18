package io.github.lucaargolo.extragenerators.network

import io.github.lucaargolo.extragenerators.client.screen.*
import io.github.lucaargolo.extragenerators.common.blockentity.FluidGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.FluidItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.entity.GeneratorAreaEffectCloudEntity
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.InventoryUtils
import io.github.lucaargolo.extragenerators.utils.InventoryUtils.fluidResourceFromMcBuffer
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Identifier

object PacketCompendium {

    val UPDATE_ITEM_GENERATOR_SCREEN = ModIdentifier("update_item_generator_screen")
    val UPDATE_FLUID_GENERATOR_SCREEN = ModIdentifier("update_fluid_generator_screen")
    val UPDATE_FLUID_ITEM_GENERATOR_SCREEN = ModIdentifier("update_item_fluid_generator_screen")
    val UPDATE_COLORFUL_GENERATOR_SCREEN = ModIdentifier("update_colorful_generator_screen")
    val UPDATE_INFINITE_GENERATOR_SCREEN = ModIdentifier("update_infinite_generator_screen")
    val SPAWN_GENERATOR_AREA_EFFECT_CLOUD = ModIdentifier("spawn_generator_area_effect_cloud")

    val SYNC_ITEM_GENERATORS = ModIdentifier("sync_item_generators")
    val SYNC_FLUID_GENERATORS = ModIdentifier("sync_fluid_generators")
    val SYNC_BLOCK_TEMPERATURE = ModIdentifier("sync_block_temperature")

    val REQUEST_RESOURCES = ModIdentifier("request_resources")
    val INTERACT_CURSOR_WITH_TANK = ModIdentifier("interact_cursor_with_tank")

    fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_ITEM_GENERATOR_SCREEN) { client, _, buf, _ ->
            val long = buf.readLong()
            val burningFuel = GeneratorFuel.fromBuf(buf)
            client.execute {
                (client.currentScreen as? ItemGeneratorScreen)?.screenHandler?.let {
                    it.energyStored = long
                    it.burningFuel = burningFuel
                }
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_FLUID_GENERATOR_SCREEN) { client, _, buf, _ ->
            val long = buf.readLong()
            val burningFuel = FluidGeneratorFuel.fromBuf(buf)
            val fluidVolume = fluidResourceFromMcBuffer(buf)
            client.execute {
                (client.currentScreen as? FluidGeneratorScreen)?.screenHandler?.let {
                    it.energyStored = long
                    it.burningFuel = burningFuel
                    it.fluidVolume = fluidVolume
                }
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_FLUID_ITEM_GENERATOR_SCREEN) { client, _, buf, _ ->
            val long = buf.readLong()
            val burningFuel = FluidGeneratorFuel.fromBuf(buf)
            val fluidVolume = fluidResourceFromMcBuffer(buf)
            client.execute {
                (client.currentScreen as? FluidItemGeneratorScreen)?.screenHandler?.let {
                    it.energyStored = long
                    it.burningFuel = burningFuel
                    it.fluidVolume = fluidVolume
                }
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_COLORFUL_GENERATOR_SCREEN) { client, _, buf, _ ->
            val long = buf.readLong()
            val burningFuel = GeneratorFuel.fromBuf(buf)
            client.execute {
                (client.currentScreen as? ColorfulGeneratorScreen)?.screenHandler?.let {
                    it.energyStored = long
                    it.burningFuel = burningFuel
                }
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_INFINITE_GENERATOR_SCREEN) { client, _, buf, _ ->
            val long = buf.readLong()
            val activeGenerators = linkedMapOf<Identifier, Int>()
            repeat(buf.readInt()) {
                activeGenerators[buf.readIdentifier()] = buf.readInt()
            }
            client.execute {
                (client.currentScreen as? InfiniteGeneratorScreen)?.screenHandler?.let {
                    it.energyStored = long
                    it.activeGenerators = activeGenerators
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
                entity.id = id
                entity.uuid = uuid
                entity.blockEntityPos = blockEntityPos
                entity.generatorBlockEntity = world.getBlockEntity(blockEntityPos) as? ItemGeneratorBlockEntity
                world.addEntity(id, entity)
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(SYNC_ITEM_GENERATORS) { _, _, buf, _ ->
            ResourceCompendium.ITEM_GENERATORS.fromBuf(buf)
        }
        ClientPlayNetworking.registerGlobalReceiver(SYNC_FLUID_GENERATORS) { _, _, buf, _ ->
            ResourceCompendium.FLUID_GENERATORS.fromBuf(buf)
        }
        ClientPlayNetworking.registerGlobalReceiver(SYNC_BLOCK_TEMPERATURE) { _, _, buf, _ ->
            ResourceCompendium.BLOCK_TEMPERATURE.fromBuf(buf)
        }
    }

    fun onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_RESOURCES) { _, player, _, _, _ ->
            ServerPlayNetworking.send(player, SYNC_ITEM_GENERATORS, PacketByteBufs.create().also { ResourceCompendium.ITEM_GENERATORS.toBuf(it) })
            ServerPlayNetworking.send(player, SYNC_FLUID_GENERATORS, PacketByteBufs.create().also { ResourceCompendium.FLUID_GENERATORS.toBuf(it) })
            ServerPlayNetworking.send(player, SYNC_BLOCK_TEMPERATURE, PacketByteBufs.create().also { ResourceCompendium.BLOCK_TEMPERATURE.toBuf(it) })
        }

        ServerPlayNetworking.registerGlobalReceiver(INTERACT_CURSOR_WITH_TANK) { server, player, _, buf, _ ->
            val world = player.world
            val blockPos = buf.readBlockPos()
            server.execute {
                (world.getBlockEntity(blockPos) as? FluidGeneratorBlockEntity)?.let {
                    InventoryUtils.interactPlayerCursor(it.fluidInv, player, canExtract = false)
                }
                (world.getBlockEntity(blockPos) as? FluidItemGeneratorBlockEntity)?.let {
                    InventoryUtils.interactPlayerCursor(it.fluidInv, player, canExtract = false)
                }
            }
        }
    }

}