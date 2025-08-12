package com.cavetale.skyblock;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SkyblockPlugin extends JavaPlugin {
    protected static SkyblockPlugin instance;
    protected final SkyblockCommand skyblockCommand = new SkyblockCommand(this);
    protected final SkyblockAdminCommand skyblockAdminCommand = new SkyblockAdminCommand(this);
    protected final Worlds worlds = new Worlds();
    protected final Sessions sessions = new Sessions();

    public SkyblockPlugin() {
        instance = this;
    }

    @Override
    public void onEnable() {
        skyblockCommand.enable();
        skyblockAdminCommand.enable();
        worlds.enable();
        sessions.enable();
        new EventListener(this).enable();
    }

    @Override
    public void onDisable() {
        sessions.disable();
        worlds.disable();
    }

    public void teleportToLobby(Player player) {
        worlds.storeCurrentLocation(player);
        worlds.onLeaveWorld(player);
        player.teleport(worlds.getLobbyWorld().getSpawnLocation());
        Sessions.resetPlayer(player);
        final Session session = sessions.get(player.getUniqueId());
        session.clearWorld();
        sessions.save(session);
    }

    public static SkyblockPlugin plugin() {
        return instance;
    }
}
