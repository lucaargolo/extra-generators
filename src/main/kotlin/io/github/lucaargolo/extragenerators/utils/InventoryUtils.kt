@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.extragenerators.utils

import com.google.gson.JsonObject
import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.extragenerators.ExtraGenerators
import io.github.lucaargolo.extragenerators.mixin.ItemStackInvoker
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.client.render.*
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.inventory.SidedInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import org.apache.commons.lang3.math.Fraction

typealias ItemFilter = (slot: Int, stack: ItemStack) -> Boolean

class SimpleSidedInventory(slots: Int, private val insertFilter: ItemFilter, private val extractFilter: ItemFilter, private val sides: (Direction?) -> IntArray): SimpleInventory(slots), SidedInventory {

    override fun getAvailableSlots(side: Direction?) = sides.invoke(side)

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        return sides.invoke(dir).contains(slot) && insertFilter.invoke(slot, stack)
    }

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
        return sides.invoke(dir).contains(slot) && extractFilter.invoke(slot, stack)
    }

    override fun readNbtList(nbtList: NbtList) {
        for (i in nbtList.indices) {
            setStack(i, ItemStack.fromNbt(nbtList.getCompound(i)))
        }
    }

    override fun toNbtList(): NbtList {
        return NbtList().also {
            for (i in 0 until size()) {
                it.add(getStack(i).writeNbt(NbtCompound()))
            }
        }
    }

    class SimpleSlot(private val simpleInventory: SimpleSidedInventory, index: Int, x: Int, y: Int): Slot(simpleInventory, index, x, y) {

        override fun canInsert(stack: ItemStack): Boolean {
            return simpleInventory.canInsert(index, stack, null)
        }

    }

}

object InventoryUtils {

    fun ResourceAmount<FluidVariant>.getTankFluidTooltip(capacity: Long): List<Text> {
        val list = mutableListOf<Text>()
        if(resource.fluid == Fluids.EMPTY) {
            list.add(Text.translatable("tooltip.extragenerators.empty"))
        } else {
            list.add(FluidVariantAttributes.getName(resource).copyContentOnly().also { it.style = it.style.withColor(FluidVariantRendering.getColor(resource)) })
        }
        val storedMb = if(amount in 1..80) {
            "< 1"
        }else{
            "${amount/81}"
        }
        list.add(Text.literal("$storedMb / ${capacity/81} mB").formatted(Formatting.GRAY))
        return list
    }

    fun interactPlayerHand(tank: Storage<FluidVariant>, player: PlayerEntity, hand: Hand, canInsert: Boolean = true, canExtract: Boolean = true): ActionResult {
        val interacted = let {
            val backupStack = if(player.isCreative) { player.getStackInHand(hand).copy() } else null
            val handStorage = ContainerItemContext.ofPlayerHand(player, hand).find(FluidStorage.ITEM) ?: return@let false
            if (canInsert && StorageUtil.move(handStorage, tank, { true }, Long.MAX_VALUE, null) > 0) {
                backupStack?.let { player.setStackInHand(hand, backupStack) }
                return@let true
            }
            if (canExtract && StorageUtil.move(tank, handStorage, { true }, Long.MAX_VALUE, null) > 0) {
                backupStack?.let { player.setStackInHand(hand, backupStack) }
                return@let true
            }
            return@let false
        }
        return if (interacted) {
            ActionResult.success(player.world.isClient)
        } else {
            ActionResult.PASS
        }
    }

    fun interactPlayerCursor(tank: Storage<FluidVariant>, player: PlayerEntity, canInsert: Boolean = true, canExtract: Boolean = true) {
        val backupStack = if(player.isCreative) { player.currentScreenHandler.cursorStack.copy() } else null
        val cursorStorage = ContainerItemContext.ofPlayerCursor(player, player.currentScreenHandler).find(FluidStorage.ITEM) ?: return
        if (canInsert && StorageUtil.move(cursorStorage, tank, { true }, Long.MAX_VALUE, null) > 0) {
            backupStack?.let { player.currentScreenHandler.cursorStack = backupStack }
            return
        }
        if (canExtract && StorageUtil.move(tank, cursorStorage, { true }, Long.MAX_VALUE, null) > 0) {
            backupStack?.let { player.currentScreenHandler.cursorStack = backupStack }
            return
        }
    }

