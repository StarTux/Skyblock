package com.cavetale.skyblock;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skyblock.SkyblockPlugin.plugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public final class WorldAbandonGui {
    private final Player player;

    public void open() {
        final int size = 1 * 9;
        Gui gui = new Gui(plugin()).size(size);
        GuiOverlay.Builder builder = GuiOverlay.BLANK.builder(size, RED)
            .title(text("Abandon Skyblock World", WHITE));
        final ItemStack okItem = Mytems.NO.createIcon(List.of(text("Abandon your world", RED)));
        gui.setItem(4, okItem, click -> {
                if (!click.isLeftClick()) return;
                onClickOk();
            });
        gui.title(builder.build());
        gui.open(player);
    }

    private void onClickOk() {
        LoadedWorld loadedWorld = plugin().getWorlds().get(player.getUniqueId());
        if (loadedWorld != null) {
            for (Player playerInWorld : loadedWorld.world.getPlayers()) {
                playerInWorld.teleport(plugin().getWorlds().getLobbyWorld().getSpawnLocation());
                Sessions.resetPlayer(playerInWorld);
                playerInWorld.setGameMode(GameMode.ADVENTURE);
            }
            if (!plugin().getWorlds().unload(loadedWorld)) {
                player.sendMessage(text("Cannot unload world", RED));
                return;
            }
        }
        if (!plugin().getWorlds().delete(player.getUniqueId())) {
            player.sendMessage(text("You do not have a world!", RED));
            return;
        }
        player.sendMessage(text("Your Skyblock world was successfully abandoned", RED));
        player.closeInventory();
    }
}
