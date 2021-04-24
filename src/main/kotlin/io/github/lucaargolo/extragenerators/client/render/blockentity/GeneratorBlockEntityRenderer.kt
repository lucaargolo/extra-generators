package io.github.lucaargolo.extragenerators.client.render.blockentity

import io.github.lucaargolo.extragenerators.common.blockentity.AbstractGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.blockentity.InfiniteGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.mixin.BakedModelManagerAccessor
import io.github.lucaargolo.extragenerators.utils.ModIdentifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.state.property.Properties
import net.minecraft.util.Util
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.util.*

class GeneratorBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher?) : BlockEntityRenderer<AbstractGeneratorBlockEntity<*>>(dispatcher) {

    override fun render(entity: AbstractGeneratorBlockEntity<*>, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()
        val model = (client.bakedModelManager as? BakedModelManagerAccessor)?.models?.get(ModIdentifier("block/cog_wheels")) ?: return
        val facing = entity.cachedState[Properties.HORIZONTAL_FACING]
        val random = Random()
        matrices.push()
        matrices.translate(0.5, 0.5, 0.5)
        when(facing) {
            Direction.SOUTH -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270f))
            Direction.NORTH -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            Direction.WEST -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f))
            else -> {}
        }
        matrices.translate(-0.5, -0.5, -0.5)
        val cogWheelRotation = MathHelper.lerp(tickDelta, entity.lastCogWheelRotationDegree, entity.cogWheelRotationDegree)
        matrices.translate(8.5/16.0, 6.0/16.0, 6.0/16.0)
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(cogWheelRotation))
        matrices.translate(-8.5/16.0, -6.0/16.0, -6.0/16.0)
        entity.lastCogWheelRotationDegree = cogWheelRotation
        val quads = mutableListOf<BakedQuad>()
        Direction.values().forEach { quads.addAll(model.getQuads(null, it, random)) }.also { quads.addAll(model.getQuads(null, null, random)) }
        val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))
        quads.forEach {
            if(entity is InfiniteGeneratorBlockEntity && entity.isRunning()) {
                val c = Color.getHSBColor(Util.getMeasuringTimeMs() % 2000 / 2000f, 0.8f, 0.95f)
                vertexConsumer.quad(matrices.peek(), it, c.red/255f, c.green/255f, c.blue/255f, light, overlay)
            }else{
                vertexConsumer.quad(matrices.peek(), it, 1f, 1f, 1f, light, overlay)
            }
        }
        matrices.pop()
    }

}