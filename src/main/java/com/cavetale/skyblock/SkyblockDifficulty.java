package com.cavetale.skyblock;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Difficulty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public enum SkyblockDifficulty {
    EASY(text("Easy", GREEN), Difficulty.EASY, true, true, false),
    NORMAL(text("Normal", YELLOW), Difficulty.NORMAL, false, true, false),
    HARD(text("Hard", RED), Difficulty.HARD, false, true, false),
    HARDCORE(text("Hardcore", DARK_RED), Difficulty.HARD, false, false, true);
    ;

    public final Component displayName;
    public final Difficulty difficulty;
    public final boolean keepInventory;
    public final boolean naturalRegeneration;
    public final boolean hardcore;
}
