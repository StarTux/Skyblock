package com.cavetale.skyblock;

import com.cavetale.core.util.Json;
import java.io.File;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

@RequiredArgsConstructor
public final class LoadedWorld {
    protected final World world;
    protected final UUID uuid;
    protected WorldTag tag;
    protected boolean dirty;
    protected int emptyTicks; // See Worlds

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
}
