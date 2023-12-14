package com.cavetale.skyblock;

import com.cavetale.core.event.hud.PlayerHudEvent;
import com.cavetale.core.event.hud.PlayerHudPriority;
import com.cavetale.core.event.player.PlayerTPAEvent;
import com.cavetale.core.playercache.PlayerCache;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import static com.cavetale.core.font.Unicode.tiny;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public final class EventListener implements Listener {
    private final SkyblockPlugin plugin;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, SkyblockPlugin.plugin());
    }

    @EventHandler
    private void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        final Player player = event.getPlayer();
        final Session session = plugin.getSessions().load(player.getUniqueId());
        if (session.tag.inWorld == null || session.tag.lastLocation == null) {
            event.setSpawnLocation(plugin.getWorlds().getLobbyWorld().getSpawnLocation());
            return;
        }
        LoadedWorld loadedWorld = plugin.getWorlds().get(session.tag.inWorld);
        if (loadedWorld == null) {
            // World not currently loaded
            WorldTag worldTag = plugin.getWorlds().loadTag(session.tag.inWorld);
            if (worldTag != null && worldTag.creationTime < session.tag.inWorldSince) {
                loadedWorld = plugin.getWorlds().load(session.tag.inWorld);
            }
        }
        if (loadedWorld == null) {
            // World does not exist (anymore)
            session.clearWorld();
            plugin.getSessions().save(session);
            event.setSpawnLocation(plugin.getWorlds().getLobbyWorld().getSpawnLocation());
            return;
        }
        assert loadedWorld != null && session.tag.lastLocation != null;
        event.setSpawnLocation(session.tag.lastLocation.toLocation(loadedWorld.world));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (player.getWorld().equals(plugin.getWorlds().getLobbyWorld())) {
            Sessions.resetPlayer(player);
            player.setGameMode(GameMode.ADVENTURE);
        }
        if (plugin.getWorlds().in(player.getWorld()) != null) {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Session session = plugin.getSessions().get(player.getUniqueId());
        final LoadedWorld loadedWorld = plugin.getWorlds().in(player.getWorld());
        if (loadedWorld != null) {
            session.setLocation(loadedWorld, player.getLocation());
            plugin.getSessions().save(session);
        }
        plugin.getSessions().unload(session);
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final LoadedWorld loadedWorld = plugin.getWorlds().in(player.getWorld());
        // Not in Skyblock world
        if (loadedWorld == null) {
            event.setRespawnLocation(plugin.getWorlds().getLobbyWorld().getSpawnLocation());
            return;
        }
        // Hardcore
        if (loadedWorld.tag.difficulty.hardcore) {
            event.setRespawnLocation(plugin.getWorlds().getLobbyWorld().getSpawnLocation());
            final Session session = plugin.getSessions().get(player.getUniqueId());
            session.clearWorld();
            plugin.getSessions().save(session);
            player.sendMessage(text("You died in a Hardcore Skyblock world!", DARK_RED));
            return;
        }
        // Respawn in same world
        if (event.getRespawnLocation().getWorld().equals(loadedWorld.world)) return;
        // Set to world spawn
        event.setRespawnLocation(loadedWorld.world.getSpawnLocation());
    }

    @EventHandler
    private void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        final Player player = event.getPlayer();
        if (plugin.getWorlds().getLobbyWorld().equals(player)) {
            Sessions.resetPlayer(player);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    @EventHandler
    private void onPlayerTPA(PlayerTPAEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerHud(PlayerHudEvent event) {
        final Player player = event.getPlayer();
        LoadedWorld loadedWorld = plugin.getWorlds().in(player.getWorld());
        if (loadedWorld == null) {
            event.footer(PlayerHudPriority.DEFAULT, List.of(text("Skyblock Lobby", BLUE)));
        } else {
            event.footer(PlayerHudPriority.DEFAULT, List.of(textOfChildren(text(tiny("world "), GRAY), text(PlayerCache.nameForUuid(loadedWorld.uuid), WHITE)),
                                                            textOfChildren(text(tiny("age"), GRAY),
                                                                           text(" " + loadedWorld.days, WHITE), text("d", GRAY),
                                                                           text(" " + loadedWorld.hours, WHITE), text("h", GRAY),
                                                                           text(" " + loadedWorld.minutes, WHITE), text("m", GRAY),
                                                                           text(" " + loadedWorld.seconds, WHITE), text("s", GRAY)),
                                                            textOfChildren(text(tiny("difficulty "), GRAY), loadedWorld.tag.difficulty.displayName)));
        }
    }
}
