package org.angler22.directionalblockappearance.model;

import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player's selected region, defined by two corner positions
 * similar to WorldEdit.
 * Players can set the corners using /dba pos1, /dba pos2 or the selection wand.
 */
public class Selection {

    private Block pos1;
    private Block pos2;

    /**
     * Returns the first corner of the selection.
     *
     * @return the first corner, or null if it is not set
     */
    public Block getPos1() {
        return pos1;
    }

    /**
     * Returns the second corner of the selection.
     *
     * @return the second corner, or null if it is not set
     */
    public Block getPos2() {
        return pos2;
    }

    /**
     * Sets the first corner.
     *
     * @param pos1 the block to use as first corner
     */
    public void setPos1(Block pos1) {
        this.pos1 = pos1;
    }

    /**
     * Sets the second corner.
     *
     * @param pos2 the block to use as second corner
     */
    public void setPos2(Block pos2) {
        this.pos2 = pos2;
    }

    /**
     * Checks if the selection can be used.
     *
     * @return true if both corners are set and in the same world
     */
    public boolean isComplete() {
        return pos1 != null && pos2 != null && pos1.getWorld().equals(pos2.getWorld());
    }

    /**
     * Returns the number of blocks in the selected region.
     * Uses long, so very large selections do not overflow.
     * Only call this if {@link #isComplete()} is true.
     *
     * @return the number of blocks between both corners, including the corners
     */
    public long getVolume() {
        return (long) (Math.abs(pos1.getX() - pos2.getX()) + 1) * (Math.abs(pos1.getY() - pos2.getY()) + 1) * (Math.abs(pos1.getZ() - pos2.getZ()) + 1);
    }

    /**
     * Returns all blocks in the selected region, no matter which corner is where.
     * Only call this if {@link #isComplete()} is true.
     *
     * @return all blocks between both corners, including the corners
     */
    public List<Block> getBlocks() {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        World world = pos1.getWorld();
        List<Block> blocks = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }
}
