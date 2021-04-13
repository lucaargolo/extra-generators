package io.github.lucaargolo.extragenerators.utils

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType

abstract class SynchronizeableBlockEntity(type: BlockEntityType<*>): BlockEntity(type), BlockEntityClientSerializable {

    fun markDirtyAndSync() = markDirty().also{ if(world?.isClient == false) sync() }

}