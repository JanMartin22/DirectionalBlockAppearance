package org.angler22.directionalblockappearance.manager;

import org.angler22.directionalblockappearance.model.BlockBackup;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Stores the last changes of each player for /dba undo.
 * <p>
 * Every change (one /dba set, /dba quickset or /dba clear) is one step:
 * a list of {@link BlockBackup}s with the state before the change.
 * Each player has their own history with at most maxSteps steps.
 * When it is full, the oldest step is removed.
 * The history is kept until the server restarts, also when the player logs out.
 */
public class UndoManager {
    private final Map<UUID, List<List<BlockBackup>>> history = new HashMap<>();
    private final int maxSteps;

    /**
     * Creates an undo manager.
     *
     * @param maxSteps the maximum number of steps kept per player (undo.max-steps)
     */
    public UndoManager(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    /**
     * Saves a change as the newest step of the player.
     * Removes the oldest step if the history is full.
     *
     * @param player the player who made the change
     * @param change the state of all changed blocks before the change
     */
    public void push(Player player, List<BlockBackup> change) {
        List<List<BlockBackup>> list = history.get(player.getUniqueId());
        if (list == null) {
            list = new ArrayList<>();
            history.put(player.getUniqueId(), list);
        }
        list.add(change);
        if(list.size() > maxSteps) {
            list.remove(0);
        }
    }

    /**
     * Removes and returns the newest step of the player.
     *
     * @param player the player who wants to undo
     * @return the state of the blocks before that change, or null if there is nothing to undo
     */
    public List<BlockBackup> pop(Player player) {
        List<List<BlockBackup>> list = history.get(player.getUniqueId());
        if(list == null || list.isEmpty()){
            return null;
        }
        return list.removeLast();
    }
}

