package io.github.lucaargolo.extragenerators.common.block

import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.ExtraGenerators.Companion.creativeGroupSettings
import io.github.lucaargolo.extragenerators.common.resource.ResourceCompendium
import io.github.lucaargolo.extragenerators.mixin.KeyBindingAccessor
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object BlockCompendium: RegistryCompendium<Block>(Registry.BLOCK) {

    val FURNACE_GENERATOR = register("furnace_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.furnaceGenerator) { GeneratorFuel.fromFurnaceGeneratorFuel(it.item) })
    val CULINARY_GENERATOR = register("culinary_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.culinaryGenerator) { GeneratorFuel.fromCulinaryGeneratorFuel(it.item) } )
    val FROSTY_GENERATOR = register("frosty_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.frostyGenerator) { GeneratorFuel.fromResource("frosty", it) } )

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.forEach { (identifier, block) ->
            itemMap[identifier] = when(block) {
                else -> BlockItem(block, creativeGroupSettings())
            }
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