package com.cavetale.skyblock;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

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
        if (loadedWorld == null) return;
        // Respawn in same world
        if (event.getRespawnLocation().getWorld().equals(loadedWorld.world)) return;
        // Set to world spawn
        event.setRespawnLocation(loadedWorld.world.getSpawnLocation());
    }
}
