package org.angler22.directionalblockappearance.model;

import org.bukkit.Location;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ViewGroup {
    private final String name;
    private final ViewMode mode;
    private final Map<BlockKey, ViewBlock> blocks = new HashMap<>();
    private UUID boxWorld;
    private int minX, minY, minZ, maxX, maxY, maxZ;
    private boolean multiWorld = false;
    private boolean boxDirty = false;

    public ViewGroup(String name, ViewMode mode) {
        this.name = name;
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public ViewMode getMode() {
        return mode;
    }

    public Map<BlockKey, ViewBlock> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    public void addBlock(ViewBlock block){
        blocks.put(block.getKey(), block);
        expandBox(block.getKey());
    }

    public void removeBlock(BlockKey key){
        blocks.remove(key);
        boxDirty = true;
    }

    public ViewBlock getBlock(BlockKey key){
        return blocks.get(key);
    }

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

    private void recalculateBox() {
        boxWorld = null;
        multiWorld = false;
        for (BlockKey key : blocks.keySet()) {
            expandBox(key);
        }
    }

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
