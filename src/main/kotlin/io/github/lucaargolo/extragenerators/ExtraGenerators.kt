package io.github.lucaargolo.extragenerators

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.extragenerators.common.containers.ScreenHandlerCompendium
import io.github.lucaargolo.extragenerators.common.entity.EntityCompendium
import io.github.lucaargolo.extragenerators.common.item.ItemCompendium
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.ModConfig
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files

class ExtraGenerators: ModInitializer {

    override fun onInitialize() {
        PacketCompendium.onInitialize()
        BlockCompendium.initialize()
        BlockEntityCompendium.initialize()
        EntityCompendium.initialize()
        ItemCompendium.initialize()
        ScreenHandlerCompendium.initialize()
        ResourceCompendium.initialize()
    }

    companion object {
        const val MOD_ID = "extragenerators"

        private val creativeTab = FabricItemGroupBuilder.create(ModIdentifier("creative_tab")).icon{ ItemStack(BlockCompendium.BURNABLE_GENERATOR) }.build()

        val PARSER = JsonParser()
        val GSON = GsonBuilder().setPrettyPrinting().create()
        val LOGGER: Logger = LogManager.getLogger("Extra Generators")
        val CONFIG: ModConfig by lazy {
            val configFile = File("${FabricLoader.getInstance().configDir}${File.separator}$MOD_ID.json")
            var finalConfig: ModConfig
            LOGGER.info("Trying to read config file...")
            try {
                if (configFile.createNewFile()) {
                    LOGGER.info("No config file found, creating a new one...")
                    val json: String = GSON.toJson(PARSER.parse(GSON.toJson(ModConfig())))
                    PrintWriter(configFile).use { out -> out.println(json) }
                    finalConfig = ModConfig()
                    LOGGER.info("Successfully created default config file.")
                } else {
                    LOGGER.info("A config file was found, loading it..")
                    finalConfig = GSON.fromJson(String(Files.readAllBytes(configFile.toPath())), ModConfig::class.java)
                    if (finalConfig == null) {
                        throw NullPointerException("The config file was empty.")
                    } else {
                        LOGGER.info("Successfully loaded config file.")
                    }
                }
            } catch (exception: Exception) {
                LOGGER.error("There was an error creating/loading the config file!", exception)
                finalConfig = ModConfig()
                LOGGER.warn("Defaulting to original config.")
            }
            finalConfig
        }

        fun creativeGroupSettings(): Item.Settings = Item.Settings().group(creativeTab)
    }

}