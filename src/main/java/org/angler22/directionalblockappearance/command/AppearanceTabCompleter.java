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
 * Only suggestions that start with what the player already typed are shown
 * * and only for commands the sender has the permission for.
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
     *   <li>1st argument: all subcommands the sender may use</li>
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
            if(sender.hasPermission("dba.command.wand")){
                results.add("wand");
            }
            if(sender.hasPermission("dba.command.group.list") || sender.hasPermission("dba.command.group.create") || sender.hasPermission("dba.command.group.delete")){
                results.add("group");
            }
            if(sender.hasPermission("dba.command.set")){
                results.add("set");
            }
            if(sender.hasPermission("dba.command.quickset")){
                results.add("quickset");
            }
            if(sender.hasPermission("dba.command.clear")){
                results.add("clear");
            }
            if(sender.hasPermission("dba.command.undo")){
                results.add("undo");
            }
            if(sender.hasPermission("dba.command.pos")){
                results.add("pos1");
            }
            if(sender.hasPermission("dba.command.pos")){
                results.add("pos2");
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("group")) {
            if(sender.hasPermission("dba.command.group.create")){
                results.add("create");
            }
            if(sender.hasPermission("dba.command.group.delete")){
                results.add("delete");
            }
            if(sender.hasPermission("dba.command.group.list")){
                results.add("list");
            }

        } else if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("quickset"))) {
            if(sender.hasPermission("dba.command." + args[0].toLowerCase())){
                for (ViewGroup group : manager.getGroups()) {
                    results.add(group.getName());
                }
            }

        } else if (args.length == 3 && args[0].equalsIgnoreCase("group") && args[1].equalsIgnoreCase("delete")) {
           if(sender.hasPermission("dba.command.group.delete")){
               for (ViewGroup group : manager.getGroups()) {
                   results.add(group.getName());
               }
           }

        } else if (args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("quickset"))) {
            if(sender.hasPermission("dba.command." + args[0].toLowerCase())){
                for (Material m : Material.values()) {
                    if (m.isBlock() && !m.name().startsWith("LEGACY_")) {
                        results.add(m.name().toLowerCase());
                    }
                }
            }

        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            if(sender.hasPermission("dba.command.set")){
                results.add("north");
                results.add("south");
                results.add("east");
                results.add("west");
                results.add("up");
                results.add("down");
            }
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], results, new ArrayList<>());
    }
}
