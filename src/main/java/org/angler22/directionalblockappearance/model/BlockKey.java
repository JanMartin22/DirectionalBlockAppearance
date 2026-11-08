package org.angler22.directionalblockappearance.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.UUID;

/**
 * The position of a block: world plus x, y and z coordinates.
 * Two keys are equal if world and coordinates are equal,
 * so this class can be used as a map key.
 */
public final class BlockKey {
    private final UUID world;
    private final int x, y, z;

    /**
     * Creates a key from the given world and coordinates.
     *
     * @param world the UUID of the world
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public BlockKey(UUID world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a key for the position of the given block.
     *
     * @param b the block to take world and coordinates from
     */
    public BlockKey(Block b) {
        this(b.getWorld().getUID(), b.getX(), b.getY(), b.getZ());
    }

    /**
     * Returns the world of this position.
     *
     * @return the UUID of the world
     */
    public UUID getWorld() {
        return world;
    }

    /**
     * Returns the x coordinate of this position.
     *
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate of this position.
     *
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the z coordinate of this position.
     *
     * @return the z coordinate
     */
    public int getZ() {
        return z;
    }

    /**
     * Two keys are equal if they have the same world and coordinates.
     *
     * @param o the object to compare with
     * @return true if o is a BlockKey with the same world and coordinates
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlockKey blockKey = (BlockKey) o;
        return x == blockKey.x && y == blockKey.y && z == blockKey.z && Objects.equals(world, blockKey.world);
    }

    /**
     * @return a hash code based on world and coordinates
     */
    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    /**
     * @return the center of the block, or null if the world is not loaded
     */
    public Location toCenter(){

        World w = Bukkit.getWorld(world);
        if(w == null){
            return null;
        }
        return new Location(w, x+0.5, y+0.5, z+0.5);
    }
}
