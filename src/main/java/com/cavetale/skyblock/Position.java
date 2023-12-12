package com.cavetale.skyblock;

import lombok.Value;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Serializable Location.
 */
@Value
public final class Position {
    public final double x;
    public final double y;
    public final double z;
    public final float pitch;
    public final float yaw;

    public Position(final Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
    }

    public Location toLocation(final World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
