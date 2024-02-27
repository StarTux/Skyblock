package com.cavetale.skyblock;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.playercache.PlayerCache;
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
            .completers(CommandArgCompleter.NULL)
            .playerCaller(this::invite);
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
        new WorldStartGui(player).open();
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
        if (!loadedWorld.tag.invites.contains(target.uuid)) {
            throw new CommandWarn(target.name + " is not invited");
        }
        loadedWorld.tag.invites.remove(target.uuid);
        loadedWorld.dirty = true;
        player.sendMessage(text("Uninvited: " + target.name, YELLOW));
        Player targetPlayer = Bukkit.getPlayer(target.uuid);
        if (targetPlayer != null && plugin.getWorlds().in(targetPlayer.getWorld()) == loadedWorld) {
            plugin.getWorlds().storeCurrentLocation(targetPlayer);
            plugin.getWorlds().onLeaveWorld(targetPlayer);
            targetPlayer.teleport(plugin.getWorlds().getLobbyWorld().getSpawnLocation());
            plugin.getLogger().info("[RESET] " + player.getName() + " Uninvite Command Target");
            Sessions.resetPlayer(targetPlayer);
            Session targetSession = plugin.getSessions().get(target.uuid);
            targetSession.clearWorld();
        }
        return true;
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
        plugin.getWorlds().storeCurrentLocation(player);
        plugin.getWorlds().onLeaveWorld(player);
        player.teleport(plugin.getWorlds().getLobbyWorld().getSpawnLocation());
        plugin.getLogger().info("[RESET] " + player.getName() + " Leave Command");
        Sessions.resetPlayer(player);
        Session session = plugin.getSessions().get(player.getUniqueId());
        session.clearWorld();
        plugin.getSessions().save(session);
        player.sendMessage(text("You left the Skyblock world of " + PlayerCache.nameForUuid(loadedWorld.uuid) + "!", YELLOW));
    }
}
