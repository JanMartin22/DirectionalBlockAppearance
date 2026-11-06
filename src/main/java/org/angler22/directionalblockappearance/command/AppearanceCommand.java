package org.angler22.directionalblockappearance.command;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.manager.SelectionManager;
import org.angler22.directionalblockappearance.manager.UndoManager;
import org.angler22.directionalblockappearance.model.*;
import org.angler22.directionalblockappearance.view.ViewService;
import org.angler22.directionalblockappearance.wand.Wand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AppearanceCommand implements CommandExecutor {

    private final AppearanceManager manager;
    private final ViewService viewService;
    private final SelectionManager selectionManager;
    private final Wand wand;
    private final UndoManager undoManager;


    public AppearanceCommand(AppearanceManager manager, ViewService viewService, SelectionManager selectionManager, Wand wand, UndoManager undoManager) {
        this.manager = manager;
        this.viewService = viewService;
        this.selectionManager = selectionManager;
        this.wand = wand;
        this.undoManager = undoManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /dba <wand|pos1|pos2|set|quickset|clear|undo|group>");
            return true;
        }
        if (args[0].equalsIgnoreCase("group")) {
            handleGroup(sender, args);
        } else if (args[0].equalsIgnoreCase("pos1")) {
            handlePosition(sender, args);
        } else if (args[0].equalsIgnoreCase("pos2")) {
            handlePosition(sender, args);
        } else if (args[0].equalsIgnoreCase("quickset")) {
            handleQuickset(sender, args);
        } else if (args[0].equalsIgnoreCase("set")) {
            handleSet(sender, args);
        } else if (args[0].equalsIgnoreCase("wand")) {
            handleWand(sender, args);
        } else if (args[0].equalsIgnoreCase("clear")) {
            handleClear(sender, args);
        } else if (args[0].equalsIgnoreCase("undo")) {
            handleUndo(sender, args);
        } else {
            sender.sendMessage("Unknown subcommand");
        }
        return true;
    }

    private void handleGroup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /dba group <create|delete|list>");
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            if (manager.getGroups().isEmpty()) {
                sender.sendMessage("No groups");
                return;
            }
            for (ViewGroup group : manager.getGroups()) {
                sender.sendMessage(group.getName() + " (" + group.getMode() + ", " + group.getBlocks().size() + " blocks)");
            }
        } else if (args[1].equalsIgnoreCase("create")) {
            handleCreate(sender, args);
        } else if (args[1].equalsIgnoreCase("delete")) {
            handleDelete(sender, args);
        }
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /dba group create <name>");
            return;
        }
        ViewGroup group = manager.createGroup(args[2], ViewMode.TWO_SIDED);
        if (group == null) {
            sender.sendMessage("Group " + args[2] + " already exists");
            return;
        }
        sender.sendMessage("Group " + args[2] + " created");
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /dba group delete <name>");
            return;
        }
        ViewGroup group = manager.deleteGroup(args[2]);
        if (group == null) {
            sender.sendMessage("Group " + args[2] + " doesn't exist");
            return;
        }
        for (BlockKey key : group.getBlocks().keySet()) {
            viewService.restore(key);
        }
        sender.sendMessage("Group " + args[2] + " deleted (" + group.getBlocks().size() + " blocks)");
    }

    private void handlePosition(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players");
            return;
        }

        RayTraceResult hit = player.rayTraceBlocks(25);
        if (hit == null || hit.getHitBlock() == null) {
            player.sendMessage("Look at a block (max. 25 blocks away)");
            return;
        }
        Block block = hit.getHitBlock();
        Selection sel = selectionManager.getSelection(player);

        if (args[0].equalsIgnoreCase("pos1")) {
            sel.setPos1(block);
        } else {
            sel.setPos2(block);
        }
        player.sendMessage(args[0] + " set (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")");
    }

    private void handleQuickset(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players");
            return;
        }
        if (args.length < 3) {
            player.sendMessage("Usage: /dba quickset <group> <block>");
            return;
        }
        ViewGroup group = manager.getGroup(args[1]);
        if (group == null) {
            player.sendMessage("Group " + args[1] + " doesn't exist");
            return;
        }
        Material mat = Material.matchMaterial(args[2]);
        if (mat == null || !mat.isBlock()) {
            player.sendMessage("Unknown block!");
            return;
        }
        RayTraceResult hit = player.rayTraceBlocks(25);
        if (hit == null || hit.getHitBlock() == null || hit.getHitBlockFace() == null) {
            player.sendMessage("Look at a block (max. 25 blocks away)");
            return;
        }
        Block block = hit.getHitBlock();
        BlockFace face = hit.getHitBlockFace();
        BlockBackup before = backup(block);
        SetResult result = applyView(group, block, face, mat.createBlockData());
        if (result == SetResult.ADDED) {
            player.sendMessage("Block added to " + group.getName() + ": " + face + " -> " + mat);
        } else if (result == SetResult.UPDATED) {
            player.sendMessage("View updated: " + face + " -> " + mat);
        } else {
            player.sendMessage("Block belongs to another group");
            return;
        }
        undoManager.push(player, List.of(before));
        for (Player p : Bukkit.getOnlinePlayers()) {
            viewService.update(p);
        }
    }

    private SetResult applyView(ViewGroup group, Block block, BlockFace face, BlockData data) {
        ViewBlock existing = manager.getBlock(new BlockKey(block));
        if (existing == null) {
            manager.addBlock(group, block, face, data);
            return SetResult.ADDED;
        } else if (existing.getGroup() != group) {
            return SetResult.SKIPPED;
        } else {
            manager.replaceView(existing, face, data);
            return SetResult.UPDATED;
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players");
            return;
        }
        if (args.length < 3) {
            player.sendMessage("Usage: /dba set <group> <block> [side]");
            return;
        }
        ViewGroup group = manager.getGroup(args[1]);
        if (group == null) {
            player.sendMessage("Group " + args[1] + " doesn't exist");
            return;
        }
        Material mat = Material.matchMaterial(args[2]);
        if (mat == null || !mat.isBlock()) {
            player.sendMessage("Unknown block!");
            return;
        }

        Selection sel = selectionManager.getSelection(player);
        if (!sel.isComplete()) {
            player.sendMessage("Set pos1 and pos2 first");
            return;
        }
        if (sel.getVolume() > 10000) {
            player.sendMessage("Selection too large (max. 10000 blocks)");
            return;
        }

        BlockFace face;
        if (args.length >= 4) {
            try {
                face = BlockFace.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("Unknown side: " + args[3]);
                return;
            }
        } else {
            face = player.getFacing().getOppositeFace();
        }
        BlockData data = mat.createBlockData();
        int added = 0;
        int updated = 0;
        int skipped = 0;

        List<BlockBackup> change = new ArrayList<>();
        for (Block b : sel.getBlocks()) {
            if (b.getType().isAir()) {
                continue;
            }
            BlockBackup before = backup(b);
            SetResult result = applyView(group, b, face, data);
            if (result == SetResult.ADDED) {
                added++;
                change.add(before);
            } else if (result == SetResult.UPDATED) {
                updated++;
                change.add(before);
            } else {
                skipped++;
            }
        }
        if (!change.isEmpty()) {
            undoManager.push(player, change);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            viewService.update(p);
        }
        player.sendMessage(face + " -> " + mat + ": " + added + " added, " + updated + " updated, " + skipped + " skipped");
    }

    private void handleWand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players");
            return;
        }
        player.getInventory().addItem(wand.createWand());
        player.sendMessage("DBA-Wand added to your Inventory");
    }

    private void handleClear(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players");
            return;
        }
        Selection sel = selectionManager.getSelection(player);
        if (!sel.isComplete()) {
            player.sendMessage("Set pos1 and pos2 first");
            return;
        }
        if (sel.getVolume() > 10000) {
            player.sendMessage("Selection too large (max. 10000 blocks)");
            return;
        }
        List<BlockBackup> change = new ArrayList<>();
        int removed = 0;
        for (Block b : sel.getBlocks()) {
            BlockKey key = new BlockKey(b);
            if (manager.getBlock(key) == null) {
                continue;
            }
            change.add(backup(b));
            manager.removeBlock(key);
            viewService.restore(key);
            removed++;
        }
        if (!change.isEmpty()) {
            undoManager.push(player, change);
        }
        player.sendMessage(removed + " DBA blocks removed");
    }

    private BlockBackup backup(Block block) {
        BlockKey key = new BlockKey(block);
        ViewBlock existing = manager.getBlock(key);
        if (existing == null) {
            return new BlockBackup(key, null, Map.of());
        }
        return new BlockBackup(key, existing.getGroup().getName(), existing.getViews());
    }

    private void handleUndo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players");
            return;
        }
        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Not a number: " + args[1]);
                return;
            }
            if (amount < 1) {
                player.sendMessage("Amount must be at least 1");
                return;
            }
        }
        int undone = 0;
        int blocks = 0;
        for (int i = 0; i < amount; i++) {
            List<BlockBackup> change = undoManager.pop(player);
            if (change == null) {
                break;
            }
            for (BlockBackup b : change) {
                viewService.restore(b.getKey());
                manager.restoreBackup(b);
                blocks++;
            }
            undone++;
        }
        if (undone == 0) {
            player.sendMessage("Nothing to undo");
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            viewService.update(p);
        }
        player.sendMessage(undone + " change(s) undone (" + blocks + " blocks)");
    }
}
