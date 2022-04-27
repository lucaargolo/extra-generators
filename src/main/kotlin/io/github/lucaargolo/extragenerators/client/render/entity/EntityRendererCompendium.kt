package io.github.lucaargolo.extragenerators.client.render.entity

import io.github.lucaargolo.extragenerators.common.entity.EntityCompendium
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.EmptyEntityRenderer

object EntityRendererCompendium {

    fun initialize() {
        EntityRendererRegistry.register(EntityCompendium.GENERATOR_AREA_EFFECT_CLOUD, ::EmptyEntityRenderer)
    }

}