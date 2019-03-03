package com.winterhaven_mc.creativenono.listeners;

import com.winterhaven_mc.creativenono.PluginMain;
import com.winterhaven_mc.creativenono.messages.MessageId;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


/**
 * Implements event listener for <code>CreativeNoNo</code> events.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public class EventListener implements Listener {

	private PluginMain plugin;
	
	private HashMap<UUID,Boolean> pickupNotified = new HashMap<>();


	/**
	 * constructor method for <code>EventListener</code> class
	 * 
	 * @param	plugin		A reference to this plugin's main class
	 */
	public EventListener(PluginMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/** prevent drops for creative players
	 * 
	 * @param	event	PlayerDropItemEvent
	 */
	@EventHandler
	void onPlayerDrop(PlayerDropItemEvent event) {

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
		if (!plugin.getConfig().getBoolean("worlds." + worldname + ".prevent-drops",true)) {
			return;
		}
		if (player.hasPermission("creativenono.bypass.drops")) {
			return;
		}
		if (plugin.getConfig().getBoolean("destroy-on-drop", false)) {
			event.getItemDrop().remove();
			if (plugin.getConfig().getBoolean("play-sound",true)) {
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			}
			plugin.messageManager.sendMessage(player, MessageId.DESTROY_ON_DROP);
			return;
		}
		event.setCancelled(true);
		plugin.messageManager.sendMessage(player,MessageId.PREVENT_DROPS);
		playDeniedSound(player);			
	}


	/** prevent death drops for creative players
	 * 
	 * @param	event	PlayerDeathEvent
	 */
	@EventHandler
	void onPlayerDeath(PlayerDeathEvent event) {
		
		final Player player = event.getEntity();
		final String worldname = player.getWorld().getName();

		// if player world is not enabled, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}

		if (player.getGameMode() != GameMode.CREATIVE) {
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
	 */
	@EventHandler
	void onPickup(final EntityPickupItemEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if event entity is not player, do nothing and return
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		// cast entity to player
		final Player player = (Player)event.getEntity();

		// get player world name
		final String worldName = player.getWorld().getName();

		// if player is not in creative mode, do nothing and return
		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}

		// if player world is not enabled, do nothing and return
		if (!plugin.worldManager.isEnabled(worldName)) {
			return;
		}

		// if prevent-pickup is not enabled in world specific configuration, do nothing and return
		if (!plugin.getConfig().getBoolean("worlds." + worldName + ".prevent-pickup")) {
			return;
		}

		// if player has bypass permission, do nothing and return
		if (player.hasPermission("creativenono.pickup.bypass")) {
			return;
		}

		// cancel event
		event.setCancelled(true);

		if (!pickupNotified.containsKey(player.getUniqueId())) {
			plugin.messageManager.sendMessage(player,MessageId.PREVENT_PICKUP);
			pickupNotified.put(player.getUniqueId(), true);

			new BukkitRunnable() {

				@Override
				public void run() {
					pickupNotified.remove(player.getUniqueId());
				}

			}.runTaskLater(this.plugin, plugin.getConfig().getInt("pickup-notify-interval",20)*20);
		}
	}


	/** prevent container access for creative players
	 * 
	 * @param	event	PlayerInteractEvent
	 */
	@EventHandler
	void onPlayerInteract(PlayerInteractEvent event) {
		
		final Player player = event.getPlayer();
		final String worldName = player.getWorld().getName();
		final Block block = event.getClickedBlock();
//		final ItemStack playerItem = event.getItem();

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// if player is not in creative mode, do nothing and return
		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}
		
		// if player world is disabled in config, do nothing and return
		if (worldDisabled(worldName)) {
			return;
		}
		
//		// if player item is a spawn egg, check if spawn eggs are disabled in world
//		if (playerItem != null && playerItem.getType().equals(Material.MONSTER_EGG)) {
//
//			// if spawn eggs are not disabled in world, do nothing and return
//			if (!plugin.getConfig().getBoolean("worlds." + worldName + ".prevent-spawn-eggs",true)) {
//				return;
//			}
//
//			// if player has override permission, do nothing and return
//			if (player.hasPermission("creativenono.bypass.spawnegg")) {
//				return;
//			}
//
//			// cancel event, send player message and return
//			event.setCancelled(true);
//			plugin.messageManager.sendPlayerMessage(player, "prevent-spawn-eggs");
//			playDeniedSound(player);
//			return;
//		}
		
		// if block clicked is a container
		if (block != null && isContainerBlock(block)) {
			
			// if player tried to open container
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				
				// if creative container access is not disabled in world, do nothing and return
				if (!plugin.getConfig().getBoolean("worlds." + worldName + ".prevent-container-access",true)) {
					return;
				}

				// if player has override permission, do nothing and return
				if (player.hasPermission("creativenono.bypass.containers")) {
					return;
				}
				
				// cancel event, send player message and return
				event.setCancelled(true);
				plugin.messageManager.sendMessage(player,MessageId.PREVENT_CONTAINER_ACCESS);
				playDeniedSound(player);
			}
		}
	}


	/** prevent placing blacklisted items by creative players
	 * 
	 * @param	event	BlockPlaceEvent
	 */
	@EventHandler
	void onBlockPlace(BlockPlaceEvent event) {

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
			plugin.messageManager.sendMessage(player, MessageId.BLACKLIST, material);
			playDeniedSound(player);
		}
	}


	/** prevent breaking blocks at layer 0
	 * 
	 * @param	event	BlockBreakEvent
	 */
	@EventHandler
	void onBlockBreak(BlockBreakEvent event) {
		
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
		if (!plugin.getConfig().getBoolean("worlds." + worldname + ".layer-zero-protect", true)) {
			return;
		}
		if (player.hasPermission("creativenono.bypass.layerzero")) {
			return;
		}
		if (event.getBlock().getLocation().getBlockY() == 0) {
			event.setCancelled(true);
			plugin.messageManager.sendMessage(player,MessageId.LAYER_ZERO_PROTECT);
			playDeniedSound(player);
		}
	}
	
	
	
	/**
	 * Limit the number of creatures that can be spawned with spawn eggs in an area
	 * @param event		CreatureSpawnEvent
	 */
	@EventHandler
	void onCreatureSpawn(CreatureSpawnEvent event) {
		
		// get spawn event reason
		CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
		
		// if creature spawn was not caused by spawn egg, do nothing and return
		if (!reason.equals(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)) {
			return;
		}
		
		// get world
		String worldName = event.getLocation().getWorld().getName();
		
		// if spawn egg limits are not enabled for world, do nothing and return
		if (!plugin.getConfig().getBoolean("worlds." + worldName + ".limit-spawn-eggs",true)) {
			return;
		}

		int radius = plugin.getConfig().getInt("worlds." + worldName + ".spawn-egg-radius",64);
		int limit = plugin.getConfig().getInt("worlds." + worldName + ".spawn-egg-limit",10);
		int entityCount = 0;
		
		List<Entity> entityList = event.getEntity().getNearbyEntities(radius, radius, radius);
		
		EntityType entityType = event.getEntityType();
		
		for (Entity entity : entityList) {
			if (entity.getType().equals(entityType)) {
				entityCount++;
			}
		}
		
		if (entityCount >= limit) {
			event.setCancelled(true);
		}
	}


	/** check if block is a container
	 *
	 * @param	block block to check if container
	 * @return	{@code true} if block is a container, {@code false} if not
	 */
	private boolean isContainerBlock(Block block) {
		return block.getState() instanceof Container;
	}


	/** check if block is blacklisted
	 * 
	 * @param	material material to check in blacklist
	 * @return	boolean
	 */
	private boolean blockBlacklisted(Material material) {
		List<String> blacklist = plugin.getConfig().getStringList("blacklist");
		return blacklist.contains(material.toString());
	}
	

	/** check if world is in disabled list
	 * 
	 * @param worldname name of world to check in list
	 * @return	boolean
	 */
	private boolean worldDisabled(String worldname) {
		List<String> disabledworlds = plugin.getConfig().getStringList("disabled-worlds");
		return disabledworlds.contains(worldname);
	}
	
	private void playDeniedSound(Player player) {
		if (plugin.getConfig().getBoolean("play-sound")) {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
		}
	}
	
}
