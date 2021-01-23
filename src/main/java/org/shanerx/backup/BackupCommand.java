package org.shanerx.backup;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class BackupCommand implements CommandExecutor {

    private AutoBackup plugin;

    BackupCommand(AutoBackup plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Message.INFO.toString());
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(Message.INFO.toString());
                if (sender.hasPermission("autobackup.help")) {
                    sender.sendMessage(Message.HELP.toString());
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("autobackup.reload")) {
                    sender.sendMessage(Message.NO_PERMISSION.toString());
                    return true;
                }

                plugin.getServer().getScheduler().cancelTasks(plugin);
                plugin.reloadConfig();
                plugin.loadSettings();

                sender.sendMessage(Message.RELOAD.toString());
                //sender.sendMessage(Message.INVALID_CONFIG.toString());
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                if (!sender.hasPermission("autobackup.list")) {
                    sender.sendMessage(Message.NO_PERMISSION.toString());
                    return true;
                }

                if (plugin.getBackups().size() == 0) {
                    sender.sendMessage(Message.NO_BACKUPS.toString());
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                for (BackupMode mode : plugin.getBackups()) {
                    if (sender.hasPermission("autobackup.backup." + mode.getName().toLowerCase()) && mode.isAllowedManually() ||
                            sender == plugin.getServer().getConsoleSender() && plugin.getConfig().getBoolean("allow-force-console")) {
                        sb.append(mode.getName()).append("  ");
                    }
                }

                if (sb.length() > 0) {
                    sender.sendMessage(Message.MODES_ACCESS.toString());
                    sender.sendMessage(sb.toString());
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("default")) {
                if (!sender.hasPermission("autobackup.default")) {
                    sender.sendMessage(Message.NO_PERMISSION.toString());
                    return true;
                }

                else if (plugin.getDefaultBackups().size() == 0) {
                    sender.sendMessage(Message.NO_DEFAULT.toString());
                    return true;
                }

                else if (plugin.getConfig().getBoolean("log-to-console")) {
                    plugin.getServer().getConsoleSender().sendMessage(Message.MANUAL_BACKUP_LOG.toConsoleString()
                                    .replaceAll("%NAME%", sender.getName())
                                    .replaceAll("%MODE%", "(default)"));
                }

                StringBuilder sb = new StringBuilder();
                for (BackupMode mode : plugin.getDefaultBackups()) {
                    if (plugin.performBackup(mode, true)) {
                        sb.append(mode.getName()).append("  ");
                        continue;
                    }
                    if (sender instanceof Player) sender.sendMessage(Message.BACKUP_FAILED.toString() + mode.getName());
                    plugin.getServer().getConsoleSender().sendMessage(Message.BACKUP_FAILED.toConsoleString() + mode.getName());
                }
                if (sb.length() > 0) {
                    if (sender instanceof Player) sender.sendMessage(Message.BACKUP_PERFORMING.toString() + sb.toString());
                    if (plugin.getConfig().getBoolean("log-to-console")) {
                        plugin.getServer().getConsoleSender().sendMessage(Message.BACKUP_PERFORMING.toConsoleString() + sb.toString());
                    }
                }
                return true;
            }
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("take")) {
                if (!sender.hasPermission("autobackup.backup." + args[1].toLowerCase())) {
                    sender.sendMessage(Message.NO_PERMISSION.toString());
                    return true;
                }

                for (BackupMode mode : plugin.getBackups()) {
                    if (mode.getName().equalsIgnoreCase(args[1])) {
                        if (plugin.getConfig().getBoolean("log-to-console")) {
                            plugin.getServer().getConsoleSender().sendMessage(Message.MANUAL_BACKUP_LOG.toConsoleString()
                                            .replaceAll("%NAME%", sender.getName())
                                            .replaceAll("%MODE%", mode.getName()));
                        }

                        if (!(sender == plugin.getServer().getConsoleSender()
                                && plugin.getConfig().getBoolean("allow-force-console")
                                || mode.isAllowedManually())) {
                            sender.sendMessage(Message.NO_PERMISSION.toConsoleString());
                            return true;
                        }

                        if (plugin.performBackup(mode, true)) {
                            if (sender instanceof Player) sender.sendMessage(Message.BACKUP_PERFORMING.toString() + mode.getName());
                            if (plugin.getConfig().getBoolean("log-to-console")) {
                                plugin.getServer().getConsoleSender().sendMessage(Message.BACKUP_PERFORMING.toConsoleString() + mode.getName());
                            }
                            return true;
                        }

                        // always log failure to console, regardless of setting
                        if (sender instanceof Player) sender.sendMessage(Message.BACKUP_FAILED.toString() + mode.getName());
                        plugin.getServer().getConsoleSender().sendMessage(Message.BACKUP_FAILED.toConsoleString() + mode.getName());
                        return true;
                    }
                }

                sender.sendMessage(Message.INVALID_MODE.toString());
                return true;
            }
        }

        sender.sendMessage(Message.INVALID_USAGE.toString());
        return true;
    }
}
