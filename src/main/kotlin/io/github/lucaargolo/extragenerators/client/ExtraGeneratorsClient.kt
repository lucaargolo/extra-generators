package io.github.lucaargolo.extragenerators.client

import io.github.lucaargolo.extragenerators.client.render.bakedmodel.BakedModelCompendium
import io.github.lucaargolo.extragenerators.client.render.blockentity.BlockEntityRendererCompendium
import io.github.lucaargolo.extragenerators.client.render.entity.EntityRendererCompendium
import io.github.lucaargolo.extragenerators.common.containers.ScreenHandlerCompendium
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl
import net.minecraft.client.util.ModelIdentifier

class ExtraGeneratorsClient: ClientModInitializer {

    override fun onInitializeClient() {
        PacketCompendium.onInitializeClient()
        ScreenHandlerCompendium.onInitializeClient()
        BlockEntityRendererCompendium.initialize()
        EntityRendererCompendium.initialize()
        BakedModelCompendium.initialize()

        ModelLoadingRegistry.INSTANCE.registerModelProvider { _, out ->
            out.accept(ModelIdentifier(ModIdentifier("generator_wheels"), ""))
        }
    }

}