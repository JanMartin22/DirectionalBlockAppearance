package org.angler22.directionalblockappearance.model;

import org.bukkit.Location;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A named group of DBA blocks, for example all blocks of one window.
 * Players create groups with /dba group create and fill them with /dba set.
 * <p>
 * Each group keeps a bounding box around the centers of all its blocks.
 * It is used to skip whole groups that are too far away from a player.
 * The box is expanded when a block is added and recalculated lazily
 * after a block was removed.
 */
public final class ViewGroup {
    private final String name;
    private final ViewMode mode;
    private final Map<BlockKey, ViewBlock> blocks = new HashMap<>();
    private UUID boxWorld;
    private int minX, minY, minZ, maxX, maxY, maxZ;
    private boolean multiWorld = false;
    private boolean boxDirty = false;

    /**
     * Creates a new empty group.
     *
     * @param name the name of the group
     * @param mode how the blocks of this group decide which view to show
     */
    public ViewGroup(String name, ViewMode mode) {
        this.name = name;
        this.mode = mode;
    }

    /**
     * Returns the name of this group.
     *
     * @return the name of the group
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the view mode of this group.
     *
     * @return how the blocks of this group decide which view to show
     */
    public ViewMode getMode() {
        return mode;
    }

    /**
     * Returns all blocks of this group.
     *
     * @return all blocks of this group by position (read-only)
     */
    public Map<BlockKey, ViewBlock> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    /**
     * Adds a block to this group and expands the bounding box to include it.
     * A block at the same position is replaced.
     *
     * @param block the block to add
     */
    public void addBlock(ViewBlock block){
        blocks.put(block.getKey(), block);
        expandBox(block.getKey());
    }

    /**
     * Removes the block at the given position.
     * The bounding box is not shrunk right away. It is recalculated
     * the next time {@link #isNear(Location, double)} is called.
     *
     * @param key the position of the block to remove
     */
    public void removeBlock(BlockKey key){
        blocks.remove(key);
        boxDirty = true;
    }

    /**
     * Returns the block at the given position.
     *
     * @param key the position to look up
     * @return the block at that position, or null if it is not in this group
     */
    public ViewBlock getBlock(BlockKey key){
        return blocks.get(key);
    }

    /**
     * Expands the bounding box so it includes the given position.
     * If the position is in another world than the box, the group
     * is marked as multi-world instead.
     *
     * @param key the position to include
     */
    private void expandBox(BlockKey key) {
        if (boxWorld == null) {
            boxWorld = key.getWorld();
            minX = maxX = key.getX();
            minY = maxY = key.getY();
            minZ = maxZ = key.getZ();
            return;
        }
        if (!key.getWorld().equals(boxWorld)) {
            multiWorld = true;
            return;
        }
        minX = Math.min(minX, key.getX());
        maxX = Math.max(maxX, key.getX());
        minY = Math.min(minY, key.getY());
        maxY = Math.max(maxY, key.getY());
        minZ = Math.min(minZ, key.getZ());
        maxZ = Math.max(maxZ, key.getZ());
    }

    /**
     * Rebuilds the bounding box from all blocks of this group.
     */
    private void recalculateBox() {
        boxWorld = null;
        multiWorld = false;
        for (BlockKey key : blocks.keySet()) {
            expandBox(key);
        }
    }


    /**
     * Checks if the bounding box of this group is within the given radius of a location.
     * Uses the shortest distance from the location to the box (AABB distance).
     * <p>
     * Empty groups are never near. Groups with blocks in more than one world
     * are always treated as near, so they are never skipped.
     *
     * @param loc the location to check, usually a player
     * @param r   the radius in blocks
     * @return true if the box is within the radius of the location
     */
    public boolean isNear(Location loc, double r) {
        if (boxDirty) {
            recalculateBox();
            boxDirty = false;
        }
        if (boxWorld == null) {
            return false;
        }
        if (multiWorld) {
            return true;
        }
        if (!loc.getWorld().getUID().equals(boxWorld)) {
            return false;
        }
        double dx = Math.max(Math.max(minX + 0.5 - loc.getX(), 0), loc.getX() - (maxX + 0.5));
        double dy = Math.max(Math.max(minY + 0.5 - loc.getY(), 0), loc.getY() - (maxY + 0.5));
        double dz = Math.max(Math.max(minZ + 0.5 - loc.getZ(), 0), loc.getZ() - (maxZ + 0.5));
        return dx*dx + dy*dy + dz*dz <= r*r;
    }
}
