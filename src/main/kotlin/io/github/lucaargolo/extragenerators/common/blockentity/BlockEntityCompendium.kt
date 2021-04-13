package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry

@Suppress("UNCHECKED_CAST")
object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registry.BLOCK_ENTITY_TYPE) {

    val ITEM_GENERATOR_TYPE = register("item_generator", BlockEntityType.Builder.create( { ItemGeneratorBlockEntity() }, BlockCompendium.FURNACE_GENERATOR, BlockCompendium.GLUTTONY_GENERATOR, BlockCompendium.ICY_GENERATOR ).build(null)) as BlockEntityType<ItemGeneratorBlockEntity>

}