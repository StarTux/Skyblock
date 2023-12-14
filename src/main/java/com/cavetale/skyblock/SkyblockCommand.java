package com.cavetale.skyblock;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.playercache.PlayerCache;
import org.bukkit.GameMode;
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

    private boolean invite(Player player, String[] args) {
        if (args.length != 1) return false;
        final Player target = CommandArgCompleter.requirePlayer(args[0]);
        if (target.equals(player)) throw new CommandWarn("You cannot invite yourself");
        final LoadedWorld loadedWorld = plugin.getWorlds().in(player.getWorld());
        if (loadedWorld == null || !loadedWorld.uuid.equals(player.getUniqueId())) {
            throw new CommandWarn("This world does not belong to you!");
        }
        if (plugin.getWorlds().in(target.getWorld()) != null) {
            throw new CommandWarn(target.getName() + " is already playing in a world");
        }
        if (loadedWorld.tag.joined.contains(target.getUniqueId())) {
            throw new CommandWarn(target.getName() + " already joined your world in the past");
        }
        final Session session = plugin.getSessions().get(player.getUniqueId());
        session.invites.add(target.getUniqueId());
        final String cmd = "/skyblock join " + player.getName();
        target.sendMessage(text(player.getName() + " invited you to their Skyblock world. Click here to accept", GREEN)
                           .hoverEvent(showText(text(cmd)))
                           .clickEvent(runCommand(cmd)));
        player.sendMessage(text("Invite sent to " + target.getName(), GREEN));
        return true;
    }

    private boolean join(Player player, String[] args) {
        if (args.length != 1) return false;
        final Player inviter = CommandArgCompleter.requirePlayer(args[0]);
        final Session inviterSession = plugin.getSessions().get(inviter.getUniqueId());
        if (!inviterSession.invites.contains(player.getUniqueId())) {
            throw new CommandWarn(inviter.getName() + " did not invite you!");
        }
        final LoadedWorld loadedWorld = plugin.getWorlds().in(inviter.getWorld());
        if (loadedWorld == null || !loadedWorld.uuid.equals(inviter.getUniqueId())) {
            throw new CommandWarn(inviter.getName() + " cannot invite you there!");
        }
        if (plugin.getWorlds().in(player.getWorld()) != null) {
            throw new CommandWarn("You are already playing in a world!");
        }
        player.teleport(inviter);
        Sessions.resetPlayer(player);
        player.setGameMode(GameMode.SURVIVAL);
        loadedWorld.tag.joined.add(player.getUniqueId());
        loadedWorld.save();
        final Session session = plugin.getSessions().get(player.getUniqueId());
        session.setLocation(loadedWorld, player.getLocation());
        plugin.getSessions().save(session);
        inviterSession.invites.remove(player.getUniqueId());
        player.sendMessage(text("You joined the Skyblock world of " + inviter.getName(), GREEN));
        inviter.sendMessage(text(player.getName() + " joined your world", GREEN));
        return true;
    }

    private void leave(Player player) {
        final LoadedWorld loadedWorld = plugin.getWorlds().in(player.getWorld());
        if (loadedWorld == null) {
            throw new CommandWarn("You are not playing in a world");
        }
        if (loadedWorld.uuid.equals(player.getUniqueId())) {
            throw new CommandWarn("You cannot leave your own world!");
        }
        player.teleport(plugin.getWorlds().getLobbyWorld().getSpawnLocation());
        Sessions.resetPlayer(player);
        player.setGameMode(GameMode.ADVENTURE);
        Session session = plugin.getSessions().get(player.getUniqueId());
        session.clearWorld();
        plugin.getSessions().save(session);
        player.sendMessage(text("You left the Skyblock world of " + PlayerCache.nameForUuid(loadedWorld.uuid) + "!", YELLOW));
    }
}
