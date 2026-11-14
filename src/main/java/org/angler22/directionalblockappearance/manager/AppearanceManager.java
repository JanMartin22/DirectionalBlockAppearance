package org.angler22.directionalblockappearance.manager;

import org.angler22.directionalblockappearance.model.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.util.*;

/**
 * Holds all groups and DBA blocks. All changes during the game go through this class.
 * <p>
 * Groups are stored by name, blocks additionally in an index by position,
 * so a block can be found quickly without searching every group.
 * A block can only be in one group at a time.
 * <p>
 * Every change sets a changed flag. The autosave only writes the file
 * if something changed since the last save.
 * <p>
 * This class only changes data. Sending fake or real blocks to players
 * is done by {@link org.angler22.directionalblockappearance.view.ViewService}.
 */
public class AppearanceManager {
    private final Map<String, ViewGroup> groups = new HashMap<>();
    private final Map<BlockKey, ViewBlock> index = new HashMap<>();
    private boolean changed = false;

    /**
     * Creates a new empty group. The name is stored in lower case,
     * so "Test" and "test" are the same group.
     *
     * @param name the name of the group
     * @param mode how the blocks of the group decide which view to show
     * @return the new group, or null if a group with this name already exists
     */
    public ViewGroup createGroup(String name, ViewMode mode) {
        String lowerName = name.toLowerCase(Locale.ROOT);
        if (groups.containsKey(lowerName)) {
            return null;
        } else {
            ViewGroup vg = new ViewGroup(lowerName, mode);
            groups.put(lowerName, vg);
            changed = true;
            return vg;
        }
    }

    /**
     * Returns the group with the given name, ignoring upper and lower case.
     *
     * @param name the name of the group
     * @return the group, or null if it does not exist
     */
    public ViewGroup getGroup(String name) {
        return groups.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the DBA block at the given position.
     *
     * @param key the position to look up
     * @return the block, or null if there is no DBA block at this position
     */
    public ViewBlock getBlock(BlockKey key) {
        return index.get(key);
    }

    /**
     * Adds a block with one view to a group.
     * Used by /dba set and /dba quickset.
     *
     * @param group the group to add the block to
     * @param block the block in the world
     * @param face  the side that shows the fake block
     * @param data  the fake block to show on that side
     * @return the new DBA block, or null if this position is already a DBA block
     * or the group is not managed by this manager
     */
    public ViewBlock addBlock(ViewGroup group, Block block, BlockFace face, BlockData data) {
        BlockKey key = new BlockKey(block);

        if (groups.get(group.getName()) != group) {
            return null;
        }
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

    /**
     * Removes the DBA block at the given position from its group.
     * Does nothing if there is no DBA block there.
     *
     * @param key the position of the block to remove
     */
    public void removeBlock(BlockKey key) {
        ViewBlock vb = index.get(key);
        index.remove(key);
        if (vb != null) {
            vb.getGroup().removeBlock(key);
            changed = true;
        }
    }

    /**
     * Returns all DBA blocks whose center is within the given radius of a location.
     * Groups whose bounding box is too far away are skipped without checking their blocks.
     *
     * @param loc the location to search around, usually a player
     * @param r   the radius in blocks
     * @return all blocks within the radius, empty if there are none
     */
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
                BlockKey key = vb.getKey();
                double dx = key.getX() + 0.5 - loc.getX();
                double dy = key.getY() + 0.5 - loc.getY();
                double dz = key.getZ() + 0.5 - loc.getZ();
                if (dx * dx + dy * dy + dz * dz <= rSquared) {
                    list.add(vb);
                }
            }
        }
        return list;
    }

    /**
     * Returns all groups.
     *
     * @return all groups (read-only)
     */
    public Collection<ViewGroup> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    /**
     * Adds a block without any views to a group.
     * Used when loading from storage and by undo.
     *
     * @param group the group to add the block to
     * @param key   the position of the block
     * @return the new DBA block, or null if this position is already a DBA block
     * or the group is not managed by this manager
     */
    public ViewBlock addBlock(ViewGroup group, BlockKey key) {
        if (groups.get(group.getName()) != group) {
            return null;
        }

        if (index.containsKey(key)) {
            return null;
        }
        ViewBlock vb = new ViewBlock(key, group);
        group.addBlock(vb);
        index.put(key, vb);
        changed = true;
        return vb;
    }

    /**
     * Checks if anything changed since the last save.
     *
     * @return true if groups or blocks changed since {@link #markSaved()}
     */
    public boolean hasChanged() {
        return changed;
    }

    /**
     * Marks the current state as saved.
     */
    public void markSaved() {
        changed = false;
    }

    /**
     * Deletes the group with the given name, ignoring upper and lower case,
     * and all its blocks.
     * Does not send the real blocks to the players, the caller has to do that.
     *
     * @param name the name of the group
     * @return the deleted group, or null if it does not exist
     */
    public ViewGroup deleteGroup(String name) {
        ViewGroup group = groups.remove(name.toLowerCase(Locale.ROOT));
        if (group == null) {
            return null;
        }
        for (BlockKey key : group.getBlocks().keySet()) {
            index.remove(key);
        }
        changed = true;
        return group;
    }

    /**
     * Replaces all views of a block with one new view.
     *
     * @param vb   the block to change
     * @param face the side that shows the fake block
     * @param data the fake block to show on that side
     */
    public void replaceView(ViewBlock vb, BlockFace face, BlockData data) {

        vb.clearViews();
        vb.setView(face, data);
        changed = true;
    }

    /**
     * Restores a block to the state saved in the backup. Used by /dba undo.
     * <p>
     * If the block was not a DBA block before, it is removed.
     * If its group was deleted in the meantime, it is removed and not restored.
     * Does not send anything to the players, the caller has to do that.
     *
     * @param backup the saved state of the block
     */
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
        if (vb == null) {
            return;
        }
        for (Map.Entry<BlockFace, BlockData> entry : backup.getViews().entrySet()) {
            vb.setView(entry.getKey(), entry.getValue());
        }
    }
}
