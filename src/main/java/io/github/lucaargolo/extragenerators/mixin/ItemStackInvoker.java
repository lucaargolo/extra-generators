package io.github.lucaargolo.extragenerators.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStack.class)
public interface ItemStackInvoker {

    @Invoker("<init>")
    static ItemStack unsafeFromNbt(NbtCompound nbt) {
        throw new AssertionError();
    }

}
