package com.winterhaven_mc.creativenono;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

class CommandManager implements CommandExecutor {

	// reference to main class
	private PluginMain plugin;
	
	CommandManager(PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// register this class as command executor for nono command
		plugin.getCommand("nono").setExecutor(this);

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

		// reload command
		if (subcmd.equalsIgnoreCase("reload") && sender.hasPermission("creativenono.reload")) {

			// reload config
			plugin.reloadConfig();

			// reload messages
			plugin.messageManager.reload();

			// send player message
			sender.sendMessage("CreativeNoNo config reloaded!");

			return true;
		}
		return false;
	}
}	
