package com.cavetale.skyblock;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandContext;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class SkyblockCommand extends AbstractCommand<SkyblockPlugin> {
    protected SkyblockCommand(final SkyblockPlugin plugin) {
        super(plugin, "skyblock");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("start").denyTabCompletion()
            .description("Start a Skyblock world")
            .playerCaller(this::start);
        rootNode.addChild("abandon").denyTabCompletion()
            .description("Abandon your skyblock world")
            .playerCaller(this::abandon);
        rootNode.addChild("home").denyTabCompletion()
            .description("Go to your hardcore world")
            .playerCaller(this::home);
        rootNode.addChild("invite").arguments("<player>")
            .description("Invite someone to your Skyblock world")
            .completers(CommandArgCompleter.PLAYER_CACHE)
            .playerCaller(this::invite);
        rootNode.addChild("uninvite").arguments("<player>")
            .description("Remove an invitation")
            .completers(this::completeUninvite)
            .playerCaller(this::uninvite);
        rootNode.addChild("join").arguments("<player>")
            .description("Accept an invite")
            .completers(CommandArgCompleter.NULL)
            .playerCaller(this::join);
        rootNode.addChild("leave").denyTabCompletion()
            .description("Leave the current world")
            .playerCaller(this::leave);
    }

    protected void start(Player player) {
        if (plugin.getWorlds().loadTag(player.getUniqueId()) != null) {
            throw new CommandWarn("You already have a world. Abandon it first!");
        }
        new WorldChoiceGui(player).enable().open();
    }

    private void abandon(Player player) {
        if (plugin.getWorlds().loadTag(player.getUniqueId()) == null) {
            throw new CommandWarn("You do not have a world!");
        }
        new WorldAbandonGui(player).open();
    }

    private void home(Player player) {
        WorldTag tag = plugin.getWorlds().loadTag(player.getUniqueId());
        if (tag == null) {
            throw new CommandWarn("You do not have a world");
        }
        if (tag.difficulty.hardcore && tag.deathCount.getOrDefault(player.getUniqueId(), 0) > 0) {
            throw new CommandWarn("You died in this Hardcore world");
        }
        LoadedWorld loadedWorld = plugin.getWorlds().getOrLoad(player.getUniqueId());
        if (plugin.getWorlds().in(player.getWorld()) == loadedWorld) {
            throw new CommandWarn("You are already in your world");
        }
        loadedWorld.tag.lastUseTime = System.currentTimeMillis();
        loadedWorld.tag.updateComments();
        plugin.getWorlds().storeCurrentLocation(player);
        plugin.getWorlds().onLeaveWorld(player);
        player.teleport(loadedWorld.getLocation(player.getUniqueId()));
        plugin.getWorlds().onJoinWorld(player);
        Session session = plugin.getSessions().get(player.getUniqueId());
        session.setWorld(loadedWorld);
        session.dirty = true;
        player.sendMessage(text("Welcome back to your world!", GREEN));
    }

    private boolean invite(Player player, String[] args) {
        if (args.length != 1) return false;
        final PlayerCache target = CommandArgCompleter.requirePlayerCache(args[0]);
        if (target.equals(player)) throw new CommandWarn("You cannot invite yourself");
        final LoadedWorld loadedWorld = plugin.getWorlds().in(player.getWorld());
        if (loadedWorld == null || !loadedWorld.uuid.equals(player.getUniqueId())) {
            throw new CommandWarn("This world does not belong to you!");
        }
        if (loadedWorld.tag.difficulty.hardcore && loadedWorld.getDeathCount(target.uuid) > 0) {
            throw new CommandWarn(target.name + " died in this Hardcore world");
        }
        if (!loadedWorld.tag.invites.contains(target.uuid)) {
            loadedWorld.tag.invites.add(target.uuid);
            loadedWorld.dirty = true;
        }
        final String cmd = "/skyblock join " + player.getName();
        player.sendMessage(text("Invite sent to " + target.name, GREEN));
        Player targetPlayer = Bukkit.getPlayer(target.uuid);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(text(player.getName() + " invited you to their Skyblock world. Click here to accept", GREEN)
                                     .hoverEvent(showText(text(cmd)))
                                     .clickEvent(runCommand(cmd)));
        }
        return true;
    }

    private boolean uninvite(Player player, String[] args) {
        if (args.length != 1) return false;
        final PlayerCache target = CommandArgCompleter.requirePlayerCache(args[0]);
        if (target.equals(player)) throw new CommandWarn("You cannot invite yourself");
        final LoadedWorld loadedWorld = plugin.getWorlds().in(player.getWorld());
        if (loadedWorld == null || !loadedWorld.uuid.equals(player.getUniqueId())) {
            throw new CommandWarn("This world does not belong to you!");
        }
        // Edit world tag
        final boolean result = loadedWorld.getTag().getInvites().remove(target.getUniqueId());
        loadedWorld.setDirty(true);
        // Warp player out
        final Player targetPlayer = Bukkit.getPlayer(target.getUuid());
        final boolean playerPresent = targetPlayer != null && targetPlayer.getWorld().equals(loadedWorld.getWorld());
        if (playerPresent) {
            plugin.getLogger().info("[RESET] " + targetPlayer.getName() + " Uninvite Command Target");
            plugin.teleportToLobby(targetPlayer);
        }
        // Error message
        if (!result && !playerPresent) {
            throw new CommandWarn(target.getName() + " was not invited");
        }
        player.sendMessage(text("Revoked invitation from " + target.getName(), YELLOW));
        return true;
    }

    private List<String> completeUninvite(CommandContext context, CommandNode node, String arg) {
        if (!context.isPlayer()) return List.of();
        final LoadedWorld loadedWorld = plugin.getWorlds().in(context.player.getWorld());
        if (loadedWorld == null || !loadedWorld.uuid.equals(context.player.getUniqueId())) {
            return List.of();
        }
        final String lower = arg.toLowerCase();
        final List<String> result = new ArrayList<>();
        for (UUID uuid : loadedWorld.getTag().getInvites()) {
            final String name = PlayerCache.nameForUuid(uuid);
            if (name.toLowerCase().contains(lower)) {
                result.add(name);
            }
        }
        return result;
    }

    private boolean join(Player player, String[] args) {
        if (args.length != 1) return false;
        final PlayerCache inviter = CommandArgCompleter.requirePlayerCache(args[0]);
        final WorldTag tag = plugin.getWorlds().loadTag(inviter.uuid);
        if (tag == null || (!inviter.uuid.equals(player.getUniqueId()) && !tag.invites.contains(player.getUniqueId()))) {
            throw new CommandWarn(inviter.name + " did not invite you!");
        }
        if (tag.difficulty.hardcore && tag.deathCount.getOrDefault(player.getUniqueId(), 0) > 0) {
            throw new CommandWarn("You died in this Hardcore world");
        }
        plugin.getWorlds().storeCurrentLocation(player);
        final LoadedWorld targetWorld = plugin.getWorlds().getOrLoad(inviter.uuid);
        if (plugin.getWorlds().in(player.getWorld()) == targetWorld) {
            throw new CommandWarn("You are already in this world");
        }
        targetWorld.tag.lastUseTime = System.currentTimeMillis();
        targetWorld.tag.updateComments();
        plugin.getWorlds().onLeaveWorld(player);
        player.teleport(targetWorld.getLocation(player.getUniqueId()));
        plugin.getWorlds().onJoinWorld(player);
        final Session session = plugin.getSessions().get(player.getUniqueId());
        session.setWorld(targetWorld);
        session.dirty = true;
        player.sendMessage(text("You joined the Skyblock world of " + inviter.name, GREEN));
        Player inviterPlayer = Bukkit.getPlayer(inviter.uuid);
        if (inviterPlayer != null) {
            inviterPlayer.sendMessage(text(player.getName() + " joined your world", GREEN));
        }
        return true;
    }

    private void leave(Player player) {
        final LoadedWorld loadedWorld = plugin.getWorlds().in(player.getWorld());
        if (loadedWorld == null) {
            throw new CommandWarn("You are not playing in a world");
        }
        plugin.getLogger().info("[RESET] " + player.getName() + " Leave Command");
        plugin.teleportToLobby(player);
        player.sendMessage(text("You left the Skyblock world of " + PlayerCache.nameForUuid(loadedWorld.uuid) + "!", YELLOW));
    }
}
