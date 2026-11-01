package org.angler22.directionalblockappearance.manager;

import org.angler22.directionalblockappearance.model.Selection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {
    private final Map<UUID, Selection> selections = new HashMap<>();

    public Selection getSelection(Player player) {
        Selection sel = selections.get(player.getUniqueId());
        if (sel == null) {
            sel = new Selection();
            selections.put(player.getUniqueId(), sel);
        }
        return sel;
    }

    public void clear(Player player) {
        selections.remove(player.getUniqueId());
    }
}
