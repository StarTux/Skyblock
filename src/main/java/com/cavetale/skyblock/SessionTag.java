package com.cavetale.skyblock;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;

@Data
public final class SessionTag implements Serializable {
    protected String name;
    protected UUID inWorld;
    protected long inWorldSince;
    protected Position lastLocation;
}
