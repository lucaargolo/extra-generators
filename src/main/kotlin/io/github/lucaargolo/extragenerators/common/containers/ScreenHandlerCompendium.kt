package io.github.lucaargolo.extragenerators.common.containers

import io.github.lucaargolo.extragenerators.client.screen.*
import io.github.lucaargolo.extragenerators.common.blockentity.*
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry

@Suppress("UNCHECKED_CAST")
object ScreenHandlerCompendium: RegistryCompendium<ScreenHandlerType<*>>(Registry.SCREEN_HANDLER) {

    val ITEM_GENERATOR = register("item_generator", ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
        val pos = packetByteBuf.readBlockPos()
        val player = playerInventory.player
        val world = player.world
        val be = world.getBlockEntity(pos) as ItemGeneratorBlockEntity
        ItemGeneratorScreenHandler(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
    }) as ScreenHandlerType<ItemGeneratorScreenHandler>

    val FLUID_GENERATOR = register("fluid_generator", ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
        val pos = packetByteBuf.readBlockPos()
        val player = playerInventory.player
        val world = player.world
        val be = world.getBlockEntity(pos) as FluidGeneratorBlockEntity
        FluidGeneratorScreenHandler(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
    }) as ScreenHandlerType<FluidGeneratorScreenHandler>

    val FLUID_ITEM_GENERATOR = register("fluid_item_generator", ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
        val pos = packetByteBuf.readBlockPos()
        val player = playerInventory.player
        val world = player.world
        val be = world.getBlockEntity(pos) as FluidItemGeneratorBlockEntity
        FluidItemGeneratorScreenHandler(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
    }) as ScreenHandlerType<FluidItemGeneratorScreenHandler>

    val COLORFUL_GENERATOR = register("colorful_generator", ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
        val pos = packetByteBuf.readBlockPos()
        val player = playerInventory.player
        val world = player.world
        val be = world.getBlockEntity(pos) as ColorfulGeneratorBlockEntity
        ColorfulGeneratorScreenHandler(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
    }) as ScreenHandlerType<ColorfulGeneratorScreenHandler>

    val INFINITE_GENERATOR = register("infinite_generator", ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
        val pos = packetByteBuf.readBlockPos()
        val player = playerInventory.player
        val world = player.world
        val be = world.getBlockEntity(pos) as InfiniteGeneratorBlockEntity
        InfiniteGeneratorScreenHandler(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
    }) as ScreenHandlerType<InfiniteGeneratorScreenHandler>

    fun onInitializeClient() {
        HandledScreens.register(ITEM_GENERATOR, ::ItemGeneratorScreen)
        HandledScreens.register(FLUID_GENERATOR, ::FluidGeneratorScreen)
        HandledScreens.register(FLUID_ITEM_GENERATOR, ::FluidItemGeneratorScreen)
        HandledScreens.register(COLORFUL_GENERATOR, ::ColorfulGeneratorScreen)
        HandledScreens.register(INFINITE_GENERATOR, ::InfiniteGeneratorScreen)

    }

}