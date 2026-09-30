package me.twoj.client.modules;

import me.twoj.client.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Wstrzymuje pakiety ruchu i wysyla je paczkami co DELAY_TICKS ticków,
 * przez co inni gracze widza postac skaczaca miedzy pozycjami.
 */
public class FakeLag extends Module {
    private static final int DELAY_TICKS = 8;
    private static final List<Packet<?>> queue = new ArrayList<>();
    private static boolean flushing;
    private static boolean active;
    private int ticks;

    public FakeLag() { super("FakeLag", Category.MOVEMENT); }

    public static boolean shouldQueue(Packet<?> p) {
        return active && !flushing && p instanceof PlayerMoveC2SPacket;
    }

    public static void queue(Packet<?> p) { queue.add(p); }

    public static void reset() { queue.clear(); }

    private static void flush() {
        var handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null) { queue.clear(); return; }
        flushing = true;
        try {
            for (Packet<?> p : queue) handler.sendPacket(p);
        } finally {
            queue.clear();
            flushing = false;
        }
    }

    @Override
    public void onEnable() { ticks = 0; active = true; }

    @Override
    public void onDisable() { active = false; flush(); }

    @Override
    public void onTick() {
        if (++ticks >= DELAY_TICKS) {
            flush();
            ticks = 0;
        }
    }
}
