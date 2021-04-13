package io.github.lucaargolo.extragenerators.client.render.bakedmodel

import com.mojang.datafixers.util.Pair
import io.github.lucaargolo.extragenerators.utils.GenericCompendium
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import java.util.function.Function

object BakedModelCompendium: GenericCompendium<BakedModel>() {

//    private val SCREEN_MODEL = ScreenBakedModel()
//    private val ELECTRIC_LIFT_MODEL = ElectricLiftBakedModel()
//
//    init {
//        register(ModIdentifier("screen"), SCREEN_MODEL)
//        register(ModIdentifier("electric_lift_mk1"), ELECTRIC_LIFT_MODEL)
//        register(ModIdentifier("electric_lift_mk2"), ELECTRIC_LIFT_MODEL)
//        register(ModIdentifier("electric_lift_mk3"), ELECTRIC_LIFT_MODEL)
//        register(ModIdentifier("electric_lift_mk4"), ELECTRIC_LIFT_MODEL)
//        register(ModIdentifier("electric_lift_mk5"), ELECTRIC_LIFT_MODEL)
//    }

    override fun initialize() {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider {
            ModelVariantProvider { modelIdentifier, _ ->
                map.forEach { (identifier, model) ->
                    val equals = if(identifier is ModelIdentifier) {
                        identifier.namespace == modelIdentifier.namespace && modelIdentifier.path == identifier.path && identifier.variant == modelIdentifier.variant
                    } else {
                        identifier.namespace == modelIdentifier.namespace && modelIdentifier.path == identifier.path
                    }
                    if(equals) {
                        return@ModelVariantProvider (model as? UnbakedModel) ?: object : UnbakedModel {
                            override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
                            override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationScreenHandler: ModelBakeSettings, modelId: Identifier) = model
                            override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>?, unresolvedTextureReferences: MutableSet<Pair<String, String>>?): MutableCollection<SpriteIdentifier> = mutableListOf()
                        }
                    }
                }
                return@ModelVariantProvider null
            }
        }
    }

}