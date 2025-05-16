package org.nojorundev.nplaytimerewards;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.nojorundev.nplaytimerewards.commands.CommandSchedule;
import org.nojorundev.nplaytimerewards.utils.ChatColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NPlaytimeRewards extends JavaPlugin {

    private int time;
    private List<Reward> rewards;
    private Random random;
    private int taskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        random = new Random();

        getCommand("nprewards").setExecutor(new CommandSchedule(this));
    }

    public void loadConfig() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        time = getConfig().getInt("time", 600);
        rewards = new ArrayList<>();

        for (String rewardKey : getConfig().getConfigurationSection("rewards").getKeys(false)) {
            String path = "rewards." + rewardKey + ".";

            boolean permissionNode = getConfig().getBoolean(path + "permission-node", false);
            String permission = getConfig().getString(path + "permission", "");
            List<String> commands = getConfig().getStringList(path + "commands");

            rewards.add(new Reward(permissionNode, permission, commands));
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this,
                this::giveRandomReward,
                time * 20L,
                time * 20L
        );
    }

    public void giveRandomReward() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) return;

        List<Player> eligiblePlayers = new ArrayList<>(onlinePlayers);
        Reward randomReward = rewards.get(random.nextInt(rewards.size()));

        Player randomPlayer = null;
        while (!eligiblePlayers.isEmpty()) {
            randomPlayer = eligiblePlayers.get(random.nextInt(eligiblePlayers.size()));

            if (!randomReward.permissionNode || randomPlayer.hasPermission(randomReward.permission)) {
                break;
            }

            eligiblePlayers.remove(randomPlayer);
            randomPlayer = null;
        }

        if (randomPlayer == null) return;

        for (String command : randomReward.commands) {
            if (command.startsWith("[MESSAGE_ALL]")) {
                String message = command.substring("[MESSAGE_ALL]".length()).trim();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColorUtil.color(message.replace("{player}", randomPlayer.getName())));
                }
            } else if (command.startsWith("[MESSAGE]")) {
                String message = command.substring("[MESSAGE]".length()).trim();
                randomPlayer.sendMessage(ChatColorUtil.color(message.replace("{player}", randomPlayer.getName())));
            } else {
                String processedCommand = command.replace("{player}", randomPlayer.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            }
        }
    }

    private static class Reward {
        boolean permissionNode;
        String permission;
        List<String> commands;

        Reward(boolean permissionNode, String permission, List<String> commands) {
            this.permissionNode = permissionNode;
            this.permission = permission;
            this.commands = commands;
        }
    }
}