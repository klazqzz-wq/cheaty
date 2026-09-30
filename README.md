# MyClient (Fabric, Minecraft 1.21.4)

ClickGUI otwierane apostrofem (`'`). Lewy przycisk na module = wlacz/wylacz,
przeciaganie naglowka = przesuwanie panelu, prawy przycisk na naglowku = zwijanie.

## Budowanie
1. Zainstaluj JDK 21.
2. Otworz folder w IntelliJ IDEA (zaimportuje `build.gradle`) i uruchom task `build`,
   albo w terminalu (jesli masz Gradle 8.x): `gradle build`.
3. Gotowy jar: `build/libs/myclient-1.0.0.jar` (bez `-sources`).
4. Wrzuc go do folderu `mods` razem z Fabric API.

Inna wersja Minecrafta? Zmien wartosci w `gradle.properties`
(wygeneruj je na https://fabricmc.net/develop) i dostosuj `depends` w `fabric.mod.json`.

## Dodawanie modulu
Stworz klase dziedziczaca po `Module` w `modules/` i dodaj ja w `ModuleManager.init()`.

## Moduly
- COMBAT: KillAura (gracze i moby w zasiegu 3.5), AutoWeb (pajeczyna pod najblizszym graczem, wymaga cobweb w hotbarze), Trap (blok wokol najblizszego gracza, domyslnie obsydian - wybierasz w panelu, wymaga tego bloku w hotbarze)
- MOVEMENT: Sprint, FakeLag (wstrzymuje pakiety ruchu i wysyla je paczkami co 8 tickow)
- RENDER: Fullbright
- PLAYER: Freecam (kamera-duch: WASD + spacja/shift, postac stoi w miejscu i nic nie robi)

Zasieg KillAura/AutoWeb/Trap: domyslnie 3 bloki, zmieniany w panelu modulu (LPM +0.5, PPM -0.5, zakres 1-6).
AutoWeb nie sprawdza widocznosci, wiec dziala przez sciany. AutoWeb/Trap stawiaja tez "w powietrzu", gdy nie ma sasiedniego bloku.

## GUI
- Lewy klik na module: wlacz/wylacz
- Prawy klik na module: rozwija panel: Bind, przelacznik Wlaczony/Wylaczony i (dla KillAura/AutoWeb/Trap) Zasieg
- Trap: wiersz "Blok" otwiera panel wszystkich blokow (wyszukiwarka, kolko myszy = przewijanie, klik = wybor, Esc = powrot)
- KillAura bije tez przez bloki (nie sprawdza widocznosci)
- Bind: klik -> "Kliknij przycisk..." -> nacisnij klawisz (Backspace/Delete = usun bind, Esc = anuluj)
