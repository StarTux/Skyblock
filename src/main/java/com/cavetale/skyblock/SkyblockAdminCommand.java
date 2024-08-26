package com.cavetale.skyblock;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.util.Json;
import java.io.File;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class SkyblockAdminCommand extends AbstractCommand<SkyblockPlugin> {
    protected SkyblockAdminCommand(final SkyblockPlugin plugin) {
        super(plugin, "skyblockadmin");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("fixlastused").denyTabCompletion()
            .description("Fix the missing last used value in all worlds")
            .senderCaller(this::fixLastUsed);
    }

    private void fixLastUsed(CommandSender sender) {
        final File worldsFolder = Bukkit.getWorldContainer();
        int count = 0;
        int noTag = 0;
        int noLastModified = 0;
        int success = 0;
        for (File worldFolder : worldsFolder.listFiles()) {
            if (!worldFolder.isDirectory()) continue;
            final File skyblockFile = new File(worldFolder, "skyblock.json");
            if (!skyblockFile.isFile()) continue;
            final WorldTag tag = Json.load(skyblockFile, WorldTag.class);
            if (tag == null) {
                noTag += 1;
                continue;
            }
            count += 1;
            final long lastUsed = skyblockFile.lastModified();
            if (lastUsed == 0L) {
                noLastModified += 1;
                continue;
            }
            tag.lastUseTime = lastUsed;
            tag.updateComments();
            Json.save(skyblockFile, tag, true);
            success += 1;
        }
        sender.sendMessage(text("Visited " + count + " world folders,"
                                + " success=" + success
                                + " noTag=" + noTag
                                + " noLastModified=" + noLastModified,
                                YELLOW));
    }
}
