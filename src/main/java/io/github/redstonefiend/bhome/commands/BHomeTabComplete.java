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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 *
 * @author Chris Courson
 */
public class BHomeTabComplete implements TabCompleter {

    Main plugin;
    BHome bhome;

    public BHomeTabComplete(Main plugin, BHome bhome) {
        this.plugin = plugin;
        this.bhome = bhome;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        List<String> list = new ArrayList();
        switch (args.length) {
            case 1:
                list.add("version");
                list.add("reload");
                list.add("max");
                list.add("show");
                list.add("tp");
                break;
            case 2:
                if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("tp")) {
                    for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
                        list.add(offlinePlayer.getName());
                        Collections.sort(list);
                    }
                }
                break;
            case 3:
                if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("tp")) {
                    UUID playerID = bhome.getPlayerId(args[1]);
                    if (playerID != null) {
                        for (String home : plugin.loadPlayerHomes(playerID, false).keySet()) {
                            list.add(home);
                        }
                    }
                }
        }
        return list;
    }
}
