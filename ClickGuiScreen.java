package me.twoj.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final int W = 110, H = 16;
    private static final int ACCENT = 0xFF7B2FF7;

    private static class Panel {
        final Module.Category cat;
        int x, y;
        boolean open = true;
        Panel(Module.Category cat, int x, int y) { this.cat = cat; this.x = x; this.y = y; }
    }

    // static, zeby pozycje paneli zostawaly po zamknieciu GUI
    private static final List<Panel> panels = new ArrayList<>();
    private Panel dragging;
    private int dragOffX, dragOffY;
    private Module listening; // modul, ktoremu czekamy na klawisz

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
        if (panels.isEmpty()) {
            int x = 20;
            for (Module.Category c : Module.Category.values()) {
                panels.add(new Panel(c, x, 20));
                x += W + 10;
            }
        }
    }

    private static boolean in(double mx, double my, int x, int y) {
        return mx >= x && mx <= x + W && my >= y && my <= y + H;
    }

    private static String keyName(int key) {
        if (key < 0) return "BRAK";
        return InputUtil.Type.KEYSYM.createFromCode(key).getLocalizedText().getString();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0x66000000);
        for (Panel p : panels) {
            ctx.fill(p.x, p.y, p.x + W, p.y + H, ACCENT);
            ctx.drawTextWithShadow(textRenderer, p.cat.name(), p.x + 4, p.y + 4, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, p.open ? "-" : "+", p.x + W - 10, p.y + 4, 0xFFFFFFFF);
            if (!p.open) continue;

            int y = p.y + H;
            for (Module m : ModuleManager.byCategory(p.cat)) {
                // wiersz modulu
                int bg = m.isEnabled() ? 0xFF3A1C71 : (in(mx, my, p.x, y) ? 0xEE2A2A2A : 0xEE141414);
                ctx.fill(p.x, y, p.x + W, y + H, bg);
                if (m.isEnabled()) ctx.fill(p.x, y, p.x + 2, y + H, ACCENT);
                ctx.drawTextWithShadow(textRenderer, m.name, p.x + 6, y + 4,
                        m.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA);
                y += H;

                if (m.guiOpen) {
                    // przycisk Bind
                    boolean wait = listening == m;
                    ctx.fill(p.x, y, p.x + W, y + H, in(mx, my, p.x, y) ? 0xEE2C2C2C : 0xEE1E1E1E);
                    ctx.drawTextWithShadow(textRenderer,
                            wait ? "Kliknij przycisk..." : "Bind: " + keyName(m.key),
                            p.x + 10, y + 4, wait ? 0xFFFFDD55 : 0xFFDDDDDD);
                    y += H;

                    // przycisk wlacz/wylacz pod bindem
                    ctx.fill(p.x, y, p.x + W, y + H, m.isEnabled() ? 0xFF1F5C2E : 0xFF5C1F1F);
                    ctx.drawTextWithShadow(textRenderer, m.isEnabled() ? "Włączony" : "Wyłączony",
                            p.x + 10, y + 4, 0xFFFFFFFF);
                    y += H;

                    // zasieg: LPM = +0.5, PPM = -0.5
                    if (m.hasRange()) {
                        ctx.fill(p.x, y, p.x + W, y + H, in(mx, my, p.x, y) ? 0xEE2C2C2C : 0xEE1E1E1E);
                        ctx.drawTextWithShadow(textRenderer, "Zasięg: " + m.range,
                                p.x + 10, y + 4, 0xFFDDDDDD);
                        y += H;
                    }

                    // wybor bloku: klik otwiera panel ze wszystkimi blokami
                    if (m.hasBlockPick()) {
                        ctx.fill(p.x, y, p.x + W, y + H, in(mx, my, p.x, y) ? 0xEE2C2C2C : 0xEE1E1E1E);
                        ctx.drawTextWithShadow(textRenderer,
                                textRenderer.trimToWidth("Blok: " + m.block.getName().getString(), W - 32),
                                p.x + 10, y + 4, 0xFFDDDDDD);
                        ctx.drawItem(new ItemStack(m.block), p.x + W - 18, y);
                        y += H;
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        Module wasListening = listening;
        listening = null; // kazde klikniecie anuluje oczekiwanie, chyba ze kliknieto Bind

        for (Panel p : panels) {
            if (in(mx, my, p.x, p.y)) {
                if (button == 0) {
                    dragging = p;
                    dragOffX = (int) mx - p.x;
                    dragOffY = (int) my - p.y;
                } else if (button == 1) {
                    p.open = !p.open;
                }
                return true;
            }
            if (!p.open) continue;

            int y = p.y + H;
            for (Module m : ModuleManager.byCategory(p.cat)) {
                if (in(mx, my, p.x, y)) {
                    if (button == 0) m.toggle();
                    else if (button == 1) m.guiOpen = !m.guiOpen; // prawy = panel bind
                    return true;
                }
                y += H;

                if (m.guiOpen) {
                    if (in(mx, my, p.x, y)) {
                        if (button == 0 && wasListening != m) listening = m;
                        return true;
                    }
                    y += H;
                    if (in(mx, my, p.x, y)) {
                        if (button == 0) m.toggle();
                        return true;
                    }
                    y += H;

                    if (m.hasRange()) {
                        if (in(mx, my, p.x, y)) {
                            if (button == 0) m.range = Math.min(6.0, m.range + 0.5);
                            else if (button == 1) m.range = Math.max(1.0, m.range - 0.5);
                            return true;
                        }
                        y += H;
                    }

                    if (m.hasBlockPick()) {
                        if (in(mx, my, p.x, y)) {
                            if (button == 0) client.setScreen(new BlockPickerScreen(this, m));
                            return true;
                        }
                        y += H;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null && button == 0) {
            dragging.x = (int) mx - dragOffX;
            dragging.y = (int) my - dragOffY;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                // anuluj bez zmiany
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                listening.key = -1; // usun bind
            } else {
                listening.key = keyCode;
            }
            listening = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_APOSTROPHE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
