package io.github.lucaargolo.extragenerators.common.entity

import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.registry.Registry

object EntityCompendium: RegistryCompendium<EntityType<*>>(Registry.ENTITY_TYPE) {

    val GENERATOR_AREA_EFFECT_CLOUD = register ("generator_area_effect_cloud", FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type: EntityType<GeneratorAreaEffectCloudEntity>, world -> GeneratorAreaEffectCloudEntity(type, world) }.fireImmune().dimensions(EntityDimensions.changing(6.0F, 0.5F)).trackRangeBlocks(10).trackedUpdateRate(Integer.MAX_VALUE).build())

}