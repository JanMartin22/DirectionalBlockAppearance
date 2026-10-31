package org.angler22.directionalblockappearance;

import org.angler22.directionalblockappearance.command.AppearanceCommand;
import org.angler22.directionalblockappearance.listener.ViewListener;
import org.angler22.directionalblockappearance.manager.AppearanceManager;
import org.angler22.directionalblockappearance.view.ViewService;
import org.bukkit.plugin.java.JavaPlugin;

public final class DirectionalBlockAppearance extends JavaPlugin {

    @Override
    public void onEnable() {
        AppearanceManager manager = new AppearanceManager();
        ViewService viewService = new ViewService(manager, 48);

        getServer().getPluginManager().registerEvents(new ViewListener(this, viewService), this);

        getCommand("dba").setExecutor(new AppearanceCommand(manager, viewService));

        getLogger().info("Enabled");
    }
}