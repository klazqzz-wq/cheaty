package me.twoj.client.modules;

import me.twoj.client.Module;
import me.twoj.client.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/** Obudowuje najblizszego gracza wybranym blokiem (domyslnie obsydian): 4 boki na wysokosci nog i glowy + dach. */
public class Trap extends Module {
    private static final int PER_TICK = 2;
    private int cooldown;

    public Trap() { super("Trap", Category.COMBAT); }

    @Override
    public boolean hasRange() { return true; }

    @Override
    public boolean hasBlockPick() { return true; }

    @Override
    public void onTick() {
        if (cooldown-- > 0) return;
        var mc = MinecraftClient.getInstance();
        var target = Util.nearestPlayer(range);
        if (target == null) return;

        int slot = Util.findHotbar(block.asItem());
        if (slot == -1) return;

        BlockPos b = target.getBlockPos();
        List<BlockPos> positions = new ArrayList<>();
        for (int y = 0; y <= 1; y++)
            for (Direction d : Direction.Type.HORIZONTAL)
                positions.add(b.up(y).offset(d));
        positions.add(b.up(2));

        var inv = mc.player.getInventory();
        int prev = inv.selectedSlot;
        inv.selectedSlot = slot;
        int placed = 0;
        for (BlockPos pos : positions) {
            if (placed >= PER_TICK) break;
            if (Util.place(pos)) placed++;
        }
        inv.selectedSlot = prev;
        cooldown = 2;
    }
}
