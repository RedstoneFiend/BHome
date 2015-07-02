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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 *
 * @author chrisbot
 */
public class Home implements CommandExecutor {

    private final Main plugin;

    public Home(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("home").setTabCompleter(new TabComplete(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "ERROR: home cannot be called from console.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            plugin.printHomes(player);
            return false;
        }
        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Error in command.");
            return false;
        }
        String homeName = args[0].toLowerCase();
        if (plugin.homes.get(player.getUniqueId()).containsKey(homeName)) {
            Location location = plugin.homes.get(player.getUniqueId()).get(homeName).add(0.5, 0, 0.5);
            if (location != null) {
                if (player.getVehicle() != null) {
                    Entity entity = player.getVehicle();
                    entity.eject();
                    entity.teleport(location);
                    player.teleport(location);
                    entity.setPassenger(player);
                } else {
                    player.setFallDistance(0);
                    player.teleport(location);
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.teleportMessage));
            } else {
                plugin.printHomes(player);
            }
        }
        return true;
    }
}
