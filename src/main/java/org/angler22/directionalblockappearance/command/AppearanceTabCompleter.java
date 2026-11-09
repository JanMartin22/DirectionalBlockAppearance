package org.angler22.directionalblockappearance.command;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.model.ViewGroup;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab completion for /dba.
 * <p>
 * Suggests subcommands, group actions, existing group names,
 * block types and sides, depending on the argument position.
 * Only suggestions that start with what the player already typed are shown.
 */
public class AppearanceTabCompleter implements TabCompleter {

    private final AppearanceManager manager;

    /**
     * Creates the tab completer.
     *
     * @param manager used to suggest the names of existing groups
     */
    public AppearanceTabCompleter(AppearanceManager manager) {
        this.manager = manager;
    }

    /**
     * Returns the suggestions for the argument the player is typing.
     * <ul>
     *   <li>1st argument: all subcommands</li>
     *   <li>group: create, delete, list; after delete: group names</li>
     *   <li>set / quickset: group names, then block types</li>
     *   <li>set: the side as 4th argument</li>
     * </ul>
     *
     * @param sender  who is typing the command
     * @param command the command
     * @param alias   the alias that was used
     * @param args    the arguments typed so far
     * @return the matching suggestions, empty if there are none
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            results.add("wand");
            results.add("group");
            results.add("set");
            results.add("quickset");
            results.add("clear");
            results.add("undo");
            results.add("pos1");
            results.add("pos2");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("group")) {
            results.add("create");
            results.add("delete");
            results.add("list");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("quickset"))) {
            for (ViewGroup group : manager.getGroups()) {
                results.add(group.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("group") && args[1].equalsIgnoreCase("delete")) {
            for (ViewGroup group : manager.getGroups()) {
                results.add(group.getName());
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("quickset"))) {
            for (Material m : Material.values()) {
                if (m.isBlock() && !m.name().startsWith("LEGACY_")) {
                    results.add(m.name().toLowerCase());
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            results.add("north");
            results.add("south");
            results.add("east");
            results.add("west");
            results.add("up");
            results.add("down");
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], results, new ArrayList<>());
    }
}
