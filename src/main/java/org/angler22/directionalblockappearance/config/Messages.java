package org.angler22.directionalblockappearance.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;

public class Messages {
    private final FileConfiguration config;
    private final String prefix;

    public Messages(Plugin plugin) {

        this.config = plugin.getConfig();
        this.prefix = color(config.getString("messages.prefix", "")) ;
    }

    public String get(String key, String... replacements) {
        String text = config.getString("messages." + key, key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace("%" + replacements[i] + "%", replacements[i + 1]);
        }
        return prefix + color(text);
    }

    public void send(CommandSender sender, String key, String... replacements) {
        sender.sendMessage(get(key, replacements));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
