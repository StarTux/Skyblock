package com.cavetale.skyblock;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Session {
    protected final UUID uuid;
    protected SessionTag tag;
    protected boolean dirty;

    protected void clearWorld() {
        tag.inWorld = null;
        tag.inWorldSince = 0L;
        dirty = true;
    }

    protected void setWorld(LoadedWorld loadedWorld) {
        tag.inWorld = loadedWorld.uuid;
        tag.inWorldSince = System.currentTimeMillis();
        dirty = true;
    }
}
