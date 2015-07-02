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
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author chrisbot
 */
public class DelHome implements CommandExecutor {

    private final Main plugin;

    public DelHome(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("delhome").setTabCompleter(new TabComplete(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "ERROR: delhome cannot be called from console.");
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
        if (plugin.homes.get(player.getUniqueId()).containsKey(homeName)) {
            plugin.homes.get(player.getUniqueId()).remove(homeName);
            new BukkitRunnable() {

                @Override
                public void run() {
                    YamlConfiguration homeConfig = new YamlConfiguration();
                    try {
                        homeConfig.load(new File(plugin.homesFolder, player.getUniqueId().toString() + ".yml"));
                    } catch (Exception ex) {

                    }
                    try {
                        homeConfig.set(homeName, null);
                        homeConfig.save(new File(plugin.homesFolder, player.getUniqueId().toString() + ".yml"));
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.SEVERE, "Unable to delete player home ''{0}'' in {1}.yml",
                                new Object[]{homeName, player.getUniqueId().toString()});
                    }
                }
            }.runTaskLater(plugin, 1);
            player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.ITALIC + homeName + ChatColor.RESET + ChatColor.GREEN + " has been deleted.");
        } else {
            plugin.printHomes(player);
        }
        return true;
    }
}
