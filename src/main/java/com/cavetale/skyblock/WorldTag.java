package com.cavetale.skyblock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
public final class WorldTag implements Serializable {
    protected UUID owner;
    protected long creationTime;
    protected Set<UUID> invites = new HashSet<>();
    protected Map<UUID, Integer> deathCount = new HashMap<>();
    protected int aliveTicks;
    protected SkyblockDifficulty difficulty = SkyblockDifficulty.NORMAL;
    protected Map<UUID, Position> playerPositions = new HashMap<>();
}
