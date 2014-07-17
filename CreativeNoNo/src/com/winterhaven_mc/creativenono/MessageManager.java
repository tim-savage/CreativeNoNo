package com.winterhaven_mc.creativenono;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageManager {
	
	private final CreativeNoNoMain plugin;
	private ConfigAccessor messages;
	
	/**
	 * constructor method for <code>MessageManager</code>
	 * 
	 * @param	plugin		A reference to this plugin's main class
	 * @see		com.winterhaven_mc.spawnstar.SpawnStarMain
	 */
	public MessageManager(CreativeNoNoMain plugin) {

		this.plugin = plugin;
		
		// install localization files
		String[] localization_files = {"en-US","es-ES","de-DE"};	
		installLocalizationFiles(localization_files);
		
		// get configured language
		String language = plugin.getConfig().getString("language","en-US");
		
		if (!new File(plugin.getDataFolder() + "/language/" + language + "/messages.yml").exists()) {
			plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
			language = "en-US";
		}
		
		// instantiate custom configuration manager
		messages = new ConfigAccessor(plugin, "language/" + language + "/messages.yml");
		
	}
	
	/** Send message to player
	 * 
	 * @param player		Player to message
	 * @param messageID		Identifier of message to send form messages.yml
	 */
	public void sendPlayerMessage(Player player, String messageID) {

		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled",false)) {
			String message = messages.getConfig().getString("messages." + messageID + ".string");

			// strip colorcodes and special characters from variables
			String itemmaterial = player.getItemInHand().getType().toString().toLowerCase().replace('_', ' ');
			String playername = player.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playernickname = player.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playerdisplayname = player.getDisplayName();
			String worldname = player.getWorld().getName();

			// perform variable substitutions
			message = message.replaceAll("%itemmaterial%", itemmaterial);
			message = message.replaceAll("%playername%", playername);
			message = message.replaceAll("%playerdisplayname%", playerdisplayname);
			message = message.replaceAll("%playernickname%", playernickname);
			message = message.replaceAll("%worldname%", worldname);

			// send message to player
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
		}
	}
	
	
	private void installLocalizationFiles(String[] filelist) {
	
		for (String filename : filelist) {
			if (!new File(plugin.getDataFolder() + "/language/" + filename + "/messages.yml").exists()) {
				this.plugin.saveResource("language/" + filename + "/messages.yml",false);
				plugin.getLogger().info("Installed localization files for " + filename + ".");
			}
		}
	}
	
	public void reloadMessages() {
		messages.reloadConfig();
	}

}
