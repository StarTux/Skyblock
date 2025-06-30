package com.cavetale.skyblock;

import com.cavetale.mytems.Mytems;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Difficulty;
import org.bukkit.inventory.ItemStack;
import static io.papermc.paper.datacomponent.item.TooltipDisplay.tooltipDisplay;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Getter
@RequiredArgsConstructor
public enum SkyblockDifficulty {
    EASY(Mytems.WINK_SMILE::createIcon, text("Easy", GREEN), Difficulty.EASY, true, true, true, true, false),
    NORMAL(Mytems.SMILE::createIcon, text("Normal", YELLOW), Difficulty.NORMAL, false, true, false, true, false),
    HARD(Mytems.FROWN::createIcon, text("Hard", RED), Difficulty.HARD, false, true, false, false, false),
    HARDCORE(Mytems.SKELETON_FACE::createIcon, text("Hardcore", DARK_RED), Difficulty.HARD, false, false, false, false, true);
    ;

    public final Supplier<ItemStack> iconSupplier;
    public final Component displayName;
    public final Difficulty difficulty;
    public final boolean keepInventory;
    public final boolean naturalRegeneration;
    public final boolean disableFireSpread;
    public final boolean disableMobGriefing;
    public final boolean hardcore;

    public ItemStack createIcon() {
        final ItemStack result = iconSupplier.get();
        result.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay().hideTooltip(true));
        return result;
    }
}
