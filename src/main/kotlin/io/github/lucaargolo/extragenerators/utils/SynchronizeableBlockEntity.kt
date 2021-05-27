package io.github.lucaargolo.extragenerators.utils

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos

abstract class SynchronizeableBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState): BlockEntity(type, pos, state), BlockEntityClientSerializable {

    fun markDirtyAndSync() = markDirty().also{ if(world?.isClient == false) sync() }

}