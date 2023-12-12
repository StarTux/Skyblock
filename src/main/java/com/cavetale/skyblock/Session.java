package com.cavetale.skyblock;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@RequiredArgsConstructor
public final class Session {
    protected final UUID uuid;
    protected SessionTag tag;
    protected boolean dirty;
    protected Set<UUID> invites = new HashSet<>();

    protected void clearWorld() {
        tag.inWorld = null;
        tag.inWorldSince = 0L;
        tag.lastLocation = null;
        dirty = true;
    }

    protected void setLocation(LoadedWorld loadedWorld, Location location) {
        tag.inWorld = loadedWorld.uuid;
        tag.lastLocation = new Position(location);
        tag.inWorldSince = System.currentTimeMillis();
        dirty = true;
    }
}
