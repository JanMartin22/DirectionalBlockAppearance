package org.angler22.directionalblockappearance.command;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.model.ViewBlock;
import org.angler22.directionalblockappearance.model.ViewGroup;
import org.angler22.directionalblockappearance.model.ViewMode;
import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;


public class AppearanceCommand implements CommandExecutor {

    private final AppearanceManager manager;
    private final ViewService viewService;


    public AppearanceCommand(AppearanceManager manager, ViewService viewService) {
        this.manager = manager;
        this.viewService = viewService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /dba <test|group>");
            return true;
        }
        if (args[0].equalsIgnoreCase("test")) {
            handleTest(sender);
        } else if (args[0].equalsIgnoreCase("group")) {
            handleGroup(sender, args);
        } else {
            sender.sendMessage("Unknown subcommand");
        }
        return true;
    }

    private void handleTest(CommandSender sender) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players");
            return;
        }

        RayTraceResult hit = player.rayTraceBlocks(5);
        if (hit == null || hit.getHitBlock() == null || hit.getHitBlockFace() == null) {
            player.sendMessage("Look at a block (max. 5 blocks away)");
            return;
        }
        Block block = hit.getHitBlock();
        BlockFace face = hit.getHitBlockFace();

        ViewGroup group = manager.getGroup("test");
        if (group == null) {
            group = manager.createGroup("test", ViewMode.TWO_SIDED);
        }

        ViewBlock vb = manager.addBlock(group, block, face, Material.GLASS.createBlockData());
        if (vb == null) {
            player.sendMessage("This block is already a DBA block");
            return;
        }
        viewService.update(player);
        player.sendMessage("Test block created! Glass side: " + face);

    }
    private void handleGroup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /dba group <create|delete|list>");
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            if(manager.getGroups().isEmpty()){
                sender.sendMessage("No groups");
                return;
            }
            for(ViewGroup group: manager.getGroups()){
                sender.sendMessage(group.getName() + " (" + group.getMode() + ", " + group.getBlocks().size() + " blocks)");
            }
        }
    }
}