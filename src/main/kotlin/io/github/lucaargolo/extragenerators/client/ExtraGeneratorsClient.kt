package io.github.lucaargolo.extragenerators.client

import io.github.lucaargolo.extragenerators.client.render.bakedmodel.BakedModelCompendium
import io.github.lucaargolo.extragenerators.client.render.blockentity.BlockEntityRendererCompendium
import io.github.lucaargolo.extragenerators.common.containers.ScreenHandlerCompendium
import io.github.lucaargolo.extragenerators.network.PacketCompendium
import net.fabricmc.api.ClientModInitializer

class ExtraGeneratorsClient: ClientModInitializer {

    override fun onInitializeClient() {
        PacketCompendium.onInitializeClient()
        ScreenHandlerCompendium.onInitializeClient()
        BlockEntityRendererCompendium.initialize()
        BakedModelCompendium.initialize()
    }

}