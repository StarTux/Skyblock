package com.cavetale.skyblock;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.skyblock.SkyblockPlugin.plugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * This menu comes after the world select screen.
 * Consider renaming it to Difficulty Select or similar.
 */
@RequiredArgsConstructor
public final class WorldStartGui {
    private final Player player;
    private final WorldChoiceGui worldChoiceGui;
    private SkyblockDifficulty difficulty = SkyblockDifficulty.NORMAL;

    public WorldStartGui enable() {
        return this;
    }

    public void open() {
        Gui gui = new Gui(plugin())
            .size(6 * 9)
            .layer(GuiOverlay.BLANK, BLUE)
            .title(text("Choose a Difficulty", WHITE));
        for (SkyblockDifficulty theDifficulty : SkyblockDifficulty.values()) {
            final int slot = 10 + theDifficulty.ordinal() * 9;
            final boolean selected = theDifficulty == difficulty;
            List<Component> lore = new ArrayList<>();
            lore.add(theDifficulty.displayName);
            lore.add(yesno("health regenerates", theDifficulty.naturalRegeneration));
            lore.add(yesno("disable mob griefing", theDifficulty.disableMobGriefing));
            lore.add(yesno("keep inventory", theDifficulty.keepInventory));
            lore.add(yesno("disable fire spread", theDifficulty.disableFireSpread));
            if (theDifficulty.hardcore) lore.add(textOfChildren(Mytems.ATTENTION, text(" One Life!", DARK_RED)));
            final ItemStack checkbox = selected
                ? Mytems.CROSSED_CHECKBOX.createIcon(lore)
                : Mytems.CHECKBOX.createIcon(lore);
            gui.setItem(slot, theDifficulty.createIcon());
            gui.setItem(slot + 1, checkbox, click -> {
                    if (!click.isLeftClick()) return;
                    onClickDifficulty(theDifficulty);
                });
        }
        final ItemStack okItem = Mytems.OK.createIcon(List.of(text("Start your Journey", GREEN)));
        gui.setItem(43, okItem, click -> {
                if (!click.isLeftClick()) return;
                onClickOk();
            });
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 0.5f);
                worldChoiceGui.open();
            });
        gui.open(player);
    }

    private static Component yesno(String name, boolean value) {
        return textOfChildren(value ? Mytems.ON.component : Mytems.OFF.component,
                              text(" " + tiny(name), value ? GREEN : GRAY));
    }

    private void onClickDifficulty(final SkyblockDifficulty theDifficulty) {
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
        this.difficulty = theDifficulty;
        open();
    }

    private void onClickOk() {
        player.closeInventory();
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
        if (plugin().getWorlds().loadTag(player.getUniqueId()) != null) {
            player.sendMessage(text("You already have a world. Abandon it first!", RED));
            return;
        }
        final LoadedWorld loadedWorld = plugin().getWorlds().create(player.getUniqueId(), worldChoiceGui.getBuildWorld(), difficulty);
        final Session session = plugin().getSessions().get(player.getUniqueId());
        final Location location = loadedWorld.getSpawnLocation();
        plugin().getWorlds().storeCurrentLocation(player);
        plugin().getWorlds().onLeaveWorld(player);
        player.teleport(location);
        plugin().getLogger().info("[RESET] " + player.getName() + " World Start GUI");
        Sessions.resetPlayer(player);
        session.setWorld(loadedWorld);
        session.dirty = true;
        loadedWorld.setLocation(player.getUniqueId(), location);
        loadedWorld.dirty = true;
        player.sendMessage(text("Welcome to your new Skyblock world!", GREEN));
    }
}
