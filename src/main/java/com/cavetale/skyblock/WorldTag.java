package com.cavetale.skyblock;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class WorldTag {
    protected UUID owner;
    protected long creationTime;
    protected Set<UUID> joined = new HashSet<>();
}
