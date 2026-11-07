package org.angler22.directionalblockappearance.command;

import org.angler22.directionalblockappearance.config.Messages;
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
    private final int selectionRange;
    private final int maxBlocks;
    private final Messages messages;


    public AppearanceCommand(AppearanceManager manager, ViewService viewService, SelectionManager selectionManager, Wand wand, UndoManager undoManager, Messages messages,  int selectionRange, int maxBlocks) {
        this.manager = manager;
        this.viewService = viewService;
        this.selectionManager = selectionManager;
        this.wand = wand;
        this.undoManager = undoManager;
        this.selectionRange = selectionRange;
        this.maxBlocks = maxBlocks;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            messages.send(sender, "usage");
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
            messages.send(sender, "unknown-subcommand");
        }
        return true;
    }

    private void handleGroup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.send(sender, "usage-group");
            return;
        }
        if (args[1].equalsIgnoreCase("list")) {
            if (noPermission(sender, "dba.command.group.list")) {
                return;
            }
            if (manager.getGroups().isEmpty()) {
                messages.send(sender, "group-list-empty");
                return;
            }
            for (ViewGroup group : manager.getGroups()) {
                messages.send(sender, "group-list-entry", "group", group.getName(), "mode", group.getMode().name(), "blocks", String.valueOf(group.getBlocks().size()));
            }
        } else if (args[1].equalsIgnoreCase("create")) {
            handleCreate(sender, args);
        } else if (args[1].equalsIgnoreCase("delete")) {
            handleDelete(sender, args);
        }
    }


    private void handleCreate(CommandSender sender, String[] args) {
        if (noPermission(sender, "dba.command.group.create")) {
            return;
        }
        if (args.length < 3) {
            messages.send(sender, "usage-group-create");
            return;
        }
        ViewGroup group = manager.createGroup(args[2], ViewMode.TWO_SIDED);
        if (group == null) {
            messages.send(sender, "group-exists", "group", args[2]);
            return;
        }
        messages.send(sender, "group-created", "group", args[2]);
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (noPermission(sender, "dba.command.group.delete")) {
            return;
        }
        if (args.length < 3) {
            messages.send(sender, "usage-group-delete");
            return;
        }
        ViewGroup group = manager.deleteGroup(args[2]);
        if (group == null) {
            messages.send(sender, "group-not-found", "group", args[2]);
            return;
        }
        for (BlockKey key : group.getBlocks().keySet()) {
            viewService.restore(key);
        }
        messages.send(sender, "group-deleted", "group", args[2], "blocks", String.valueOf(group.getBlocks().size()));
    }

    private void handlePosition(CommandSender sender, String[] args) {
        if (noPermission(sender, "dba.command.pos")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-players");
            return;
        }

        RayTraceResult hit = player.rayTraceBlocks(selectionRange);
        if (hit == null || hit.getHitBlock() == null) {
            messages.send(player, "look-at-block", "range", String.valueOf(selectionRange));
            return;
        }
        Block block = hit.getHitBlock();
        Selection sel = selectionManager.getSelection(player);

        if (args[0].equalsIgnoreCase("pos1")) {
            sel.setPos1(block);
        } else {
            sel.setPos2(block);
        }
        messages.send(player, "pos-set", "pos", args[0].toLowerCase(), "x", String.valueOf(block.getX()), "y", String.valueOf(block.getY()), "z", String.valueOf(block.getZ()));
    }

    private void handleQuickset(CommandSender sender, String[] args) {
        if (noPermission(sender, "dba.command.quickset")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-players");
            return;
        }
        if (args.length < 3) {
            messages.send(player, "usage-quickset");
            return;
        }
        ViewGroup group = manager.getGroup(args[1]);
        if (group == null) {
            messages.send(player, "group-not-found", "group", args[1]);
            return;
        }
        Material mat = Material.matchMaterial(args[2]);
        if (mat == null || !mat.isBlock()) {
            messages.send(player, "unknown-block", "block", args[2]);
            return;
        }
        RayTraceResult hit = player.rayTraceBlocks(selectionRange);
        if (hit == null || hit.getHitBlock() == null || hit.getHitBlockFace() == null) {
            messages.send(player, "look-at-block", "range", String.valueOf(selectionRange));
            return;
        }
        Block block = hit.getHitBlock();
        BlockFace face = hit.getHitBlockFace();
        BlockBackup before = backup(block);
        SetResult result = applyView(group, block, face, mat.createBlockData());
        if (result == SetResult.ADDED) {
            messages.send(player, "quickset-added", "group", group.getName(), "side", face.name(), "block", mat.name());
        } else if (result == SetResult.UPDATED) {
            messages.send(player, "quickset-updated", "side", face.name(), "block", mat.name());
        } else {
            messages.send(player, "other-group");
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
        if (noPermission(sender, "dba.command.set")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-players");
            return;
        }
        if (args.length < 3) {
            messages.send(player,"usage-set");
            return;
        }
        ViewGroup group = manager.getGroup(args[1]);
        if (group == null) {
            messages.send(player, "group-not-found", "group", args[1]);
            return;
        }
        Material mat = Material.matchMaterial(args[2]);
        if (mat == null || !mat.isBlock()) {
            messages.send(player, "unknown-block", "block", args[2]);
            return;
        }

        Selection sel = selectionManager.getSelection(player);
        if (!sel.isComplete()) {
            messages.send(player, "selection-incomplete");
            return;
        }
        if (sel.getVolume() > maxBlocks) {
            messages.send(player, "selection-too-large", "max", String.valueOf(maxBlocks));
            return;
        }

        BlockFace face;
        if (args.length >= 4) {
            try {
                face = BlockFace.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                messages.send(player, "unknown-side", "side", args[3]);
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
        messages.send(player, "set-result", "side", face.name(), "block", mat.name(), "added", String.valueOf(added), "updated", String.valueOf(updated), "skipped", String.valueOf(skipped));
    }

    private void handleWand(CommandSender sender, String[] args) {
        if (noPermission(sender, "dba.command.wand")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-players");
            return;
        }
        player.getInventory().addItem(wand.createWand());
        messages.send(player, "wand-given");
    }

    private void handleClear(CommandSender sender, String[] args) {
        if (noPermission(sender, "dba.command.clear")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-players");
            return;
        }
        Selection sel = selectionManager.getSelection(player);
        if (!sel.isComplete()) {
            messages.send(player, "selection-incomplete");
            return;
        }
        if (sel.getVolume() > maxBlocks) {
            messages.send(player, "selection-too-large", "max", String.valueOf(maxBlocks));
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
        messages.send(player, "clear-result", "count", String.valueOf(removed));
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
        if (noPermission(sender, "dba.command.undo")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "only-players");
            return;
        }
        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                messages.send(player, "undo-not-a-number", "input", args[1]);
                return;
            }
            if (amount < 1) {
                messages.send(player, "undo-min");
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
            messages.send(player, "undo-nothing");
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            viewService.update(p);
        }
        messages.send(player, "undo-result", "changes", String.valueOf(undone), "blocks", String.valueOf(blocks));
    }

    private boolean noPermission(CommandSender sender, String perm) {
        if (sender.hasPermission(perm)) {
            return false;
        }
        messages.send(sender, "no-permission");
        return true;
    }
}
