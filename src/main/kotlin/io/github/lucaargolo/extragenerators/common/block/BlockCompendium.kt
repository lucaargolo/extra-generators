package io.github.lucaargolo.extragenerators.common.block

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.ExtraGenerators.Companion.creativeGroupSettings
import io.github.lucaargolo.extragenerators.common.entity.GeneratorAreaEffectCloudEntity
import io.github.lucaargolo.extragenerators.mixin.KeyBindingAccessor
import io.github.lucaargolo.extragenerators.utils.FluidGeneratorFuel
import io.github.lucaargolo.extragenerators.utils.GeneratorFuel
import io.github.lucaargolo.extragenerators.utils.ModConfig
import io.github.lucaargolo.extragenerators.utils.RegistryCompendium
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.util.Util
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import java.awt.Color


object BlockCompendium: RegistryCompendium<Block>(Registry.BLOCK) {

    //Tier 1 Generators
    val THERMOELECTRIC_GENERATOR = register("thermoelectric_generator", ThermoelectricGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.thermoelectricGenerator))
    val BURNABLE_GENERATOR = register("burnable_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.burnableGenerator, { GeneratorFuel.fromBurnableGeneratorFuel(it.item) }))
    val ICY_GENERATOR = register("icy_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.icyGenerator, { GeneratorFuel.fromItemResource("icy", it) })  )
    val COLORFUL_GENERATOR = register("colorful_generator", ColorfulGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.colorfulGenerator))

    //Tier 2 Generators
    val SLUDGY_GENERATOR = register("sludgy_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.sludgyGenerator, { GeneratorFuel.fromItemResource("sludgy", it) })  )
    val TELEPORT_GENERATOR = register("teleport_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.teleportGenerator, { GeneratorFuel.fromItemResource("teleport", it) })  )
    val SCALDING_GENERATOR = register("scalding_generator", FluidGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.scaldingGenerator) { FluidGeneratorFuel.fromFluidResource("scalding", it) })
    val STEAM_GENERATOR = register("steam_generator", FluidItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.steamGenerator, FluidKeys.WATER) { FluidGeneratorFuel.fromSteamGeneratorFuel(it) })

    //Tier 3 Generators
    val GLUTTONY_GENERATOR = register("gluttony_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.gluttonyGenerator, { GeneratorFuel.fromGluttonyGeneratorFuel(it.item) })  )
    val BREW_GENERATOR = register("brew_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.brewGenerator, { GeneratorFuel.fromBrewGeneratorFuel(it) })  )
    val REDSTONE_GENERATOR = register("redstone_generator", FluidItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.redstoneGenerator, FluidKeys.LAVA) { FluidGeneratorFuel.fromRedstoneGeneratorFuel(it) })
    val BLAST_GENERATOR = register("blast_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.blastGenerator, { GeneratorFuel.fromItemResource("blast", it) }) {
        val world = it.world as? ServerWorld ?: return@ItemGeneratorBlock
        world.createExplosion(null, it.pos.x+0.5, it.pos.y+0.0, it.pos.z+0.5, 2f, Explosion.DestructionType.NONE)
    } )

    //Tier 4 Generators
    val ENCHANTED_GENERATOR = register("enchanted_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.enchantedGenerator, { GeneratorFuel.fromEnchantedGeneratorFuel(it) })  )
    val DEMISE_GENERATOR = register("demise_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.demiseGenerator, { GeneratorFuel.fromItemResource("demise", it) }) {
        val world = it.world as? ServerWorld ?: return@ItemGeneratorBlock
        GeneratorAreaEffectCloudEntity.createAndSpawn(world, it, StatusEffects.WEAKNESS)
    } )

    //Tier 5 Generators
    val DRAGON_GENERATOR = register("dragon_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.dragonGenerator, { GeneratorFuel.fromItemResource("dragon", it) })  )
    val WITHERED_GENERATOR = register("withered_generator", ItemGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.witheredGenerator, { GeneratorFuel.fromItemResource("withered", it) }) {
        val world = it.world as? ServerWorld ?: return@ItemGeneratorBlock
        GeneratorAreaEffectCloudEntity.createAndSpawn(world, it, StatusEffects.WITHER)
    } )

    //Tier ∞ Generators
    val HEAVENLY_GENERATOR = register("heavenly_generator", InfiniteGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.heavenlyGenerator) )
    val INFERNAL_GENERATOR = register("infernal_generator", InfiniteGeneratorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).nonOpaque(), ExtraGenerators.CONFIG.infernalGenerator) )

    fun generatorIdentifierMap() = map.filter { it.value is AbstractGeneratorBlock }
    fun generatorIdentifierArray() = map.filter { it.value is AbstractGeneratorBlock }.map { it.key }.toTypedArray()

    fun itemGeneratorArray() = map.values.filterIsInstance<ItemGeneratorBlock>().toTypedArray()

    fun fluidGeneratorArray() = map.values.filterIsInstance<FluidGeneratorBlock>().toTypedArray()

    fun fluidItemGeneratorArray() = map.values.filterIsInstance<FluidItemGeneratorBlock>().toTypedArray()

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.forEach { (identifier, block) ->
            itemMap[identifier] = when(block) {
                THERMOELECTRIC_GENERATOR, BURNABLE_GENERATOR, ICY_GENERATOR, COLORFUL_GENERATOR -> object: BlockItem(block, creativeGroupSettings()) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        world?.let { displayHiddenTooltip(tooltip) { appendGeneratorTooltip(stack, tooltip, (block as AbstractGeneratorBlock).generatorConfig, "1")} }
                    }
                }
                SLUDGY_GENERATOR, TELEPORT_GENERATOR, SCALDING_GENERATOR, STEAM_GENERATOR -> object: BlockItem(block, creativeGroupSettings().rarity(Rarity.COMMON)) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        world?.let { displayHiddenTooltip(tooltip) { appendGeneratorTooltip(stack, tooltip, (block as AbstractGeneratorBlock).generatorConfig, "2")} }
                    }
                }
                GLUTTONY_GENERATOR, BREW_GENERATOR, REDSTONE_GENERATOR, BLAST_GENERATOR -> object: BlockItem(block, creativeGroupSettings().rarity(Rarity.UNCOMMON)) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        world?.let { displayHiddenTooltip(tooltip) { appendGeneratorTooltip(stack, tooltip, (block as AbstractGeneratorBlock).generatorConfig, "3")} }
                    }
                }
                ENCHANTED_GENERATOR, DEMISE_GENERATOR -> object: BlockItem(block, creativeGroupSettings().rarity(Rarity.RARE)) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        world?.let { displayHiddenTooltip(tooltip) { appendGeneratorTooltip(stack, tooltip, (block as AbstractGeneratorBlock).generatorConfig, "4")} }
                    }
                }
                DRAGON_GENERATOR, WITHERED_GENERATOR -> object: BlockItem(block, creativeGroupSettings().rarity(Rarity.EPIC)) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        world?.let { displayHiddenTooltip(tooltip) { appendGeneratorTooltip(stack, tooltip, (block as AbstractGeneratorBlock).generatorConfig, "5")} }
                    }
                }
                HEAVENLY_GENERATOR, INFERNAL_GENERATOR -> object: BlockItem(block, creativeGroupSettings()) {
                    override fun getName(stack: ItemStack): MutableText = getRainbowText(super.getName().string)
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        world?.let { displayHiddenTooltip(tooltip) { appendGeneratorTooltip(stack, tooltip, (block as AbstractGeneratorBlock).generatorConfig, "∞")} }
                    }
                }
                else -> BlockItem(block, creativeGroupSettings())
            }
        }
    }

    fun appendGeneratorTooltip(stack: ItemStack, tooltip: MutableList<Text>, generatorConfig: ModConfig.Generator, tier: String) {
        tooltip.add(TranslatableText("tooltip.extragenerators.tier", tier).formatted(Formatting.DARK_GRAY))
        tooltip.add(TranslatableText("tooltip.extragenerators.total_storage", LiteralText(generatorConfig.storage.toString()).formatted(Formatting.GRAY)).formatted(Formatting.BLUE))
        tooltip.add(TranslatableText("tooltip.extragenerators.output", LiteralText(generatorConfig.output.toString()).formatted(Formatting.GRAY)).formatted(Formatting.BLUE))
        if(stack.hasTag()) {
            val blockEntityTag = stack.orCreateTag.getCompound("BlockEntityTag")
            if(blockEntityTag.contains("storedPower")) {
                tooltip.add(TranslatableText("tooltip.extragenerators.stored_energy", LiteralText(blockEntityTag.getDouble("storedPower").toString()).formatted(Formatting.GRAY)).formatted(Formatting.BLUE))
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

    fun getRainbowText(string: String): MutableText {
        var finalText: MutableText = LiteralText("")
        string.forEachIndexed { x, c ->
            val color = Color.HSBtoRGB((Util.getMeasuringTimeMs() - x*100) % 2000 / 2000f, 0.8f, 0.95f)
            val textColor = TextColor.fromRgb(color)
            val charText = LiteralText("$c")
            val textStyle = charText.style.withColor(textColor)
            charText.style = textStyle
            finalText = finalText.append(charText)
        }
        return finalText
    }

}