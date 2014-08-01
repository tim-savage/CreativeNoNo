package com.winterhaven_mc.creativenono;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {

	private CreativeNoNoMain plugin; // pointer to your main class, unrequired if you don't need methods from the main class
	
	public CommandHandler(CreativeNoNoMain plugin) {
		this.plugin = plugin;
	}

	/** command executor for CreativeNoNo
	 * 
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		int maxArgs = 1;
		
		if (args.length > maxArgs) {
	           sender.sendMessage(ChatColor.RED + "Too many arguments!");
	           return false;
		}
   		if (args.length < 1 && sender.hasPermission("creativenono.admin")) {
   			String versionString = plugin.getDescription().getVersion();
   			sender.sendMessage(ChatColor.AQUA + "[CreativeNoNo] Version " + versionString);
   			return true;
   		}
   		String subcmd = args[0];
	    if (subcmd.equalsIgnoreCase("reload") &&
		sender.hasPermission("creativenono.reload")) { // If the player typed '/nono reload' then do the following...
	    	String original_language = plugin.getConfig().getString("language", "en-US");
			plugin.reloadConfig();
			if (!original_language.equals(plugin.getConfig().getString("language","en-US"))) {
				plugin.messagemanager = new MessageManager(plugin);
			}
			else {
				plugin.messagemanager.reloadMessages();
			}
			sender.sendMessage("CreativeNoNo config reloaded!");
			return true;
		}
		return false;
	}
}	
