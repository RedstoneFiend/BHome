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

// https://bukkit.org/threads/updater-2-3-easy-safe-and-policy-compliant-auto-updating-for-your-plugins-new.96681/

package io.github.redstonefiend.bhome;

import io.github.redstonefiend.bhome.commands.BHome;
import io.github.redstonefiend.bhome.commands.DelHome;
import io.github.redstonefiend.bhome.commands.Home;
import io.github.redstonefiend.bhome.commands.SetHome;
import io.github.redstonefiend.bhome.listeners.PlayerJoin;
import io.github.redstonefiend.bhome.listeners.PlayerQuit;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Chris Courson
 */
public class Main
        extends JavaPlugin
        implements Listener {

    public Map<UUID, Map<String, Location>> homes = new HashMap();
    public final File homesFolder = new File(getDataFolder(), "homes");

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.homesFolder.mkdir();
        for (Player player : getServer().getOnlinePlayers()) {
            homes.put(player.getUniqueId(), loadPlayerHomes(player.getUniqueId(), true));
        }

        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);

        getCommand("bhome").setExecutor(new BHome(this));
        getCommand("sethome").setExecutor(new SetHome(this));
        getCommand("delhome").setExecutor(new DelHome(this));
        getCommand("home").setExecutor(new Home(this));

        getLogger().log(Level.INFO, "BHome version {0} loaded.", getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public Map<String, Location> loadPlayerHomes(UUID playerID, boolean touch) {
        YamlConfiguration homeConfig = new YamlConfiguration();
        try {
            File file = new File(this.homesFolder, playerID.toString() + ".yml");
            if (file.exists()) {
                homeConfig.load(file);
                if (touch) file.setLastModified(new Date().getTime());
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Exception thrown while loading player homes ({0}:{1}):\n{2}",
                    new Object[]{getServer().getOfflinePlayer(playerID).getName(), playerID, ex});
        }

        Map<String, Location> playerHomesMap = new HashMap();
        for (String homeName : homeConfig.getKeys(false)) {
            try {
                playerHomesMap.put(homeName, new Location(
                        getServer().getWorld(homeConfig.getString(homeName + ".world")), homeConfig
                        .getInt(homeName + ".x"), homeConfig
                        .getInt(homeName + ".y"), homeConfig
                        .getInt(homeName + ".z"), homeConfig
                        .getInt(homeName + ".yaw"), homeConfig
                        .getInt(homeName + ".pitch")));
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Unable to load player home ''{0}'' from {1}.yml",
                        new Object[]{homeName, playerID.toString()});
            }
        }
        return playerHomesMap;
    }

    public void printHomes(Player player) {
        Set<String> homesSet = ((Map) this.homes.get(player.getUniqueId())).keySet();
        String homesMax = Integer.toString(getConfig().getInt("max_homes", 5));
        if (player.hasPermission("bhome.unlimited")) {
            homesMax = "Unlimited";
        } else {
            Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
            for (PermissionAttachmentInfo perm : perms) {
                if (perm.getValue()) {
                    String s = perm.getPermission();
                    if (s.startsWith("bhome.max.")) {
                        homesMax = s.substring(s.lastIndexOf('.') + 1);
                    }
                }
            }
        }
        player.sendMessage(String.format(ChatColor.YELLOW + "Homes (%d/%s): %s", new Object[]{
            homesSet.size(), homesMax, homesSet
            .toString().replaceAll("[\\[\\]]", "")}));
    }

    @Override
    public void saveConfig() {
        String str = getConfig().saveToString();
        StringBuilder sb = new StringBuilder(str);

        sb.insert(sb.indexOf("\nversion:") + 1, "\n# Configuration version used during upgrade. Do not change.\n");

        sb.insert(sb.indexOf("\nmax_homes") + 1, "\n# The default maximum number of homes a player may set.\n");

        sb.insert(sb.indexOf("\nteleport_message:") + 1, "\n# Message displayed to player when teleported by home command.\n# Can include color codes (&x) where x is the color number.\n");

        sb.insert(sb.indexOf("\nspawn_height:") + 1, "\n# Specifies the Y offset at the intersection of X + 0.5 and Z + 0.5\n# that player using 'home' command will spawn\n");

        final File cfg_file = new File(getDataFolder(), "config.yml");
        final String cfg_str = sb.toString();
        final Logger logger = getLogger();

        new BukkitRunnable() {
            @Override
            public void run() {
                cfg_file.delete();
                try {
                    PrintWriter writer = new PrintWriter(cfg_file, "UTF-8");
                    Throwable localThrowable3 = null;
                    try {
                        cfg_file.createNewFile();
                        writer.write(cfg_str);
                        writer.close();
                    } catch (Throwable localThrowable1) {
                        localThrowable3 = localThrowable1;
                        throw localThrowable1;
                    } finally {
                        if (localThrowable3 != null) {
                            try {
                                writer.close();
                            } catch (Throwable localThrowable2) {
                                localThrowable3.addSuppressed(localThrowable2);
                            }
                        } else {
                            writer.close();
                        }
                    }
                } catch (IOException ex) {
                    logger.severe("Error saving configuration!");
                }
            }
        }.runTaskAsynchronously(this);
    }
}
