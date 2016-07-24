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
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 *
 * @author Chris Courson
 */
public class BHome implements CommandExecutor {

    private final Main plugin;

    public BHome(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("bhome").setTabCompleter(new BHomeTabComplete(plugin, this));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((args.length == 0) || ((args.length == 1) && ((args[0].equalsIgnoreCase("ver")) || (args[0].equalsIgnoreCase("version"))))) {

            sender.sendMessage(ChatColor.GOLD + "Boomerang Home " + ChatColor.GREEN + "version " + ChatColor.YELLOW + this.plugin.getDescription().getVersion());

        } else if ((args.length == 1) && (args[0].equalsIgnoreCase("reload"))) {

            this.plugin.reloadConfig();
            sender.sendMessage(ChatColor.YELLOW + "BHome config reloaded.");

        } else if ((args.length >= 2) && (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("tp"))) {

            UUID playerID = getPlayerId(args[1]);
            if (playerID == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found.");
                return true;
            }

            Map<String, Location> homes = plugin.loadPlayerHomes(playerID, false);

            if (args.length == 2) {
                if (homes.isEmpty()) {
                    sender.sendMessage(String.format(ChatColor.GOLD + "Player '%s' has no homes set.",
                            plugin.getServer().getOfflinePlayer(playerID).getName()));
                } else {
                    sender.sendMessage(String.format(ChatColor.GOLD + "Homes for '%s' (%d): %s", new Object[]{
                        plugin.getServer().getOfflinePlayer(playerID).getName(), homes.size(), homes.keySet().toString().replaceAll("[\\[\\]]", "")}));
                }
            } else {
                if (homes.containsKey(args[2].toLowerCase())) {

                    Location location = ((Location) (this.plugin.homes.get(playerID).get(args[2].toLowerCase()))).clone();
                    location.add(location.getX() > 0.0D ? 0.5D : -0.5D, plugin.getConfig().getDouble("spawn_height", 0.5D), location.getZ() > 0.0D ? 0.5D : -0.5D);
                    if (args[0].equalsIgnoreCase("show")) {
                        sender.sendMessage(String.format(ChatColor.GOLD + "Home '%s' for '%s' is located at:\n  X(%d) Y(%d) Z(%d)", new Object[]{
                            args[2].toLowerCase(), plugin.getServer().getOfflinePlayer(playerID).getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()}));
                    } else {
                        Player callingPlayer = (Player) sender;
                        if (callingPlayer.getVehicle() != null) {
                            Entity entity = callingPlayer.getVehicle();
                            entity.eject();
                            entity.teleport(location);
                            callingPlayer.teleport(location);
                            entity.setPassenger(callingPlayer);
                        } else {
                            callingPlayer.setFallDistance(0.0F);
                            callingPlayer.teleport(location);
                        }
                        callingPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("teleport_message", "")));
                    }
                } else {
                    sender.sendMessage(String.format(ChatColor.RED + "Home '%s' for '%s' not found.", new Object[]{
                        args[2].toLowerCase(), plugin.getServer().getOfflinePlayer(playerID).getName()}));
                }
            }

        } else if ((args.length == 2) && (args[0].equalsIgnoreCase("max"))) {

            int max;
            try {
                max = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + ex.getMessage().substring(18).replace("\"", "'") + " is not a number");
                return true;
            }
            this.plugin.getConfig().set("max_homes", max);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Max homes saved.");

        } else if ((args.length == 1) && (args[0].equalsIgnoreCase("max"))) {

            sender.sendMessage(ChatColor.GREEN + "Max homes = " + Integer.toHexString(this.plugin.getConfig().getInt("max_homes", 5)) + ".");

        } else {
            return false;
        }
        if (args.length == 0) {
            return false;
        } else {
            return true;
        }
    }

    UUID getPlayerId(String playerName) {
        UUID playerID = null;
        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            if (offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                playerID = offlinePlayer.getUniqueId();
            }
        }
        return playerID;
    }
}
