package me.twoj.client.modules;

import me.twoj.client.Module;
import me.twoj.client.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

/**
 * Stawia pajeczyne w miejscu stop najblizszego gracza. Nie sprawdza widocznosci,
 * wiec dziala tez przez sciany (liczy sie tylko odleglosc <= range).
 */
public class AutoWeb extends Module {
    private int cooldown;

    public AutoWeb() { super("AutoWeb", Category.COMBAT); }

    @Override
    public boolean hasRange() { return true; }

    @Override
    public void onTick() {
        if (cooldown-- > 0) return;
        var mc = MinecraftClient.getInstance();
        var target = Util.nearestPlayer(range);
        if (target == null) return;

        int slot = Util.findHotbar(Items.COBWEB);
        if (slot == -1) return;

        BlockPos pos = target.getBlockPos();
        if (!mc.world.getBlockState(pos).isReplaceable()) return;

        var inv = mc.player.getInventory();
        int prev = inv.selectedSlot;
        inv.selectedSlot = slot;
        if (Util.place(pos)) cooldown = 5;
        inv.selectedSlot = prev;
    }
}
