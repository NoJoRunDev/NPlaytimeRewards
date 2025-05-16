package org.nojorundev.nplaytimerewards.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nojorundev.nplaytimerewards.NPlaytimeRewards;
import org.nojorundev.nplaytimerewards.utils.ChatColorUtil;

import java.util.ArrayList;
import java.util.List;

public class CommandSchedule implements CommandExecutor {

    private final NPlaytimeRewards plugin;

    public CommandSchedule(NPlaytimeRewards plugin) {
        this.plugin = plugin;
        plugin.getCommand("nprewards").setTabCompleter((sender, command, alias, args) -> {
            if (!sender.hasPermission("nplaytimerewards.admin")) {
                return null;
            }
            List<String> completions = new ArrayList<>();
            if (args.length == 1 && sender instanceof Player) {
                completions.add("schedule");
                completions.add("reload");
                completions.add("help");
            }
            return completions;
        });
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        if (!commandSender.hasPermission("nplaytimerewards.admin")) {
            commandSender.sendMessage(ChatColorUtil.color(plugin.getConfig().getString("no-permission")));
            return false;
        }

        if (args.length == 0) {
            SendHelp(commandSender);
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "schedule":
                plugin.giveRandomReward();
                commandSender.sendMessage(ChatColorUtil.color(plugin.getConfig().getString("task-schedule")));
                return true;
            case "reload":
                plugin.reloadConfig();
                plugin.loadConfig();
                commandSender.sendMessage(ChatColorUtil.color(plugin.getConfig().getString("reload-config")));
                return true;
            case "help":
                SendHelp(commandSender);
                return true;
            default:
                SendHelp(commandSender);
                return false;
        }
    }

    private boolean SendHelp(CommandSender commandSender) {
        if (!commandSender.hasPermission("nplaytimerewards.admin")) {
            commandSender.sendMessage(ChatColorUtil.color(plugin.getConfig().getString("no-permission")));
            return false;
        }
        commandSender.sendMessage(ChatColorUtil.color("&d&lNPlaytimeRewards &7by &dNoJoRunDev&r"));
        commandSender.sendMessage(ChatColorUtil.color(plugin.getConfig().getString("help")));
        return true;
    }
}
