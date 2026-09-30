package me.twoj.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class Util {
    private Util() {}

    public static PlayerEntity nearestPlayer(double range) {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return null;
        PlayerEntity best = null;
        double bestD = range * range;
        for (AbstractClientPlayerEntity o : mc.world.getPlayers()) {
            if (o == mc.player || !o.isAlive()) continue;
            double d = mc.player.squaredDistanceTo(o);
            if (d < bestD) { bestD = d; best = o; }
        }
        return best;
    }

    public static int findHotbar(Item item) {
        var inv = MinecraftClient.getInstance().player.getInventory();
        for (int i = 0; i < 9; i++) if (inv.getStack(i).isOf(item)) return i;
        return -1;
    }

    /** Stawia blok z aktualnie trzymanego slotu na pozycji pos, klikajac w sasiedni blok. */
    public static boolean place(BlockPos pos) {
        var mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return false;
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;
        if (mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) > 6.0 * 6.0) return false;

        for (Direction d : Direction.values()) {
            BlockPos nb = pos.offset(d);
            if (mc.world.getBlockState(nb).isReplaceable()) continue;
            Direction face = d.getOpposite();
            Vec3d hit = Vec3d.ofCenter(nb).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
            BlockHitResult r = new BlockHitResult(hit, face, nb, false);
            ActionResult res = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, r);
            if (res.isAccepted()) {
                mc.player.swingHand(Hand.MAIN_HAND);
                return true;
            }
        }

        // Brak sasiedniego bloku (np. gracz w powietrzu, dach pulapki) - stawiamy "w powietrzu",
        // klikajac bezposrednio w docelowa pozycje.
        BlockHitResult air = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, air).isAccepted()) {
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        return false;
    }
}
