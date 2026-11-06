package org.angler22.directionalblockappearance.model;

import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class Selection {

    private Block pos1;
    private Block pos2;

    public Block getPos1() {
        return pos1;
    }

    public Block getPos2() {
        return pos2;
    }

    public void setPos1(Block pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(Block pos2) {
        this.pos2 = pos2;
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public int getVolume() {
        return (Math.abs(pos1.getX() - pos2.getX()) + 1) * (Math.abs(pos1.getY() - pos2.getY()) + 1) * (Math.abs(pos1.getZ() - pos2.getZ()) + 1);
    }

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
