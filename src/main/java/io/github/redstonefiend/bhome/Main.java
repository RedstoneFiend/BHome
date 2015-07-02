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
package io.github.redstonefiend.bhome;

import io.github.redstonefiend.bhome.commands.DelHome;
import io.github.redstonefiend.bhome.commands.Home;
import io.github.redstonefiend.bhome.commands.SetHome;
import io.github.redstonefiend.bhome.listeners.PlayerJoin;
import io.github.redstonefiend.bhome.listeners.PlayerQuit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
 * @author Chris
 */
public class Main extends JavaPlugin implements Listener {

    public int maxHomes = 0;
    public String teleportMessage = "";
    public Map<UUID, Map<String, Location>> homes = new HashMap<>();
    public final File homesFolder = new File(this.getDataFolder(), "homes");

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.maxHomes = this.getConfig().getInt("max_homes");
        this.teleportMessage = this.getConfig().getString("teleport_message");

        if (!homesFolder.exists()) {
            homesFolder.mkdir();
        }

        for (Player player : this.getServer().getOnlinePlayers()) {
            loadPlayerHomes(player);
        }

        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);

        getCommand("bhome").setExecutor(new SetHome(this));
        getCommand("sethome").setExecutor(new SetHome(this));
        getCommand("delhome").setExecutor(new DelHome(this));
        getCommand("home").setExecutor(new Home(this));

        this.getLogger().log(Level.INFO, "BHome version {0} loaded.", this.getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public void loadPlayerHomes(Player player) {
        YamlConfiguration homeConfig = new YamlConfiguration();
        try {
            homeConfig.load(new File(homesFolder, player.getUniqueId().toString() + ".yml"));
        } catch (Exception ex) {

        }
        Map<String, Location> playerHomesMap = new HashMap<>();
        for (String homeName : homeConfig.getKeys(false)) {
            try {
                playerHomesMap.put(homeName, new Location(
                        this.getServer().getWorld(homeConfig.getString(homeName + ".world")),
                        homeConfig.getInt(homeName + ".x"),
                        homeConfig.getInt(homeName + ".y"),
                        homeConfig.getInt(homeName + ".z"),
                        homeConfig.getInt(homeName + ".yaw"),
                        homeConfig.getInt(homeName + ".pitch")
                ));
            } catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "Unable to load player home ''{0}'' from {1}.yml",
                        new Object[]{homeName, player.getUniqueId().toString()});
            }
        }
        this.homes.put(player.getUniqueId(), playerHomesMap);
    }

    public void printHomes(Player player) {
        Set<String> homesSet = this.homes.get(player.getUniqueId()).keySet();
        String homesMax = Integer.toString(this.maxHomes);
        if (player.hasPermission("bhome.unlimited")) {
            homesMax = "Unlimited";
        } else {
            Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
            for (PermissionAttachmentInfo perm : perms) {
                if (!perm.getValue()) {
                    continue;
                }
                String s = perm.getPermission();
                if (s.startsWith("bhome.max.")) {
                    homesMax = s.substring(s.lastIndexOf('.') + 1);
                }
            }
        }
        player.sendMessage(String.format(ChatColor.YELLOW + "Homes (%d/%s): %s",
                homesSet.size(),
                homesMax,
                homesSet.toString().replaceAll("[\\[\\]]", "")));
    }

    @Override
    public void saveConfig() {
        String header
                = "################################\n"
                + "# Boomerang Home Configuration #\n"
                + "################################\n\n"
                + "# Message string should be quoted using single quotes (').\n"
                + "# Message string can include color formating codes:\n"
                + "#   &x - color code where x is the color number as defined at\n"
                + "#        http://minecraft.gamepedia.com/Formatting_codes.";

        String str = this.getConfig().saveToString();
        StringBuilder sb = new StringBuilder(str);
        sb.replace(0, sb.indexOf("\n"), header);

        sb.insert(sb.indexOf("\nversion:") + 1,
                "\n# Configuration version used during upgrade. Do not change.\n");

        sb.insert(sb.indexOf("\nmax_homes") + 1,
                "\n# The default maximum number of homes a player may set.\n");

        sb.insert(sb.indexOf("\nteleport_message:") + 1,
                "\n# Message displayed to player when teleported by home command.\n"
                + "# Can include color codes (&x) where x is the color number.\n");

        final File cfg_file = new File(this.getDataFolder(), "config.yml");
        final String cfg_str = sb.toString();
        final Logger logger = this.getLogger();

        new BukkitRunnable() {
            @Override
            public void run() {
                cfg_file.delete();
                try (PrintWriter writer = new PrintWriter(cfg_file, "UTF-8")) {
                    writer.write(cfg_str);
                    writer.close();
                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    logger.severe("Error saving configuration!");
                }
            }
        }.runTaskLater(this, 1);
    }
}
