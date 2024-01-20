package org.welcomejoin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public final class WelcomeJoin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private List<String> motdLines;
    private BossBarManager bossBarManager;


    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("motd").setExecutor(this);
        getCommand("motd").setTabCompleter(this);

        // Load or create config.yml
        saveDefaultConfig();
        loadConfig();
        bossBarManager = new BossBarManager(getConfig());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        bossBarManager.showBossBar(player);

        // Use the obtained player name in the welcome message
        for (String line : motdLines) {
            String formattedLine = ChatColor.translateAlternateColorCodes('&', line.replace("%player%", playerName));
            player.sendMessage(formattedLine);
        }
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        motdLines = config.getStringList("welcome-messages");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendMotdList(sender);
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "edit":
                handleEditMotd(sender, args);
                break;
            case "remove":
                handleRemoveMotd(sender, args);
                break;
            case "new":
                handleNewMotd(sender, args);
                break;
            case "reload":
                handleReloadMotd(sender);
                break;
            default:
                sender.sendMessage(getMessage("invalid-command-usage"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("edit", "remove", "new", "reload"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("remove")) {
                for (int i = 1; i <= motdLines.size(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
        }

        return completions;
    }

    private void sendMotdList(CommandSender sender) {
        for (int i = 0; i < motdLines.size(); i++) {
            sender.sendMessage(getMessage("motd-list")
                    .replace("%line%", String.valueOf(i + 1))
                    .replace("%message%", motdLines.get(i)));
        }
    }

    private void handleEditMotd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(getMessage("invalid-command-usage"));
            return;
        }

        try {
            int lineNumber = Integer.parseInt(args[1]) - 1;
            if (lineNumber < 0 || lineNumber >= motdLines.size()) {
                sender.sendMessage(getMessage("invalid-line-number"));
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                messageBuilder.append(args[i]).append(" ");
            }
            String newMessage = messageBuilder.toString().trim();

            // Otomatik olarak tırnak içine al
            if (!newMessage.startsWith("\"") || !newMessage.endsWith("\"")) {
                newMessage = "\"" + newMessage + "\"";
            }

            motdLines.set(lineNumber, newMessage);
            saveMotdConfig();
            sender.sendMessage(getMessage("motd-edited").replace("%line%", String.valueOf(lineNumber + 1)));
        } catch (NumberFormatException e) {
            sender.sendMessage(getMessage("invalid-line-number"));
        }
    }

    private void handleRemoveMotd(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(getMessage("invalid-command-usage"));
            return;
        }

        try {
            int lineNumber = Integer.parseInt(args[1]) - 1;
            if (lineNumber < 0 || lineNumber >= motdLines.size()) {
                sender.sendMessage(getMessage("invalid-line-number"));
                return;
            }

            motdLines.remove(lineNumber);
            saveMotdConfig();
            sender.sendMessage(getMessage("motd-removed").replace("%line%", String.valueOf(lineNumber + 1)));
        } catch (NumberFormatException e) {
            sender.sendMessage(getMessage("invalid-line-number"));
        }
    }

    private void handleNewMotd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getMessage("invalid-command-usage"));
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String newMessage = messageBuilder.toString().trim();

        // Sadece tek tırnak içinde config'e kaydet
        newMessage = "'" + newMessage + "'";

        motdLines.add(newMessage);
        saveMotdConfig();
        sender.sendMessage(getMessage("motd-added"));
    }


    private void handleReloadMotd(CommandSender sender) {
        reloadConfig();
        loadConfig();
        sender.sendMessage(getMessage("motd-reloaded"));
    }

    private String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages." + key));
    }

    private void saveMotdConfig() {
        FileConfiguration config = getConfig();
        config.set("welcome-messages", motdLines);
        saveConfig();
    }
}