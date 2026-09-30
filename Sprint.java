package me.twoj.client.modules;

import me.twoj.client.Module;
import net.minecraft.client.MinecraftClient;

public class Sprint extends Module {
    public Sprint() { super("Sprint", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var p = MinecraftClient.getInstance().player;
        if (p != null && p.input.movementForward > 0 && !p.horizontalCollision)
            p.setSprinting(true);
    }
}
