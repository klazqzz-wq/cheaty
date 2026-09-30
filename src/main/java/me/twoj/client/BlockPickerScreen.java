package me.twoj.client;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Panel ze wszystkimi blokami Minecrafta: wyszukiwarka, przewijanie kolkiem, klik = wybor. */
public class BlockPickerScreen extends Screen {
    private static final int CELL = 20;

    private final Screen parent;
    private final Module module;
    private final List<Block> all = new ArrayList<>();
    private List<Block> shown = new ArrayList<>();
    private TextFieldWidget search;
    private int scroll;
    private int cols, visibleRows, gridX, gridY;

    public BlockPickerScreen(Screen parent, Module module) {
        super(Text.literal("Wybierz blok"));
        this.parent = parent;
        this.module = module;
        for (Block b : Registries.BLOCK) {
            if (b == Blocks.AIR || b.asItem() == Items.AIR) continue;
            all.add(b);
        }
        shown = all;
    }

    @Override
    protected void init() {
        cols = Math.max(1, (width - 40) / CELL);
        gridX = (width - cols * CELL) / 2;
        gridY = 46;
        visibleRows = Math.max(1, (height - gridY - 10) / CELL);

        search = new TextFieldWidget(textRenderer, width / 2 - 100, 12, 200, 16, Text.literal("Szukaj"));
        search.setPlaceholder(Text.literal("Szukaj bloku..."));
        search.setChangedListener(s -> { filter(s); scroll = 0; });
        addDrawableChild(search);
        setInitialFocus(search);
    }

    private void filter(String s) {
        String q = s.toLowerCase(Locale.ROOT).trim();
        if (q.isEmpty()) { shown = all; return; }
        List<Block> out = new ArrayList<>();
        for (Block b : all) {
            if (b.getName().getString().toLowerCase(Locale.ROOT).contains(q)
                    || Registries.BLOCK.getId(b).getPath().contains(q)) out.add(b);
        }
        shown = out;
    }

    private int maxScroll() {
        int totalRows = (shown.size() + cols - 1) / cols;
        return Math.max(0, totalRows - visibleRows);
    }

    private Block blockAt(double mx, double my) {
        if (mx < gridX || my < gridY) return null;
        int col = (int) ((mx - gridX) / CELL);
        int row = (int) ((my - gridY) / CELL);
        if (col >= cols || row >= visibleRows) return null;
        int idx = (scroll + row) * cols + col;
        return idx < shown.size() ? shown.get(idx) : null;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xDD000000);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta); // tlo + pole wyszukiwania

        ctx.drawTextWithShadow(textRenderer, "Wybrany: " + module.block.getName().getString(),
                gridX, 32, 0xFFFFFFFF);

        Block hover = null;
        int start = scroll * cols;
        for (int i = 0; i < visibleRows * cols; i++) {
            int idx = start + i;
            if (idx >= shown.size()) break;
            int cx = gridX + (i % cols) * CELL;
            int cy = gridY + (i / cols) * CELL;
            Block b = shown.get(idx);
            boolean over = mx >= cx && mx < cx + CELL && my >= cy && my < cy + CELL;
            int bg = b == module.block ? 0xFF7B2FF7 : (over ? 0xFF3A3A3A : 0xFF1E1E1E);
            ctx.fill(cx, cy, cx + CELL - 1, cy + CELL - 1, bg);
            ctx.drawItem(new ItemStack(b), cx + 1, cy + 1);
            if (over) hover = b;
        }
        if (hover != null) ctx.drawItemTooltip(textRenderer, new ItemStack(hover), mx, my);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            Block b = blockAt(mx, my);
            if (b != null) {
                module.block = b;
                close();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(vertical)));
        return true;
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public boolean shouldPause() { return false; }
}
