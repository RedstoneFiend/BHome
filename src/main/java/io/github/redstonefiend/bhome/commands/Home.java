/*
 * The MIT License
 *
 * Copyright 2016 Chris Courson.
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
 * @author Chris Courson
 */
public class Home implements CommandExecutor {

    private final Main plugin;

    public Home(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("home").setTabCompleter(new PlayerTabComplete(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "ERROR: 'home' cannot be called from console.");
            return true;
        }
        Player player = (Player) sender;
        if ((args.length < 1) && (plugin.homes.get(player.getUniqueId()).size() > 1)) {
            this.plugin.printHomes(player);
            return false;
        }
        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Error in command.");
            return false;
        }
        String homeName;
        if (args.length == 1) {
            homeName = args[0].toLowerCase();
        } else {
            homeName = plugin.homes.get(player.getUniqueId()).keySet().iterator().next();
        }
        if ((this.plugin.homes.get(player.getUniqueId())).containsKey(homeName)) {
            Location location = ((Location) (this.plugin.homes.get(player.getUniqueId())).get(homeName)).clone().add(0.5D, plugin.getConfig().getDouble("spawn_height", 0.5D), 0.5D);
            if (location != null) {
                if (player.getVehicle() != null) {
                    Entity entity = player.getVehicle();
                    entity.eject();
                    entity.teleport(location);
                    player.teleport(location);
                    entity.setPassenger(player);
                } else {
                    player.setFallDistance(0.0F);
                    player.teleport(location);
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("teleport_message", "")));
            } else {
                this.plugin.printHomes(player);
            }
        } else {
            player.sendMessage(ChatColor.RED + "Home '" + homeName + "' not found.");
        }
        return true;
    }
}
