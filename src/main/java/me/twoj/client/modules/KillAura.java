package me.twoj.client.modules;

import me.twoj.client.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/** Atakuje najblizszego gracza/moba w zasiegu. Nie sprawdza widocznosci - bije tez przez bloki. */
public class KillAura extends Module {
    public KillAura() { super("KillAura", Category.COMBAT); }

    @Override
    public boolean hasRange() { return true; }

    @Override
    public void onTick() {
        var mc = MinecraftClient.getInstance();
        var p = mc.player;
        if (p == null || mc.world == null || mc.interactionManager == null) return;
        if (p.getAttackCooldownProgress(0.5f) < 1f) return;

        LivingEntity best = null;
        double bestD = range * range;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || e == p || !le.isAlive() || e instanceof ArmorStandEntity) continue;
            if (!(e instanceof PlayerEntity) && !(e instanceof MobEntity)) continue;
            double d = p.squaredDistanceTo(e);
            if (d < bestD) { bestD = d; best = le; }
        }
        if (best == null) return;

        mc.interactionManager.attackEntity(p, best);
        p.swingHand(Hand.MAIN_HAND);
    }
}
