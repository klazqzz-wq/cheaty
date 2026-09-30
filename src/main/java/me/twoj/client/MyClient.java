package me.twoj.client;

import me.twoj.client.modules.FakeLag;
import me.twoj.client.modules.Freecam;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MyClient implements ClientModInitializer {
    public static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.myclient.gui", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_APOSTROPHE, "category.myclient"));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            FakeLag.reset();
            Freecam.reset();
        });

        ClientTickEvents.START_CLIENT_TICK.register(Freecam::onStartTick);
        WorldRenderEvents.START.register(ctx -> Freecam.onFrame());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }

            // bindy modulow: przelaczanie po nacisnieciu przypisanego klawisza
            long window = client.getWindow().getHandle();
            for (Module m : ModuleManager.all()) {
                if (m.key < 0) { m.keyWasDown = false; continue; }
                boolean down = InputUtil.isKeyPressed(window, m.key);
                if (down && !m.keyWasDown && client.currentScreen == null && client.player != null)
                    m.toggle();
                m.keyWasDown = down;
            }

            if (client.player == null) return;
            for (Module m : ModuleManager.all())
                if (m.isEnabled()) m.onTick();
        });
    }
}
