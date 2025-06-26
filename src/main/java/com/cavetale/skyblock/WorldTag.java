package com.cavetale.skyblock;

import com.cavetale.core.playercache.PlayerCache;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
public final class WorldTag implements Serializable {
    // Commentary
    protected String ownerName;
    protected String creationTimeName;
    protected String lastUseTimeName;
    protected String originWorldPath = "skyblock";
    // Data
    protected UUID owner;
    protected long creationTime;
    protected long lastUseTime;
    protected Set<UUID> invites = new HashSet<>();
    protected Map<UUID, Integer> deathCount = new HashMap<>();
    protected int aliveTicks;
    protected SkyblockDifficulty difficulty = SkyblockDifficulty.NORMAL;
    protected Map<UUID, Position> playerPositions = new HashMap<>();

    protected void updateComments() {
        ownerName = owner != null
            ? PlayerCache.nameForUuid(owner)
            : null;
        creationTimeName = new Date(creationTime).toString();
        lastUseTimeName = new Date(lastUseTime).toString();
    }
}
