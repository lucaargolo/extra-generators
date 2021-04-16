package io.github.lucaargolo.extragenerators.mixin;

import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sprite.class)
public interface SpriteAccessor {

    @Accessor("x")
    int getX();

    @Accessor("y")
    int getY();


}
