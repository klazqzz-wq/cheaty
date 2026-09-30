package me.twoj.client;

import me.twoj.client.modules.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        modules.add(new KillAura());
        modules.add(new AutoWeb());
        modules.add(new Trap());
        modules.add(new Sprint());
        modules.add(new FakeLag());
        modules.add(new Fullbright());
        modules.add(new Freecam());
    }

    public static List<Module> all() { return modules; }

    public static List<Module> byCategory(Module.Category c) {
        return modules.stream().filter(m -> m.category == c).toList();
    }
}
