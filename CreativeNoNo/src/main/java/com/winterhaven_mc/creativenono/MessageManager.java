package com.winterhaven_mc.creativenono;

import com.winterhaven_mc.util.ConfigAccessor;
import com.winterhaven_mc.util.LanguageManager;
import com.winterhaven_mc.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

class MessageManager {
	
	private final PluginMain plugin;
	private ConfigAccessor messages;
	private LanguageManager languageManager;

	/**
	 * constructor method for MessageManager class
	 * 
	 * @param	plugin		reference to main class
	 */
	MessageManager(PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// instantiate language manager
		languageManager = new LanguageManager(plugin);

		// get custom config handler for configured language file
		this.messages = new ConfigAccessor(plugin, languageManager.getFileName());
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
	 * Reload localized messages
	 */
	final void reload() {
		this.messages = languageManager.reload(messages);
	}

}