    fun getExtractableFluid(itemStack: ItemStack): ResourceAmount<FluidVariant>? {
        val containedFluidStorage = ContainerItemContext.withInitial(itemStack).find(FluidStorage.ITEM)
        return StorageUtil.findExtractableContent(containedFluidStorage, Transaction.getCurrentUnsafe())
    }

    fun canInsertFluid(itemStack: ItemStack): Boolean {
        val containedFluidStorage = ContainerItemContext.withInitial(itemStack).find(FluidStorage.ITEM)
        return containedFluidStorage?.supportsInsertion() ?: false
    }

    fun fluidResourceFromNbt(nbt: NbtCompound): ResourceAmount<FluidVariant> {
        val variant = FluidVariant.fromNbt(nbt.getCompound("variant"))
        val amount = nbt.getLong("amount")
        return ResourceAmount(variant, amount)
    }

    fun fluidResourceFromMcBuffer(buf: PacketByteBuf): ResourceAmount<FluidVariant> {
        val variant = FluidVariant.fromPacket(buf)
        val amount = buf.readLong()
        return ResourceAmount(variant, amount)
    }

    fun fluidResourceFromJson(json: JsonObject): ResourceAmount<FluidVariant> {
        val fluidId = Identifier(json.get("fluid").asString)
        val fluid = Registry.FLUID.get(fluidId)
        val variant = FluidVariant.of(fluid)
        val amount = json.get("amount").asLong
        return ResourceAmount(variant, amount)
    }

}

fun SimpleSidedInventory.fromNbt(nbt: NbtElement?) {
    when(nbt?.nbtType) {
        NbtList.TYPE -> readNbtList(nbt as NbtList)
        NbtCompound.TYPE -> {
            //Found potential LBA inventory
            ExtraGenerators.LOGGER.info("Found old LBA inventory. Converting it")
            val nbtCompound = nbt as NbtCompound
            nbtCompound.getList("slots", 10).forEachIndexed { slot, listElement ->
                try {
                    val elementCompound = listElement as NbtCompound
                    val elementStack = if(elementCompound.contains("id")) try {
                        ItemStackInvoker.unsafeFromNbt(elementCompound)
                    } catch (e: RuntimeException) {
                        ExtraGenerators.LOGGER.debug("Failed to load old LBA inventorty item: {}", nbt, e)
                        ItemStack.EMPTY
                    } else ItemStack.EMPTY
                    setStack(slot, elementStack)
                    ExtraGenerators.LOGGER.info("Old LBA item loaded with id ${Registry.ITEM.getId(elementStack.item)} at slot $slot")

                } catch (e: ClassCastException) {
                    ExtraGenerators.LOGGER.error("Failed to convert old LBA inventory", e)
                }
            }
            ExtraGenerators.LOGGER.info("LBA inventory conversion finished.")
        }
    }
}

fun SimpleSidedInventory.toNbt(): NbtList {
    return toNbtList()
}

