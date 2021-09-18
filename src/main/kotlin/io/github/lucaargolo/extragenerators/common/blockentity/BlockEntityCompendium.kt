@file:Suppress("UNCHECKED_CAST", "DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.common.blockentity

import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import team.reborn.energy.api.EnergyStorage

object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registry.BLOCK_ENTITY_TYPE) {

    val ITEM_GENERATOR_TYPE = register("item_generator", BlockEntityType.Builder.create(::ItemGeneratorBlockEntity, *BlockCompendium.itemGeneratorArray() ).build(null)) as BlockEntityType<ItemGeneratorBlockEntity>
    val FLUID_GENERATOR_TYPE = register("fluid_generator", BlockEntityType.Builder.create(::FluidGeneratorBlockEntity, *BlockCompendium.fluidGeneratorArray() ).build(null)) as BlockEntityType<FluidGeneratorBlockEntity>
    val FLUID_ITEM_GENERATOR_TYPE = register("fluid_item_generator", BlockEntityType.Builder.create(::FluidItemGeneratorBlockEntity, *BlockCompendium.fluidItemGeneratorArray() ).build(null)) as BlockEntityType<FluidItemGeneratorBlockEntity>
    val COLORFUL_GENERATOR_TYPE = register("colorful_generator", BlockEntityType.Builder.create(::ColorfulGeneratorBlockEntity, BlockCompendium.COLORFUL_GENERATOR).build(null)) as BlockEntityType<ColorfulGeneratorBlockEntity>
    val THERMOELECTRIC_GENERATOR_TYPE = register("thermoelectric_generator", BlockEntityType.Builder.create(::ThermoelectricGeneratorBlockEntity, BlockCompendium.THERMOELECTRIC_GENERATOR ).build(null)) as BlockEntityType<ThermoelectricGeneratorBlockEntity>
    val INFINITE_GENERATOR_TYPE = register("infinite_generator", BlockEntityType.Builder.create(::InfiniteGeneratorBlockEntity, BlockCompendium.HEAVENLY_GENERATOR, BlockCompendium.INFERNAL_GENERATOR ).build(null)) as BlockEntityType<InfiniteGeneratorBlockEntity>

    init {
        FluidStorage.SIDED.registerForBlockEntity({ blockEntity: FluidGeneratorBlockEntity, _: Direction? -> blockEntity.fluidInv }, FLUID_GENERATOR_TYPE)
        FluidStorage.SIDED.registerForBlockEntity({ blockEntity: FluidItemGeneratorBlockEntity, _: Direction? -> blockEntity.fluidInv }, FLUID_ITEM_GENERATOR_TYPE)

        EnergyStorage.SIDED.registerForBlockEntities({ blockEntity: BlockEntity, _: Direction? -> (blockEntity as AbstractGeneratorBlockEntity<*>).energyStorage }, ITEM_GENERATOR_TYPE, FLUID_GENERATOR_TYPE, FLUID_ITEM_GENERATOR_TYPE, COLORFUL_GENERATOR_TYPE, THERMOELECTRIC_GENERATOR_TYPE, INFINITE_GENERATOR_TYPE)
    }

}