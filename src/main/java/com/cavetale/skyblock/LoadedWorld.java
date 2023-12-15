package com.cavetale.skyblock;

import com.cavetale.core.util.Json;
import java.io.File;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;

@RequiredArgsConstructor
public final class LoadedWorld {
    protected final World world;
    protected final UUID uuid;
    protected WorldTag tag;
    protected boolean dirty;
    // Ticking
    protected int emptyTicks; // See Worlds
    // Age
    protected int seconds;
    protected int minutes;
    protected int hours;
    protected int days;

    protected File getTagFile() {
        return new File(world.getWorldFolder(), "skyblock.json");
    }

    protected void load() {
        tag = Json.load(getTagFile(), WorldTag.class, WorldTag::new);
    }

    protected void save() {
        dirty = false;
        Json.save(getTagFile(), tag, true);
    }

    protected boolean saveIfDirty() {
        if (!dirty) return false;
        save();
        return true;
    }

    protected void tick() {
        final int theTicks = tag.aliveTicks++;
        if (theTicks % 20 == 0) {
            seconds = tag.aliveTicks / 20;
            minutes = seconds / 60;
            hours = minutes / 60;
            days = hours / 24;
            seconds %= 60;
            minutes %= 60;
            hours %= 24;
        }
        if (theTicks % 1200 == 0) {
            dirty = true;
        }
    }

    protected void setLocation(UUID playerId, Location location) {
        tag.playerPositions.put(playerId, new Position(location));
        dirty = true;
    }

    protected Position clearLocation(UUID playerId) {
        Position result = tag.playerPositions.remove(playerId);
        if (result != null) dirty = true;
        return result;
    }

    protected Location getLocation(UUID playerId) {
        Position position = tag.playerPositions.get(playerId);
        return position != null
            ? position.toLocation(world)
            : getSpawnLocation();
    }

    public Location getSpawnLocation() {
        return world.getSpawnLocation();
    }

    protected void increaseDeathCount(UUID playerId) {
        final int deaths = tag.deathCount.getOrDefault(playerId, 0);
        tag.deathCount.put(playerId, deaths + 1);
        dirty = true;
    }

    protected int getDeathCount(UUID playerId) {
        return tag.deathCount.getOrDefault(playerId, 0);
    }
}
