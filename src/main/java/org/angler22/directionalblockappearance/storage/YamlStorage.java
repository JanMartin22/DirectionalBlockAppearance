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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Stores all groups in a YAML file (groups.yml in the plugin folder).
 * <p>
 * Format:
 * <pre>
 * groups:
 *   windows:
 *     mode: TWO_SIDED
 *     blocks:
 *       - world: 4f1c...-uuid
 *         x: 10
 *         y: 64
 *         z: -5
 *         views:
 *           NORTH: minecraft:glass
 * </pre>
 */
public final class YamlStorage implements Storage {

    private final File file;
    private final Logger logger;

    /**
     * Creates a storage that reads and writes the given file.
     *
     * @param file   the YAML file to use
     * @param logger the logger for save and load errors
     */
    public YamlStorage(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Writes to a temporary file first and then replaces the old file.
     * If the file system supports atomic moves, a failed save never
     * damages the existing file. If saving fails, an error is logged.
     */
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

        File temp = new File(file.getParentFile(), file.getName() + ".tmp");
        try {
            yaml.save(temp);
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.severe("Could not save " + file.getName() + ": " + e.getMessage());
        } finally {
            try {
                Files.deleteIfExists(temp.toPath());
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Does nothing if the file does not exist yet.
     * Invalid groups or blocks are skipped and a warning is logged.
     * If anything was skipped, a copy of the original file is saved
     * as groups-broken-&lt;time&gt;.yml, so no data is lost when the file
     * is overwritten later.
     */
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

        boolean hadErrors = false;

        for (String name : groups.getKeys(false)) {
            String path = "groups." + name;

            ViewMode mode;
            try {
                mode = ViewMode.valueOf(yaml.getString(path + ".mode"));
            } catch (Exception e) {
                logger.warning("Could not load group " + name + ": invalid mode");
                hadErrors = true;
                continue;
            }

            ViewGroup group = manager.createGroup(name, mode);
            if (group == null) {
                logger.warning("Could not load group " + name + ": a group with the same name already exists");
                hadErrors = true;
                continue;
            }

            for (Map<?, ?> entry : yaml.getMapList(path + ".blocks")) {
                try {
                    UUID world = UUID.fromString((String) entry.get("world"));
                    int x = ((Number) entry.get("x")).intValue();
                    int y = ((Number) entry.get("y")).intValue();
                    int z = ((Number) entry.get("z")).intValue();

                    Map<BlockFace, BlockData> parsedViews = new EnumMap<>(BlockFace.class);
                    Map<?, ?> views = (Map<?, ?>) entry.get("views");
                    if (views != null) {
                        for (Map.Entry<?, ?> view : views.entrySet()) {
                            BlockFace face = BlockFace.valueOf((String) view.getKey());
                            BlockData data = Bukkit.createBlockData((String) view.getValue());
                            parsedViews.put(face, data);
                        }
                    }

                    ViewBlock vb = manager.addBlock(group, new BlockKey(world, x, y, z));
                    if (vb == null) {
                        continue;
                    }
                    for (Map.Entry<BlockFace, BlockData> view : parsedViews.entrySet()) {
                        vb.setView(view.getKey(), view.getValue());
                    }
                } catch (Exception e) {
                    logger.warning("Could not load a block in group " + name + ": " + e.getMessage());
                    hadErrors = true;
                }
            }
        }

        if (hadErrors) {
            File backup = new File(file.getParentFile(), "groups-broken-" + System.currentTimeMillis() + ".yml");
            try {
                Files.copy(file.toPath(), backup.toPath());
                logger.warning("Some data could not be loaded. A copy of the original file was saved as " + backup.getName());
            } catch (IOException e) {
                logger.severe("Could not back up " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}