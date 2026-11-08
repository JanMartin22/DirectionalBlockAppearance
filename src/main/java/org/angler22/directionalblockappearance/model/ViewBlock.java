package org.angler22.directionalblockappearance.model;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.util.EnumMap;
import java.util.Map;

/**
 * A single block that belongs to a {@link ViewGroup}.
 * Stores which fake block is shown for each side.
 * Sides without a view show the real block.
 */
public class ViewBlock {
    private final BlockKey key;
    private final ViewGroup group;
    private final Map<BlockFace, BlockData> views = new EnumMap<>(BlockFace.class);

    /**
     * Creates a new block without any views.
     *
     * @param key   the position of the block
     * @param group the group this block belongs to
     */
    public ViewBlock(BlockKey key, ViewGroup group) {
        this.key = key;
        this.group = group;
    }

    /**
     * Returns the position of this block.
     *
     * @return the position of this block (world and coordinates)
     */
    public BlockKey getKey() {
        return key;
    }

    /**
     * Returns the group this block belongs to.
     *
     * @return the group this block belongs to
     */
    public ViewGroup getGroup() {
        return group;
    }

    /**
     * Returns all views of this block.
     *
     * @return all set views, mapped from side to the fake block shown on that side
     */
    public Map<BlockFace, BlockData> getViews() {
        return views;
    }

    /**
     * Sets the fake block shown on the given side.
     * Replaces any view already set for that side.
     *
     * @param face the side to set
     * @param data the fake block to show on that side
     */
    public void setView(BlockFace face, BlockData data){
        views.put(face, data);
    }

    /**
     * Removes the view for the given side.
     * That side shows the real block again.
     *
     * @param face the side to remove the view from
     */
    public void removeView(BlockFace face){
        views.remove(face);
    }

    /**
     * Returns the block data shown when the block is viewed from the given side.
     * Falls back to the real block if no view is set for that side.
     *
     * @param face the side the block is viewed from.
     * @return the block data to show, or null if no view is set and the world is not loaded.
     */
    public BlockData getView(BlockFace face) {
        BlockData view = views.get(face);
        if (view == null) {
            return getRealBlock();
        }
        return view;
    }

    /**
     * Returns the block that actually exists at this position in the world.
     *
     * @return the real block data, or null if the world is not loaded.
     */
    public BlockData getRealBlock() {
        Location center = key.toCenter();
        if (center == null) {
            return null;
        }
        return center.getBlock().getBlockData();
    }

    /**
     * Removes all views. Every side shows the real block again.
     */
    public void clearViews(){
        views.clear();
    }
}

