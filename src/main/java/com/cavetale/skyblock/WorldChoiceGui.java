package com.cavetale.skyblock;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import com.winthier.creative.BuildWorld;
import com.winthier.creative.BuildWorldPurpose;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skyblock.SkyblockPlugin.plugin;
import static io.papermc.paper.datacomponent.item.TooltipDisplay.tooltipDisplay;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Getter
@RequiredArgsConstructor
public final class WorldChoiceGui {
    private final Player player;
    private BuildWorld buildWorld;
    private final List<BuildWorld> availableWorlds = new ArrayList<>();
    private boolean importsLoaded;
    private int page = 0;

    public WorldChoiceGui enable() {
        buildWorld = BuildWorld.findWithPath("skyblock");
        if (buildWorld == null) {
            throw new IllegalStateException("BuildWorld not found: skyblock");
        }
        availableWorlds.add(buildWorld);
        return this;
    }

    public void open() {
        if (buildWorld == null) {
            throw new IllegalStateException("BuildWorld not initialized");
        }
        Gui gui = new Gui(plugin())
            .size(6 * 9)
            .layer(GuiOverlay.BLANK, BLUE);
        // Skyblock worlds
        final int pageSize = 8;
        final int listOffset = page * pageSize;
        final int maxPage = (availableWorlds.size() - 1) / pageSize;
        if (maxPage > 0) {
            gui.title(textOfChildren(text("Choose a World ", WHITE),
                                     text("(" + (page + 1) + "/" + (maxPage + 1) + ")", GRAY)));
        } else {
            gui.title(text("Choose a World", WHITE));
        }
        for (int i = 0; i < 8; i += 1) {
            final int availableWorldsIndex = i + listOffset;
            if (availableWorldsIndex >= availableWorlds.size()) break;
            final int column = (i / 4) * 3;
            final int row = i % 4;
            final BuildWorld theBuildWorld = availableWorlds.get(availableWorldsIndex);
            final ItemStack icon;
            if (theBuildWorld.getRow().parsePurpose() == BuildWorldPurpose.SKYBLOCK) {
                icon = new ItemStack(Material.GRASS_BLOCK);
            } else {
                icon = Mytems.PHOTO.createIcon();
            }
            icon.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay().hideTooltip(true));
            gui.setItem(1 + column, 1 + row, icon, null);
            final ItemStack checkbox = buildWorld == theBuildWorld
                ? Mytems.CROSSED_CHECKBOX.createIcon(theBuildWorld.getTooltipLines())
                : Mytems.CHECKBOX.createIcon(theBuildWorld.getTooltipLines());
            gui.setItem(2 + column, 1 + row, checkbox, click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
                    buildWorld = theBuildWorld;
                    open();
                });
        }
        if (page > 0) {
            gui.setItem(0, 0, Mytems.ARROW_LEFT.createIcon(List.of(text("Previous Page", GRAY))), click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
                    page -= 1;
                    open();
                });
        }
        if (page < maxPage) {
            gui.setItem(8, 0, Mytems.ARROW_RIGHT.createIcon(List.of(text("Next Page", GRAY))), click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
                    page += 1;
                    open();
                });
        }
        // Import
        if (!importsLoaded && player.hasPermission("skyblock.import")) {
            gui.setItem(7, 1, Mytems.FLOPPY_DISK.createIcon(List.of(text("Import from Creative", LIGHT_PURPLE))), click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
                    importsLoaded = true;
                    final List<BuildWorld> owned = BuildWorld.findOwnedWorlds(player.getUniqueId());
                    owned.sort(Comparator.comparing(BuildWorld::getName));
                    availableWorlds.addAll(owned);
                    open();
                });
        }
        // OK
        final ItemStack okItem = Mytems.OK.createIcon(List.of(text("Continue", GREEN)));
            gui.setItem(7, 4, okItem, click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
                new WorldStartGui(player, this).enable().open();
            });
        gui.open(player);
    }
}
