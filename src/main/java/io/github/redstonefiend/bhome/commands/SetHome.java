/*
 * The MIT License
 *
 * Copyright 2015 Chris Courson.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.redstonefiend.bhome.commands;

import io.github.redstonefiend.bhome.Main;
import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author chrisbot
 */
public class SetHome implements CommandExecutor {

    private final Main plugin;

    public SetHome(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("sethome").setTabCompleter(new TabComplete(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "ERROR: sethome cannot be called from console.");
            return true;
        }
        final Player player = (Player) sender;
        if (args.length < 1) {
            plugin.printHomes(player);
            return false;
        }
        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Error in command.");
            return false;
        }
        final String homeName = args[0].toLowerCase();
        if (!homeName.matches("[a-zA-Z0-9]+")) {
            player.sendMessage(ChatColor.RED + "Home name can contain only A-Z and 0-9.");
            return true;
        }
        Set<String> homesSet = plugin.homes.get(player.getUniqueId()).keySet();
        int maxHomes = plugin.maxHomes;
        if (player.hasPermission("bhome.unlimited")) {
            maxHomes = Integer.MAX_VALUE;
        } else {
            Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
            for (PermissionAttachmentInfo perm : perms) {
                if (!perm.getValue()) {
                    continue;
                }
                String s = perm.getPermission();
                if (s.startsWith("bhome.max.")) {
                    try {
                        maxHomes = Integer.parseInt(s.substring(s.lastIndexOf('.') + 1));
                    } catch (NumberFormatException ex) {
                        plugin.getLogger().log(Level.SEVERE, "Error in permission {0}", s);
                    }
                }
            }
        }
        if ((homesSet.size() < maxHomes) || (homesSet.contains(homeName))) {
            final Location location = player.getLocation().getBlock().getLocation();
            plugin.homes.get(player.getUniqueId()).put(homeName, location);
            new BukkitRunnable() {

                @Override
                public void run() {
                    YamlConfiguration homeConfig = new YamlConfiguration();
                    try {
                        homeConfig.load(new File(plugin.homesFolder, player.getUniqueId().toString() + ".yml"));
                    } catch (Exception ex) {

                    }
                    homeConfig.set(homeName + ".world", location.getWorld().getName());
                    homeConfig.set(homeName + ".x", location.getBlockX());
                    homeConfig.set(homeName + ".y", location.getBlockY());
                    homeConfig.set(homeName + ".z", location.getBlockZ());
                    homeConfig.set(homeName + ".yaw", (int) location.getYaw());
                    homeConfig.set(homeName + ".pitch", (int) location.getPitch());
                    try {
                        homeConfig.save(new File(plugin.homesFolder, player.getUniqueId().toString() + ".yml"));
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.SEVERE, "Unable to save player home ''{0}'' to {1}.yml",
                                new Object[]{homeName, player.getUniqueId().toString()});
                    }
                }
            }.runTaskLater(plugin, 1);
            player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.ITALIC + homeName + ChatColor.RESET + ChatColor.GREEN + " set.");
        } else {
            player.sendMessage(ChatColor.RED + "Cannot set home. The maximum of " + maxHomes + " has already been set.");
        }
        return true;
    }
}
