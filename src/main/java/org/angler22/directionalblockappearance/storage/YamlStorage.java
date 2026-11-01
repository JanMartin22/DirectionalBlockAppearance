package org.angler22.directionalblockappearance.storage;

import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.model.BlockKey;
import org.angler22.directionalblockappearance.model.ViewBlock;
import org.angler22.directionalblockappearance.model.ViewGroup;
import org.angler22.directionalblockappearance.model.ViewMode;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class YamlStorage implements Storage {

    private final File file;
    private final Logger logger;

    public YamlStorage(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    @Override
    public void saveAll(AppearanceManager manager) {
        YamlConfiguration yaml = new YamlConfiguration();

        for (ViewGroup group : manager.getGroups()) {
            String path = "groups." + group.getName();

            yaml.set(path + ".mode", group.getMode().name());

            List<Map<String, Object>> blockList = new ArrayList<>();
            for (ViewBlock vb : group.getBlocks().values()) {
                BlockKey key = vb.getKey();

                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("world", key.getWorld().toString());
                entry.put("x", key.getX());
                entry.put("y", key.getY());
                entry.put("z", key.getZ());

                Map<String, String> views = new LinkedHashMap<>();
                for (Map.Entry<BlockFace, BlockData> view : vb.getViews().entrySet()) {
                    views.put(view.getKey().name(), view.getValue().getAsString());
                }
                entry.put("views", views);

                blockList.add(entry);
            }
            yaml.set(path + ".blocks", blockList);
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            logger.severe("Could not save " + file.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void loadAll(AppearanceManager manager) {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection groups = yaml.getConfigurationSection("groups");
        if (groups == null) {
            return;
        }

        for (String name : groups.getKeys(false)) {
            String path = "groups." + name;
            try {
                ViewMode mode = ViewMode.valueOf(yaml.getString(path + ".mode"));
                ViewGroup group = manager.createGroup(name, mode);
                if (group == null) {
                    continue;
                }

                for (Map<?, ?> entry : yaml.getMapList(path + ".blocks")) {
                    UUID world = UUID.fromString((String) entry.get("world"));
                    int x = ((Number) entry.get("x")).intValue();
                    int y = ((Number) entry.get("y")).intValue();
                    int z = ((Number) entry.get("z")).intValue();

                    ViewBlock vb = manager.addBlock(group, new BlockKey(world, x, y, z));
                    if (vb == null) {
                        continue;
                    }

                    Map<?, ?> views = (Map<?, ?>) entry.get("views");
                    if (views != null) {
                        for (Map.Entry<?, ?> view : views.entrySet()) {
                            BlockFace face = BlockFace.valueOf((String) view.getKey());
                            BlockData data = Bukkit.createBlockData((String) view.getValue());
                            vb.setView(face, data);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warning("Could not load group " + name + ": " + e.getMessage());
            }
        }
    }
}