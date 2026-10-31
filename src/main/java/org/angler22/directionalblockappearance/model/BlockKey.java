package org.angler22.directionalblockappearance.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.UUID;

public final class BlockKey {
    private final UUID world;
    private final int x, y, z;


    public BlockKey(UUID world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockKey(Block b) {
        this(b.getWorld().getUID(), b.getX(), b.getY(), b.getZ());
    }


    public int getX() {
        return x;
    }

    public UUID getWorld() {
        return world;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlockKey blockKey = (BlockKey) o;
        return x == blockKey.x && y == blockKey.y && z == blockKey.z && Objects.equals(world, blockKey.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    public Location toCenter(){

        World w = Bukkit.getWorld(world);
        if(w == null){
            return null;
        }
        return new Location(w, x+0.5, y+0.5, z+0.5);
    }
}
