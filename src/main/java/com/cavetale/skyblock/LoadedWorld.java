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
}
