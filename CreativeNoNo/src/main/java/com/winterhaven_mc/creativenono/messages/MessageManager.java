package com.winterhaven_mc.creativenono.messages;

import com.winterhaven_mc.creativenono.PluginMain;
import com.winterhaven_mc.util.AbstractMessageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;


public class MessageManager extends AbstractMessageManager {


	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// call super class constructor
		//noinspection unchecked
		super(plugin, MessageId.class);
	}


	@Override
	protected Map<String,String> getDefaultReplacements(CommandSender recipient) {

		Map<String,String> replacements = new HashMap<>();

		replacements.put("%PLAYER_NAME%", ChatColor.stripColor(recipient.getName()));

		replacements.put("%ITEM_MATERIAL%", "material");

		if (recipient instanceof Player) {
			Player player = (Player)recipient;
			replacements.put("%WORLD_NAME%", ChatColor.stripColor(getWorldName(recipient)));
			replacements.put("%LOC_X%", String.valueOf(player.getLocation().getBlockX()));
			replacements.put("%LOC_Y%", String.valueOf(player.getLocation().getBlockY()));
			replacements.put("%LOC_Z%", String.valueOf(player.getLocation().getBlockZ()));
		}
		else {
			replacements.put("%WORLD_NAME%", ChatColor.stripColor(plugin.getServer().getWorlds().get(0).getName()));
			replacements.put("%LOC_X%", "0");
			replacements.put("%LOC_Y%", "0");
			replacements.put("%LOC_Z%", "0");
		}

		return replacements;
	}


	/**
	 * Send message to recipient
	 * @param recipient the recipient to whom to send a message
	 * @param messageId the message identifier
	 */
	public void sendMessage(final CommandSender recipient, final MessageId messageId) {

		// get default replacement map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Send message to recipient
	 * @param recipient the recipient to whom to send a message
	 * @param messageId the message identifier
	 */
	public void sendMessage(final CommandSender recipient, final MessageId messageId, Material material) {

		// get default replacement map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		replacements.put("%MATERIAL%",material.toString());

		// send message
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}

}
