package io.github.lucaargolo.extragenerators.common.resource

import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.io.InputStreamReader

class BlockTemperatureResource: SimpleSynchronousResourceReloadListener {

    private val temperatureMap = linkedMapOf<Block, Int>()
    val clientTemperatureMap = linkedMapOf<Block, Int>()

    fun test(block: Block) = temperatureMap[block]

    fun toBuf(buf: PacketByteBuf) {
        buf.writeInt(temperatureMap.size)
        temperatureMap.forEach { (block, temperature) ->
            buf.writeVarInt(Registry.BLOCK.getRawId(block))
            buf.writeInt(temperature)
        }
    }

    fun fromBuf(buf: PacketByteBuf) {
        clientTemperatureMap.clear()
        val temperatureMapSize = buf.readInt()
        repeat(temperatureMapSize) {
            val block = Registry.BLOCK.get(buf.readVarInt())
            val temperature = buf.readInt()
            clientTemperatureMap[block] = temperature
        }
    }

    override fun getFabricId() = ModIdentifier("block_temperature")

    override fun reload(manager: ResourceManager) {
        temperatureMap.clear()
        ExtraGenerators.LOGGER.info("Loading block temperature resource.")
        manager.findResources("block_temperature") { r -> r.path.endsWith(".json") }.forEach { itemsResource ->
            val id = itemsResource.key.path.split("/").lastOrNull()?.replace(".json", "") ?: return@forEach
            val resource = itemsResource.value
            ExtraGenerators.LOGGER.info("Loading $id block temperature resource at $itemsResource.")
            try {
                val json = ExtraGenerators.PARSER.parse(InputStreamReader(resource.inputStream, "UTF-8"))
                val jsonArray = json.asJsonArray
                jsonArray.forEach { jsonElement ->
                    val jsonObject = jsonElement.asJsonObject
                    val block = Registry.BLOCK.get(Identifier(jsonObject.get("block").asString))
                    val temperature = jsonObject.get("temperature").asInt
                    temperatureMap[block] = temperature
                }
            }catch (e: Exception) {
                ExtraGenerators.LOGGER.error("Unknown error while trying to read $id block temperature resource at ${itemsResource.key}", e)
            }
        }
        ExtraGenerators.LOGGER.info("Finished loading block temperature resource (${temperatureMap.size} entries loaded).")
    }

}