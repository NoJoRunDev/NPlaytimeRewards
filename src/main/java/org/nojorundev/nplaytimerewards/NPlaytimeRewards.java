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
                                                                                                                                                                                                                                                                                             ();
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
            int minimumPlayers = getConfig().getInt(path + "minimum-players", 1);

            rewards.add(new Reward(permissionNode, permission, commands, minimumPlayers));
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

        List<Reward> eligibleRewards = new ArrayList<>(rewards);
        Reward randomReward = null;
        Player randomPlayer = null;

        while (!eligibleRewards.isEmpty()) {
            randomReward = eligibleRewards.get(random.nextInt(eligibleRewards.size()));

            if (onlinePlayers.size() < randomReward.getMinimumPlayers()) {
                eligibleRewards.remove(randomReward);
                continue;
            }

            List<Player> eligiblePlayers = new ArrayList<>(onlinePlayers);
            while (!eligiblePlayers.isEmpty()) {
                randomPlayer = eligiblePlayers.get(random.nextInt(eligiblePlayers.size()));

                if (!randomReward.permissionNode || randomPlayer.hasPermission(randomReward.permission)) {
                    break;
                }

                eligiblePlayers.remove(randomPlayer);
                randomPlayer = null;
            }

            if (randomPlayer != null) {
                break;
            }

            eligibleRewards.remove(randomReward);
            randomReward = null;
        }

        if (randomReward == null || randomPlayer == null) {
            return;
        }

        Integer randomCount = null;

        for (String command : randomReward.commands) {
            String processedCommand = command;

            if (processedCommand.contains("[RANDOM-")) {
                int startIndex = processedCommand.indexOf("[RANDOM-");
                int endIndex = processedCommand.indexOf("]", startIndex);
                if (endIndex != -1) {
                    String randomPart = processedCommand.substring(startIndex + 8, endIndex);
                    String[] parts = randomPart.split("-");
                    if (parts.length == 2) {
                        try {
                            int min = Integer.parseInt(parts[0]);
                            int max = Integer.parseInt(parts[1]);
                            randomCount = new Random().nextInt(max - min + 1) + min;
                            processedCommand = processedCommand.substring(0, startIndex) + randomCount + processedCommand.substring(endIndex + 1);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            processedCommand = processedCommand.replace("{player}", randomPlayer.getName());
            if (randomCount != null) {
                processedCommand = processedCommand.replace("{count}", randomCount.toString());
            }

            if (processedCommand.startsWith("[MESSAGE_ALL]")) {
                String message = processedCommand.substring("[MESSAGE_ALL]".length()).trim();
                Bukkit.broadcastMessage(ChatColorUtil.color(message));
            } else if (processedCommand.startsWith("[MESSAGE]")) {
                String message = processedCommand.substring("[MESSAGE]".length()).trim();
                randomPlayer.sendMessage(ChatColorUtil.color(message));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            }
        }
    }

    private static class Reward {
        boolean permissionNode;
        String permission;
        List<String> commands;
        int minimumPlayers;

        Reward(boolean permissionNode, String permission, List<String> commands, int minimumPlayers) {
            this.permissionNode = permissionNode;
            this.permission = permission;
            this.commands = commands;
            this.minimumPlayers = minimumPlayers;
        }

        public int getMinimumPlayers() {
            return minimumPlayers;
        }
    }
}