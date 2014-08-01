package com.winterhaven_mc.creativenono;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to prevent creative mode players from sharing
 * items with survival mode players.
 * 
 * @author      Tim Savage
 * @version		1.0
 */
public final class CreativeNoNoMain extends JavaPlugin {

	final Boolean debug = getConfig().getBoolean("debug");
	public MessageManager messagemanager;

	@Override
	public void onEnable() {
		
		// register command executor
		getCommand("nono").setExecutor(new CommandHandler(this));
		
        // Save a copy of the default config.yml if file does not already exist
        saveDefaultConfig();
		
        // instantiate listener object
		new EventListener(this);
		
		// instantiate message manager
		messagemanager = new MessageManager(this);
		
	}

	@Override
	public void onDisable() {
		// TODO Insert logic to be performed when the plugin is disabled
	}
}
