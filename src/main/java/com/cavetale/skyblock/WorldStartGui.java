package com.cavetale.skyblock;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.skyblock.SkyblockPlugin.plugin;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public final class WorldStartGui {
    private final Player player;
    private SkyblockDifficulty difficulty = SkyblockDifficulty.NORMAL;

    public void open() {
        final int size = 6 * 9;
        Gui gui = new Gui(plugin()).size(size);
        GuiOverlay.Builder builder = GuiOverlay.BLANK.builder(size, BLUE)
            .title(text("New Skyblock World", WHITE));
        for (SkyblockDifficulty theDifficulty : SkyblockDifficulty.values()) {
            final int slot = 10 + theDifficulty.ordinal() * 9;
            final boolean selected = theDifficulty == difficulty;
            List<Component> lore = new ArrayList<>();
            lore.add(theDifficulty.displayName);
            lore.add(yesno("keep inventory", theDifficulty.keepInventory));
            lore.add(yesno("health regenerates", theDifficulty.naturalRegeneration));
            if (theDifficulty.hardcore) lore.add(text("One Life!", DARK_RED));
            ItemStack item = selected
                ? Mytems.CROSSED_CHECKBOX.createIcon(lore)
                : Mytems.CHECKBOX.createIcon(lore);
            gui.setItem(slot, item, click -> {
                    if (!click.isLeftClick()) return;
                    onClickDifficulty(theDifficulty);
                });
        }
        final ItemStack okItem = Mytems.OK.createIcon(List.of(text("Start your Journey", GREEN)));
        gui.setItem(43, okItem, click -> {
                if (!click.isLeftClick()) return;
                onClickOk();
            });
        gui.title(builder.build());
        gui.open(player);
    }

    private static Component yesno(String name, boolean value) {
        return textOfChildren(text(tiny(name), value ? GREEN : GRAY),
                              space(),
                              value ? Mytems.ON.component : Mytems.OFF.component);
    }

    private void onClickDifficulty(final SkyblockDifficulty theDifficulty) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        this.difficulty = theDifficulty;
        open();
    }

    private void onClickOk() {
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        if (plugin().getWorlds().loadTag(player.getUniqueId()) != null) {
            player.sendMessage(text("You already have a world. Abandon it first!", RED));
            return;
        }
        final LoadedWorld loadedWorld = plugin().getWorlds().create(player.getUniqueId(), difficulty);
        final Session session = plugin().getSessions().get(player.getUniqueId());
        final Location location = loadedWorld.getSpawnLocation();
        plugin().getWorlds().storeCurrentLocation(player);
        plugin().getWorlds().onLeaveWorld(player);
        player.teleport(location);
        Sessions.resetPlayer(player);
        player.setGameMode(GameMode.SURVIVAL);
        session.setWorld(loadedWorld);
        session.dirty = true;
        loadedWorld.setLocation(player.getUniqueId(), location);
        loadedWorld.dirty = true;
        player.sendMessage(text("Welcome to your new Skyblock world!", GREEN));
    }
}
