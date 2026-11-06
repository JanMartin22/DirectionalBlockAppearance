package org.angler22.directionalblockappearance.manager;

import org.angler22.directionalblockappearance.model.BlockBackup;
import org.bukkit.entity.Player;

import java.util.*;

public class UndoManager {
    private final Map<UUID, List<List<BlockBackup>>> history = new HashMap<>();
    private final int maxSteps;

    public UndoManager(int maxSteps) {
        this.maxSteps = maxSteps;
    }

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
    public List<BlockBackup> pop(Player player) {
        List<List<BlockBackup>> list = history.get(player.getUniqueId());
        if(list == null || list.isEmpty()){
            return null;
        }
        return list.removeLast();
    }
}

