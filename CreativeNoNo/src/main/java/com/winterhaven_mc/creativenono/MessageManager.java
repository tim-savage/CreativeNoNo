package com.winterhaven_mc.creativenono;

import com.winterhaven_mc.util.ConfigAccessor;
import com.winterhaven_mc.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class MessageManager {
	
	private final PluginMain plugin;
	private ConfigAccessor messages;
	private final String directoryName = "language";
	private String language;


	/**
	 * constructor method for MessageManager class
	 * 
	 * @param	plugin		reference to main class
	 */
	MessageManager(PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// install localization files
		this.installLocalizationFiles();

		// get configured language
		this.language = languageFileExists(plugin.getConfig().getString("language"));

		// get custom config handler for configured language language file
		this.messages = new ConfigAccessor(plugin, directoryName + File.separator + language + ".yml");
	}


	/** Send message to player
	 * 
	 * @param player		Player to message
	 * @param messageID		Identifier of message to send
	 */
	void sendPlayerMessage(Player player, String messageID) {

		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled",false)) {
			String message = messages.getConfig().getString("messages." + messageID + ".string");

			// set variables
			String playername = player.getName();
			String playerdisplayname = player.getDisplayName();
			String worldname = plugin.worldManager.getWorldName(player.getWorld());

			// strip colorcodes and special characters from variables
			String playernickname = ChatColor.stripColor(player.getPlayerListName());
			String itemmaterial = player.getInventory().getItemInMainHand()
					.getType().toString().toLowerCase().replace('_', ' ');

			// perform variable substitutions
			if (message.contains("%")) {
				message = StringUtil.replace(message, "%itemmaterial%", itemmaterial);
				message = StringUtil.replace(message, "%playername%", playername);
				message = StringUtil.replace(message, "%playerdisplayname%", playerdisplayname);
				message = StringUtil.replace(message, "%playernickname%", playernickname);
				message = StringUtil.replace(message, "%worldname%", worldname);
			}

			// send message to player
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
		}
	}
	
	
	/**
	 * Install language resource files from plugin jar
	 */
	private void installLocalizationFiles() {

		List<String> filelist = new ArrayList<String>();

		// get the absolute path to this plugin as URL
		URL pluginURL = plugin.getServer().getPluginManager().getPlugin(plugin.getName()).getClass().getProtectionDomain().getCodeSource().getLocation();

		// read files contained in jar, adding language/*.yml files to list
		ZipInputStream zip;
		try {
			zip = new ZipInputStream(pluginURL.openStream());
			while (true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null) {
					break;
				}
				String name = e.getName();
				if (name.startsWith(directoryName + '/') && name.endsWith(".yml")) {
					filelist.add(name);
				}
			}
		} catch (IOException e1) {
			plugin.getLogger().warning("Could not read language files from jar.");
		}

		// iterate over list of language files and install from jar if not already present
		for (String filename : filelist) {
			// this check prevents a warning message when files are already installed
			if (new File(plugin.getDataFolder() + File.separator + filename.replace('/', File.separatorChar)).exists()) {
				continue;
			}
			plugin.getLogger().info("Installing localization file:  " + filename);
			plugin.saveResource(filename, false);
		}
	}


	private String languageFileExists(final String language) {

		// check if localization file for configured language exists, if not then fallback to en-US
		File languageFile = new File(plugin.getDataFolder()
				+ File.separator + directoryName
				+ File.separator + language + ".yml");

		if (languageFile.exists()) {
			return language;
		}
		plugin.getLogger().info("Language file " + language + ".yml does not exist. Defaulting to en-US.");
		return "en-US";
	}


	/**
	 * Reload localized messages
	 */
	final void reload() {

		// reinstall message files if necessary
		installLocalizationFiles();

		// get currently configured language
		String newLanguage = languageFileExists(plugin.getConfig().getString("language"));

		// if configured language has changed, instantiate new messages object
		if (!newLanguage.equals(this.language)) {
			this.messages = new ConfigAccessor(plugin, directoryName + File.separator + newLanguage + ".yml");
			this.language = newLanguage;
			plugin.getLogger().info("New language " + this.language + " selected.");
		}

		// reload language file
		messages.reloadConfig();
	}

}
