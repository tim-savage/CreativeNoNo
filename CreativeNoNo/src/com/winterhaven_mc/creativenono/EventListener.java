package com.winterhaven_mc.creativenono;

import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * Implements event listener for <code>CreativeNoNo</code> events.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */public class EventListener implements Listener {

	private CreativeNoNoMain plugin;
	
	private HashMap<String,Boolean> pickupnotified = new HashMap<String, Boolean>();

	/**
	 * constructor method for <code>EventListener</code> class
	 * 
	 * @param	plugin		A reference to this plugin's main class
	 */
	public EventListener(CreativeNoNoMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	/** prevent drops for creative players
	 * 
	 * @param	event	PlayerDropItemEvent
	 * @return	void
	 */
	@EventHandler
	public void onPlayerDrop(PlayerDropItemEvent event) {

		final Player player = (Player) event.getPlayer();
		final String worldname = player.getWorld().getName();

		if (event.isCancelled()) {
			return;
		}
		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}
		if (worldDisabled(worldname)) {
			return;
		}
		if (!plugin.getConfig().getBoolean("worlds." + worldname + ".prevent-drops",true)) {
			return;
		}
		if (player.hasPermission("creativenono.drops.override")) {
			return;
		}
		if (plugin.getConfig().getBoolean("destroy-on-drop", false)) {
			event.getItemDrop().remove();
			if (plugin.getConfig().getBoolean("play-sound",true)) {
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
			}
			plugin.messagemanager.sendPlayerMessage(player,"destroy-on-drop");
			return;
		}
		event.setCancelled(true);
		plugin.messagemanager.sendPlayerMessage(player,"prevent-drops");
		playDeniedSound(player);			
	}

	/** prevent death drops for creative players
	 * 
	 * @param	event	PlayerDeathEvent
	 * @return	void
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		final Player player = event.getEntity();
		final String worldname = player.getWorld().getName();
		
		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}
		if (worldDisabled(worldname)) {
			return;
		}
		if (!plugin.getConfig().getBoolean("worlds." + worldname + ".prevent-deathdrops",true)) {
			return;
		}
		if (player.hasPermission("creativenono.deathdrops.bypass")) {
			return;
		}
		event.getDrops().clear();
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
	}

	/** prevent item pickup for creative players
	 * 
	 * @param	event	PlayerPickupItemEvent
	 * @return	void
	 */
	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		final Player player = event.getPlayer();
		final String worldname = player.getWorld().getName();
		if (event.isCancelled()) {
			return;
		}
		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}
		if (worldDisabled(worldname)) {
			return;
		}
		if (!plugin.getConfig().getBoolean("worlds." + worldname + ".prevent-pickup",true)) {
			return;
		}
		if (player.hasPermission("creativenono.pickup.bypass")) {
			return;
		}
		event.setCancelled(true);
		if (!pickupnotified.containsKey(player.getName())) {
			plugin.messagemanager.sendPlayerMessage(player,"prevent-pickup");
			pickupnotified.put(player.getName(), true);

			new BukkitRunnable() {

				@Override
				public void run() {
					pickupnotified.remove(player.getName());
				}

			}.runTaskLater(this.plugin, plugin.getConfig().getInt("pickup-notify-interval",20)*20);

		}
	}

	/** prevent container access for creative players
	 * 
	 * @param	event	PlayerInteractEvent
	 * @return	void
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		final Player player = event.getPlayer();
		final String worldname = player.getWorld().getName();
		final Block block = event.getClickedBlock();

		if (event.isCancelled() || block == null) {
			return;
		}
		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}
		if (worldDisabled(worldname)) {
			return;
		}
		if (!plugin.getConfig().getBoolean("worlds." + worldname + ".prevent-container-access",true)) {
			return;
		}
		if (player.hasPermission("creativenono.bypass.containers")) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isContainerBlock(block.getType())) {
			event.setCancelled(true);
			plugin.messagemanager.sendPlayerMessage(player,"prevent-container-access");
			playDeniedSound(player);
		}
	}

	/** prevent placing blacklisted items by creative players
	 * 
	 * @param	event	BlockPlaceEvent
	 * @return	void
	 */
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {

		final Player player = event.getPlayer();
		final String worldname = player.getWorld().getName();
		final Material material = event.getBlockPlaced().getType();
		
		if (event.isCancelled()) {
			return;
		}
		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}
		if (worldDisabled(worldname)) {
			return;
		}
		if (!plugin.getConfig().getBoolean("worlds." + worldname + ".blacklist-enabled",true)) {
			return;
		}
		if (player.hasPermission("creativenono.bypass.blacklist")) {
			return;
		}
		if (blockBlacklisted(material)) {
			event.setCancelled(true);
			plugin.messagemanager.sendPlayerMessage(player,"blacklist");
			playDeniedSound(player);
		}
	}

	/** prevent breaking blocks at layer 0
	 * 
	 * @param	event	BlockBreakEvent
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		final Player player = event.getPlayer();
		final String worldname = player.getWorld().getName();
		
		if (event.isCancelled()) {
			return;
		}
		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}
		if (worldDisabled(worldname)) {
			return;
		}
		if (plugin.getConfig().getBoolean("worlds." + worldname + ".layer-zero-protect",true) != true) {
			return;
		}
		if (player.hasPermission("creativenono.bypass.layerzero")) {
			return;
		}
		if (event.getBlock().getLocation().getBlockY() == 0) {
			event.setCancelled(true);
			plugin.messagemanager.sendPlayerMessage(player,"layer-zero-protect");
			playDeniedSound(player);
		}
	}

	/** check if block is a container
	 * 
	 * @param	material	material to check if container block
	 * @return	boolean
	 */
	private boolean isContainerBlock(Material material) {
		switch(material) {
		case CHEST: return true;
		case ENDER_CHEST: return true;
		case DISPENSER: return true;
		case DROPPER: return true;
		case HOPPER: return true;
		case TRAPPED_CHEST: return true;
		case FURNACE: return true;
		case ENCHANTMENT_TABLE: return true;
		case ANVIL: return true;
		case STORAGE_MINECART: return true;
		case HOPPER_MINECART: return true;
		case POWERED_MINECART: return true;
		case BEACON: return true;
		case COMMAND: return true;
		case BREWING_STAND: return true;
		default: return false;
		}
	}

	/** check if block is blacklisted
	 * 
	 * @param	material material to check in blacklist
	 * @return	boolean
	 */
	private boolean blockBlacklisted(Material material) {
		List<String> blacklist = plugin.getConfig().getStringList("blacklist");
		if (blacklist.contains(material.toString())) {
			return true;
		}
		return false;
	}
	
	/** check if world is in disabled list
	 * 
	 * @param	worldname	name of world to check in list
	 * @return	boolean
	 */
	private boolean worldDisabled(String worldname) {
		List<String> disabledworlds = plugin.getConfig().getStringList("disabled-worlds");
		if (disabledworlds.contains(worldname)) {
			return true;
		}
		return false;
	}
	
	private void playDeniedSound(Player player) {
		if (plugin.getConfig().getBoolean("play-sound")) {
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
		}
	}
	
}

