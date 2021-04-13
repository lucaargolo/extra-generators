package io.github.lucaargolo.extragenerators.common.containers

import io.github.lucaargolo.extragenerators.client.screen.ItemGeneratorScreen
import io.github.lucaargolo.extragenerators.common.blockentity.ItemGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
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


    fun onInitializeClient() {
        ScreenRegistry.register(ITEM_GENERATOR) { handler, playerInventory, title -> ItemGeneratorScreen(handler, playerInventory, title) }
    }

}