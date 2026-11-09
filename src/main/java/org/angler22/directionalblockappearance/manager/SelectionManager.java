package org.angler22.directionalblockappearance.manager;

import org.angler22.directionalblockappearance.model.Selection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps the {@link Selection} of every player.
 * Selections only exist while the player is online and are removed on logout.
 */
public class SelectionManager {
    private final Map<UUID, Selection> selections = new HashMap<>();

    /**
     * Returns the selection of the player.
     * Creates a new empty selection if the player has none yet.
     *
     * @param player the player whose selection is needed
     * @return the selection of the player, never null
     */
    public Selection getSelection(Player player) {
        Selection sel = selections.get(player.getUniqueId());
        if (sel == null) {
            sel = new Selection();
            selections.put(player.getUniqueId(), sel);
        }
        return sel;
    }

    /**
     * Removes the selection of the player.
     * Used when the player leaves the server.
     *
     * @param player the player whose selection is removed
     */
    public void clear(Player player) {
        selections.remove(player.getUniqueId());
    }
}
