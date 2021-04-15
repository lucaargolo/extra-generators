package io.github.lucaargolo.extragenerators.common.block

import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.ExtraGenerators.Companion.creativeGroupSettings
import io.github.lucaargolo.extragenerators.common.entity.GeneratorAreaEffectCloudEntity
import io.github.lucaargolo.extragenerators.mixin.KeyBindingAccessor
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.explosion.Explosion


object BlockCompendium: RegistryCompendium<Block>(Registry.BLOCK) {

    val BURNABLE_GENERATOR = register("burnable_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.burnableGenerator, { GeneratorFuel.fromBurnableGeneratorFuel(it.item) }))
    val GLUTTONY_GENERATOR = register("gluttony_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.gluttonyGenerator, { GeneratorFuel.fromGluttonyGeneratorFuel(it.item) })  )
    val ICY_GENERATOR = register("icy_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.icyGenerator, { GeneratorFuel.fromItemResource("icy", it) })  )
    val SLUDGY_GENERATOR = register("sludgy_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.sludgyGenerator, { GeneratorFuel.fromItemResource("sludgy", it) })  )
    val DRAGON_GENERATOR = register("dragon_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.dragonGenerator, { GeneratorFuel.fromItemResource("dragon", it) })  )
    val TELEPORT_GENERATOR = register("teleport_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.teleportGenerator, { GeneratorFuel.fromItemResource("teleport", it) })  )

    val ENCHANTED_GENERATOR = register("enchanted_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.enchantedGenerator, { GeneratorFuel.fromEnchantedGeneratorFuel(it) })  )
    val BREW_GENERATOR = register("brew_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.brewGenerator, { GeneratorFuel.fromBrewGeneratorFuel(it) })  )

    val WITHERED_GENERATOR = register("withered_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.witheredGenerator, { GeneratorFuel.fromItemResource("withered", it) }) {
        val world = it.world as? ServerWorld ?: return@ItemGeneratorBlock
        GeneratorAreaEffectCloudEntity.createAndSpawn(world, it, StatusEffects.WITHER)
    } )

//    val DEMISE_GENERATOR = register("demise_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.demiseGenerator, { GeneratorFuel.fromResource("demise", it) }) {
//        val world = it.world as? ServerWorld ?: return@ItemGeneratorBlock
//        GeneratorAreaEffectCloudEntity.createAndSpawn(world, it, StatusEffects.INSTANT_DAMAGE)
//    } )

    val BLAST_GENERATOR = register("blast_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.blastGenerator, { GeneratorFuel.fromItemResource("blast", it) }) {
        val world = it.world as? ServerWorld ?: return@ItemGeneratorBlock
        world.createExplosion(null, it.pos.x+0.5, it.pos.y+0.0, it.pos.z+0.5, 2f, Explosion.DestructionType.NONE)
    } )

    val SCALDING_GENERATOR = register("scalding_generator", FluidGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.scaldingGenerator) { GeneratorFuel.fromFluidResource("scalding", it) })

    fun itemGeneratorArray() = map.values.filterIsInstance<ItemGeneratorBlock>().toTypedArray()

    fun fluidGeneratorArray() = map.values.filterIsInstance<FluidGeneratorBlock>().toTypedArray()

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.forEach { (identifier, block) ->
            itemMap[identifier] = BlockItem(block, creativeGroupSettings())
        }
    }

    fun displayHiddenTooltip(tooltip: MutableList<Text>, runnable: Runnable) {
        val client = MinecraftClient.getInstance()
        val sneakKey = client.options.keySneak
        val sneak = InputUtil.isKeyPressed(client.window.handle, (sneakKey as KeyBindingAccessor).boundKey.code)
        if (!sneak) {
            tooltip.add(TranslatableText("tooltip.extragenerators.sneak_for_more", TranslatableText(sneakKey.boundKeyTranslationKey).formatted(Formatting.GRAY)).formatted(Formatting.BLUE))
        }else{
            runnable.run()
        }
    }

}