package com.winterhaven_mc.creativenono;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to prevent creative mode players from sharing
 * items with survival mode players.
 * 
 * @author      Tim Savage
 * @version		1.0
 */
final class PluginMain extends JavaPlugin {

	@SuppressWarnings("unused")
	public boolean debug = getConfig().getBoolean("debug");

	MessageManager messagemanager;

	@Override
	public void onEnable() {
		
        // Save a copy of the default config.yml if file does not already exist
        saveDefaultConfig();
		
        // instantiate event listener
		new EventListener(this);

		// instantiate command handler
		new CommandManager(this);

		// instantiate message manager
		messagemanager = new MessageManager(this);
		
	}

}
