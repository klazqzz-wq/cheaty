package me.twoj.client.modules;

import me.twoj.client.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {
    public Fullbright() { super("Fullbright", Category.RENDER); }

    @Override
    public void onTick() {
        var p = MinecraftClient.getInstance().player;
        if (p != null)
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
                    StatusEffectInstance.INFINITE, 0, false, false, false));
    }

    @Override
    public void onDisable() {
        var p = MinecraftClient.getInstance().player;
        if (p != null) p.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
