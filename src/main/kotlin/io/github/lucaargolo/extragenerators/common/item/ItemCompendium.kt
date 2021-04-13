package io.github.lucaargolo.extragenerators.common.item

import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry

object ItemCompendium: RegistryCompendium<Item>(Registry.ITEM) {

    override fun initialize() {
        BlockCompendium.registerBlockItems(map)
        super.initialize()
    }

}