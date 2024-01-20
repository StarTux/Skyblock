package com.cavetale.skyblock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Difficulty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Getter
@RequiredArgsConstructor
public enum SkyblockDifficulty {
    EASY(text("Easy", GREEN), Difficulty.EASY, true, true, true, true, false),
    NORMAL(text("Normal", YELLOW), Difficulty.NORMAL, false, true, false, true, false),
    HARD(text("Hard", RED), Difficulty.HARD, false, true, false, false, false),
    HARDCORE(text("Hardcore", DARK_RED), Difficulty.HARD, false, false, false, false, true);
    ;

    public final Component displayName;
    public final Difficulty difficulty;
    public final boolean keepInventory;
    public final boolean naturalRegeneration;
    public final boolean disableFireSpread;
    public final boolean disableMobGriefing;
    public final boolean hardcore;
}
