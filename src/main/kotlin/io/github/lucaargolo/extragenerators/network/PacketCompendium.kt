package io.github.lucaargolo.extragenerators.network

import io.github.lucaargolo.extragenerators.client.screen.ItemGeneratorScreen
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object PacketCompendium {

    val UPDATE_ITEM_GENERATOR_SCREEN = ModIdentifier("update_item_generator_screen")

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
    }

    fun onInitialize() {

    }

}