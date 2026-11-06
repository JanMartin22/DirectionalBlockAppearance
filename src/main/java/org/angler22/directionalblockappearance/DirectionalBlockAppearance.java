package org.angler22.directionalblockappearance;

import org.angler22.directionalblockappearance.command.AppearanceCommand;
import org.angler22.directionalblockappearance.command.AppearanceTabCompleter;
import org.angler22.directionalblockappearance.listener.ProtectionListener;
import org.angler22.directionalblockappearance.listener.ViewListener;
import org.angler22.directionalblockappearance.listener.WandListener;
import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.manager.SelectionManager;
import org.angler22.directionalblockappearance.manager.UndoManager;
import org.angler22.directionalblockappearance.storage.Storage;
import org.angler22.directionalblockappearance.storage.YamlStorage;
import org.angler22.directionalblockappearance.view.ViewService;
import org.angler22.directionalblockappearance.wand.Wand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DirectionalBlockAppearance extends JavaPlugin {
    private AppearanceManager manager;
    private Storage storage;
    private ViewService viewService;
    private SelectionManager selectionManager;
    private Wand wand;
    private UndoManager undoManager;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        manager = new AppearanceManager();
        storage = new YamlStorage(new File(getDataFolder(), "groups.yml"), getLogger());
        storage.loadAll(manager);
        manager.markSaved();
        viewService = new ViewService(manager, getConfig().getDouble("view.distance", 48));
        selectionManager = new SelectionManager();
        wand = new Wand(this);
        undoManager = new UndoManager(getConfig().getInt("undo.max-steps", 15));

        getServer().getPluginManager().registerEvents(new ProtectionListener(manager, viewService, this), this);

        long resyncInterval = getConfig().getLong("view.resync-interval", 40);
        Bukkit.getScheduler().runTaskTimer(this, () -> viewService.resyncAll(), resyncInterval, resyncInterval);

        getServer().getPluginManager().registerEvents(new ViewListener(this, viewService), this);

        getServer().getPluginManager().registerEvents(new WandListener(wand, selectionManager), this);

        getCommand("dba").setTabCompleter(new AppearanceTabCompleter(manager));

        long autosaveInterval = getConfig().getLong("storage.autosave-interval", 600);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (manager.hasChanged()) {
                storage.saveAll(manager);
                manager.markSaved();
            }
        }, autosaveInterval, autosaveInterval);


        getCommand("dba").setExecutor(new AppearanceCommand(manager, viewService, selectionManager, wand, undoManager,
                getConfig().getInt("selection.range", 25),
                getConfig().getInt("selection.max-blocks", 10000)));

        getLogger().info("Enabled");
    }

    @Override
    public void onDisable() {
        viewService.restoreAll();
        storage.saveAll(manager);
    }
}