fun SingleVariantStorage<FluidVariant>.fromNbt(nbt: NbtCompound) {
    if(nbt.contains("tanks")) {
        //Found potential LBA fluid inventory
        ExtraGenerators.LOGGER.info("Found old LBA fluid inventory. Converting it")
        nbt.getList("tanks", 10).forEach { listElement ->
            try {
                val oldTank = listElement as NbtCompound
                val oldAmount = oldTank.getCompound("AmountF")
                val fluidId = oldTank.getString("ObjName")
                val fluid = Registry.FLUID.get(Identifier(fluidId))
                if(fluid != Fluids.EMPTY) {
                    val fraction = Fraction.getFraction(oldAmount.getLong("w").toInt(), oldAmount.getLong("n").toInt(), oldAmount.getLong("d").toInt())
                    val newAmount = MathHelper.floor(fraction.toFloat()*81000f)
                    if(newAmount <= capacity) {
                        ExtraGenerators.LOGGER.info("Successfully converted old LBA fluid inventory with $newAmount droplets of $fluidId")
                        variant = FluidVariant.of(fluid)
                        amount = newAmount.toLong()
                        return
                    }else{
                        ExtraGenerators.LOGGER.info("Old LBA fluid inventory had $newAmount droplets while maximum accepted was $capacity")
                    }
                }else{
                    ExtraGenerators.LOGGER.info("Old LBA fluid inventory was empty.")
                }
            } catch (e: Exception) {
                ExtraGenerators.LOGGER.error("Failed to convert old LBA fluid inventory", e)
            }
        }
    }
    variant = FluidVariant.fromNbt(nbt.getCompound("variant"))
    amount = nbt.getLong("amount")
}

fun SingleVariantStorage<FluidVariant>.toNbt(nbt: NbtCompound = NbtCompound()): NbtCompound {
    nbt.put("variant", resource.toNbt())
    nbt.putLong("amount", amount)
    return nbt
}

fun ResourceAmount<FluidVariant>.toNbt(nbt: NbtCompound = NbtCompound()): NbtCompound {
    nbt.put("variant", resource.toNbt())
    nbt.putLong("amount", amount)
    return nbt
}

fun ResourceAmount<FluidVariant>.toMcBuffer(buf: PacketByteBuf = PacketByteBufs.create()): PacketByteBuf {
    resource.toPacket(buf)
    buf.writeLong(amount)
    return buf
}

fun FluidVariant.renderGuiRect(matrices: MatrixStack, startX: Float, startY: Float, height: Float) {
    var vh = height
    var i = 0
    while (vh > 16) {
        vh -= 16
        innerRenderGuiRect(matrices, startX, startY-(i*16f)-16, 16, 1f)
        i++
    }
    innerRenderGuiRect(matrices, startX, startY-(i*16f)-vh, 16, vh/16f)
}

/*
    Original code from Modern Industrialization
    Available at: https://github.com/AztechMC/Modern-Industrialization/blob/bb0fa25698f0692ad9c1a3a104544be886856b7a/src/main/java/aztech/modern_industrialization/util/RenderHelper.java#L152
    Thanks Technici4n :3
*/
private fun FluidVariant.innerRenderGuiRect(ms: MatrixStack, i: Float, j: Float, scale: Int, fractionUp: Float) {
    RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
    val sprite = FluidVariantRendering.getSprite(this)
    val color = FluidVariantRendering.getColor(this)
    if (sprite == null) return
    val a = (color shr 24 and 255) / 256f
    val r = (color shr 16 and 255) / 256f
    val g = (color shr 8 and 255) / 256f
    val b = (color and 255) / 256f
    RenderSystem.disableDepthTest()
    RenderSystem.setShader { GameRenderer.getPositionColorTexShader() }
    val bufferBuilder = Tessellator.getInstance().buffer
    bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
    val x1 = i + scale
    val y1 = j + scale * fractionUp
    val z = 0.5f
    val u0 = sprite.minU
    val v1 = sprite.maxV
    val v0 = v1 + (sprite.minV - v1) * fractionUp
    val u1 = sprite.maxU
    val model = ms.peek().positionMatrix
    bufferBuilder.vertex(model, i, y1, z).color(r, g, b, a).texture(u0, v1).next()
    bufferBuilder.vertex(model, x1, y1, z).color(r, g, b, a).texture(u1, v1).next()
    bufferBuilder.vertex(model, x1, j, z).color(r, g, b, a).texture(u1, v0).next()
    bufferBuilder.vertex(model, i, j, z).color(r, g, b, a).texture(u0, v0).next()
    BufferRenderer.drawWithShader(bufferBuilder.end())
    RenderSystem.enableDepthTest()
}