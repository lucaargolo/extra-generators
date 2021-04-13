package io.github.lucaargolo.extragenerators.client.screen

import io.github.lucaargolo.extragenerators.common.blockentity.AbstractGeneratorBlockEntity
import io.github.lucaargolo.extragenerators.common.containers.AbstractGeneratorScreenHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

abstract class AbstractGeneratorScreen<S: AbstractGeneratorScreenHandler<S, B>, B: AbstractGeneratorBlockEntity<B>>(handler: S, inventory: PlayerInventory, title: Text): HandledScreen<S>(handler, inventory, title)