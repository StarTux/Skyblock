package com.cavetale.skyblock;

import com.cavetale.core.util.Json;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import static com.cavetale.skyblock.SkyblockPlugin.plugin;

public final class Sessions {
    private File sessionsFolder;
    private Map<UUID, Session> sessionsMap = new HashMap<>();

    protected void enable() {
        sessionsFolder = new File(plugin().getDataFolder(), "players");
        sessionsFolder.mkdirs();
        Bukkit.getScheduler().runTaskTimer(plugin(), this::tick, 1L, 1L);
        for (Player player : Bukkit.getOnlinePlayers()) {
            load(player.getUniqueId());
        }
    }

    protected void disable() {
        for (Session session : sessionsMap.values()) {
            saveIfDirty(session);
        }
        sessionsMap.clear();
    }

    public Session get(UUID uuid) {
        return sessionsMap.get(uuid);
    }

    protected Session load(UUID uuid) {
        Session session = new Session(uuid);
        File file = new File(sessionsFolder, uuid + ".json");
        session.tag = Json.load(file, SessionTag.class, SessionTag::new);
        sessionsMap.put(uuid, session);
        return session;
    }

    protected void unload(Session session) {
        sessionsMap.remove(session.uuid);
    }

    protected Session unload(UUID uuid) {
        return sessionsMap.remove(uuid);
    }

    protected void save(Session session) {
        session.dirty = false;
        File file = new File(sessionsFolder, session.uuid + ".json");
        Json.save(file, session.tag, true);
    }

    protected void saveIfDirty(Session session) {
        if (!session.dirty) return;
        save(session);
    }

    protected void tick() {
        for (Session session : sessionsMap.values()) {
            saveIfDirty(session);
        }
    }

    protected void storeCurrentWorld(Player player) {
        final LoadedWorld loadedWorld = plugin().getWorlds().in(player.getWorld());
        final Session session = get(player.getUniqueId());
        if (loadedWorld == null) {
            session.clearWorld();
        } else {
            session.setWorld(loadedWorld);
        }
        save(session);
    }

    protected static void resetPlayer(Player player) {
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);
        player.setFreezeTicks(0);
        player.setFallDistance(0);
        player.setGameMode(GameMode.SURVIVAL);
    }
}
