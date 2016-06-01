package com.winterhaven_mc.creativenono;

import com.winterhaven_mc.util.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to prevent creative mode players from sharing
 * items with survival mode players.
 * 
 * @author      Tim Savage
 * @version		1.0
 */
public final class PluginMain extends JavaPlugin {

	@SuppressWarnings("unused")
	public boolean debug = getConfig().getBoolean("debug");

	MessageManager messageManager;
	WorldManager worldManager;


	@Override
	public void onEnable() {
		
        // Save a copy of the default config.yml if file does not already exist
        saveDefaultConfig();
		
        // instantiate event listener
		new EventListener(this);

		// instantiate command handler
		new CommandManager(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate message manager
		messageManager = new MessageManager(this);
		
	}

}
