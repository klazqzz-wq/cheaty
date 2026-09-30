package me.twoj.client.modules;

import me.twoj.client.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Freecam: kamera odlacza sie od postaci i lata swobodnie (przez bloki), a Twoja postac
 * stoi w miejscu jak duch - nie ruszasz nia, nie bijesz, nie stawiasz i nie obracasz nia.
 *
 * Sterowanie kamera: WASD + spacja (w gore) + shift (w dol), mysz = rozgladanie.
 */
public class Freecam extends Module {
    private static final double SPEED = 1.0; // blokow na tick

    private static boolean active;
    private static OtherClientPlayerEntity camera;
    private static ClientPlayerEntity boundPlayer;
    private static Input oldInput;
    private static float savedYaw, savedPitch; // zamrozony obrot prawdziwej postaci
    private static float camYaw, camPitch;     // obrot kamery

    public Freecam() { super("Freecam", Category.PLAYER); }

    @Override
    public void onEnable() {
        var mc = MinecraftClient.getInstance();
        var p = mc.player;
        if (p == null || mc.world == null || active) return;

        boundPlayer = p;
        savedYaw = camYaw = p.getYaw();
        savedPitch = camPitch = p.getPitch();

        camera = new OtherClientPlayerEntity(mc.world, p.getGameProfile());
        camera.getInventory().clone(p.getInventory()); // zeby hotbar w HUD dalej wygladal normalnie
        camera.refreshPositionAndAngles(p.getX(), p.getY(), p.getZ(), camYaw, camPitch);

        // pusty input = postac nie reaguje na WASD/skok/shift
        oldInput = p.input;
        p.input = new Input();

        mc.setCameraEntity(camera);
        active = true;
    }

    @Override
    public void onDisable() {
        var mc = MinecraftClient.getInstance();
        if (active && boundPlayer != null && mc.player == boundPlayer) {
            if (oldInput != null) boundPlayer.input = oldInput;
            boundPlayer.setYaw(savedYaw);
            boundPlayer.setPitch(savedPitch);
        }
        if (mc.player != null) mc.setCameraEntity(mc.player);
        reset();
    }

    /** Czyszczenie stanu (tez przy rozlaczeniu z serwerem). */
    public static void reset() {
        active = false;
        camera = null;
        boundPlayer = null;
        oldInput = null;
    }

    @Override
    public void onTick() {
        var mc = MinecraftClient.getInstance();
        var p = mc.player;
        if (p == null || mc.world == null) return;
        // nie udalo sie wlaczyc albo zmienil sie swiat/postac (respawn, wymiar) - wylacz
        if (!active || boundPlayer != p) { toggle(); return; }

        double f = 0, s = 0, v = 0;
        if (mc.currentScreen == null) {
            var o = mc.options;
            if (o.forwardKey.isPressed()) f += 1;
            if (o.backKey.isPressed()) f -= 1;
            if (o.rightKey.isPressed()) s += 1;
            if (o.leftKey.isPressed()) s -= 1;
            if (o.jumpKey.isPressed()) v += 1;
            if (o.sneakKey.isPressed()) v -= 1;
        }

        double yaw = Math.toRadians(camYaw), pitch = Math.toRadians(camPitch);
        Vec3d fwd = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
        Vec3d right = new Vec3d(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3d move = fwd.multiply(f).add(right.multiply(s)).add(0, v, 0);
        if (move.lengthSquared() > 1.0E-6) move = move.normalize().multiply(SPEED);

        camera.prevX = camera.getX();
        camera.prevY = camera.getY();
        camera.prevZ = camera.getZ();
        camera.setPosition(camera.getX() + move.x, camera.getY() + move.y, camera.getZ() + move.z);
        camera.getInventory().selectedSlot = p.getInventory().selectedSlot;
    }

    /** START_CLIENT_TICK: zjada klikniecia, zanim gra zdazy zrobic z nich akcje postaci. */
    public static void onStartTick(MinecraftClient client) {
        if (!active) return;
        var o = client.options;
        o.attackKey.setPressed(false);
        o.useKey.setPressed(false);
        o.pickItemKey.setPressed(false);
        o.dropKey.setPressed(false);
        o.swapHandsKey.setPressed(false);
        while (o.attackKey.wasPressed()) { }
        while (o.useKey.wasPressed()) { }
        while (o.pickItemKey.wasPressed()) { }
        while (o.dropKey.wasPressed()) { }
        while (o.swapHandsKey.wasPressed()) { }
    }

    /**
     * Co klatke: ruch myszy trafia do prawdziwej postaci, wiec przenosimy go na kamere,
     * a postaci przywracamy zamrozony obrot.
     */
    public static void onFrame() {
        if (!active || boundPlayer == null || camera == null) return;
        var p = boundPlayer;

        camYaw += p.getYaw() - savedYaw;
        camPitch = MathHelper.clamp(camPitch + (p.getPitch() - savedPitch), -90f, 90f);

        p.setYaw(savedYaw);
        p.setPitch(savedPitch);
        p.prevYaw = savedYaw;
        p.prevPitch = savedPitch;

        camera.setYaw(camYaw);
        camera.setPitch(camPitch);
        camera.prevYaw = camYaw;
        camera.prevPitch = camPitch;
    }
}
