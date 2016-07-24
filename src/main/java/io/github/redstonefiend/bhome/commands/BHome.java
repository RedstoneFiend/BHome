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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Chris Courson
 */
public class BHome
        implements CommandExecutor {

    private final Main plugin;

    public BHome(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((args.length == 0) || ((args.length == 1) && ((args[0].equalsIgnoreCase("ver")) || (args[0].equalsIgnoreCase("version"))))) {
            sender.sendMessage(ChatColor.GOLD + "Boomerang Home " + ChatColor.GREEN + "version " + ChatColor.YELLOW + this.plugin.getDescription().getVersion());
        } else if ((args.length == 1) && (args[0].equalsIgnoreCase("reload"))) {
            this.plugin.reloadConfig();
            sender.sendMessage(ChatColor.YELLOW + "BHome config reloaded.");
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
}
