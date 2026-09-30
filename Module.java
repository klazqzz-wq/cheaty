package me.twoj.client;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public abstract class Module {
    public final String name;
    public final Category category;
    private boolean enabled;

    /** Klawisz z GLFW (-1 = brak bindu). */
    public int key = -1;
    /** Czy w GUI jest rozwiniety panel bind/wlacz. */
    public boolean guiOpen;
    /** Do wykrywania nacisniecia bindu. */
    public boolean keyWasDown;
    /** Zasieg w blokach (standardowo 3). Uzywany tylko przez moduly z hasRange() == true. */
    public double range = 3.0;
    /** Wybrany blok (dla modulow z hasBlockPick()). */
    public Block block = Blocks.OBSIDIAN;

    public enum Category { COMBAT, MOVEMENT, RENDER, PLAYER }

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public boolean isEnabled() { return enabled; }

    /** Czy modul ma ustawienie zasiegu w GUI. */
    public boolean hasRange() { return false; }

    /** Czy modul ma w GUI wybor bloku. */
    public boolean hasBlockPick() { return false; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
