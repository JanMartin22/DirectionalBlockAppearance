package org.angler22.directionalblockappearance.manager;

import org.angler22.directionalblockappearance.model.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.util.*;

public class AppearanceManager {
    private final Map<String, ViewGroup> groups = new HashMap<>();
    private final Map<BlockKey, ViewBlock> index = new HashMap<>();
    private boolean changed = false;

    public ViewGroup createGroup(String name, ViewMode mode) {
        if (groups.containsKey(name)) {
            return null;
        } else {
            ViewGroup vg = new ViewGroup(name, mode);
            groups.put(name, vg);
            changed = true;
            return vg;
        }
    }

    public ViewGroup getGroup(String name) {
        return groups.get(name);
    }

    public ViewBlock getBlock(BlockKey key) {
        return index.get(key);
    }

    public ViewBlock addBlock(ViewGroup group, Block block, BlockFace face, BlockData data) {
        BlockKey key = new BlockKey(block);
        if (index.containsKey(key)) {
            return null;
        }
        ViewBlock vb = new ViewBlock(key, group);
        vb.setView(face, data);
        group.addBlock(vb);
        index.put(key, vb);
        changed = true;
        return vb;
    }

    public void removeBlock(BlockKey key) {
        ViewBlock vb = index.get(key);
        index.remove(key);
        if (vb != null) {
            vb.getGroup().removeBlock(key);
            changed = true;
        }
    }

    public List<ViewBlock> getNearby(Location loc, double r) {
        List<ViewBlock> list = new ArrayList<>();
        double rSquared = r * r;
        UUID worldId = loc.getWorld().getUID();

        for (ViewGroup group : groups.values()) {
            if (!group.isNear(loc, r)) {
                continue;
            }
            for (ViewBlock vb : group.getBlocks().values()) {
                if (!vb.getKey().getWorld().equals(worldId)) {
                    continue;
                }
                Location center = vb.getKey().toCenter();
                if (center == null) {
                    continue;
                }
                if (center.distanceSquared(loc) <= rSquared) {
                    list.add(vb);
                }
            }
        }
        return list;
    }

    public Collection<ViewGroup> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public ViewBlock addBlock(ViewGroup group, BlockKey key) {
        if (index.containsKey(key)) {
            return null;
        }
        ViewBlock vb = new ViewBlock(key, group);
        group.addBlock(vb);
        index.put(key, vb);
        changed = true;
        return vb;
    }

    public boolean hasChanged() {
        return changed;
    }

    public void markSaved() {
        changed = false;
    }

    public ViewGroup deleteGroup(String name) {
        ViewGroup group = groups.remove(name);
        if (group == null) {
            return null;
        }
        for (BlockKey key : group.getBlocks().keySet()) {
            index.remove(key);
        }
        changed = true;
        return group;
    }

    public void replaceView(ViewBlock vb, BlockFace face, BlockData data) {

        vb.clearViews();
        vb.setView(face, data);
        changed = true;
    }

    public void restoreBackup(BlockBackup backup) {
        removeBlock(backup.getKey());
        if (backup.getGroupName() == null) {
            return;
        }
        ViewGroup group = getGroup(backup.getGroupName());
        if (group == null) {
            return;
        }
        ViewBlock vb = addBlock(group, backup.getKey());
        for (Map.Entry<BlockFace, BlockData> entry : backup.getViews().entrySet()) {
            vb.setView(entry.getKey(), entry.getValue());
        }
    }
}
