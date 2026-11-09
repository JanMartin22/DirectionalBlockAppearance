package org.angler22.directionalblockappearance.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;

/**
 * Reads all chat messages from the config (messages section) and sends them.
 * <p>
 * Every message starts with the prefix (messages.prefix).
 * Color codes with &amp; are supported, for example &amp;a for green.
 * Placeholders like %group% are replaced with the given values.
 */
public class Messages {
    private final FileConfiguration config;
    private final String prefix;


    /**
     * Creates the message helper and reads the prefix from the config.
     *
     * @param plugin the plugin whose config contains the messages
     */
    public Messages(Plugin plugin) {

        this.config = plugin.getConfig();
        String p = config.getString("messages.prefix");
        this.prefix = color(p != null ? p : "");
    }


    /**
     * Returns a message with prefix, colors and replaced placeholders.
     * <p>
     * Replacements are given as pairs of name and value, for example
     * {@code get("group-created", "group", "windows")} replaces %group% with windows.
     * If the message is missing in the config and in the default config,
     * the key itself is shown, so missing entries are easy to spot.
     *
     * @param key          the name of the message below messages in the config
     * @param replacements pairs of placeholder name and value
     * @return the finished message
     */
    public String get(String key, String... replacements) {
        String text = config.getString("messages." + key);
        if (text == null) {
            text = key;
        }
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace("%" + replacements[i] + "%", replacements[i + 1]);
        }
        return prefix + color(text);
    }

    /**
     * Sends a message to a player or the console.
     *
     * @param sender       who receives the message
     * @param key          the name of the message below messages in the config
     * @param replacements pairs of placeholder name and value, see {@link #get(String, String...)}
     */
    public void send(CommandSender sender, String key, String... replacements) {
        sender.sendMessage(get(key, replacements));
    }

    /**
     * Turns &amp; color codes into Minecraft colors.
     *
     * @param text the text with &amp; codes
     * @return the colored text
     */
    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
