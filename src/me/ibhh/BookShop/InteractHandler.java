package me.ibhh.BookShop;

import me.ibhh.BookShop.logger.LoggerUtility;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class InteractHandler {

	private BookShop plugin;
	private SignHandler signHandler;

	/**
	 * Konstruktor of InteractHandler
	 * 
	 * @param pl
	 */
	public InteractHandler(BookShop pl) {
		plugin = pl;
		signHandler = new SignHandler(pl);
	}

	/**
	 * Handles playerinteracts
	 * 
	 * @param event
	 */
	public void InteracteventHandler(PlayerInteractEvent event) {
		if (!plugin.toggle) {
			Player p = event.getPlayer();
			if (plugin.config.debug) {
				plugin.Logger("A interact Event dected by player: " + p.getName(), "Debug");
			}
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (plugin.getConfig().getBoolean("LEFT_CLICK_buy")) {
					try {
						LeftInteract(event);
					} catch (Exception e) {
						plugin.getReportHandler().report(3335, "Error on leftInteract", e.getMessage(), "InteractHandler", e);
					}
				}
			} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				try {
					if (event.hasBlock()) {
						if (plugin.ListenerShop.chestHandler.isChest(event.getClickedBlock())) {
							plugin.Logger("Is chest (interact)", "Debug");
							if (plugin.ListenerShop.chestHandler.isProtectedChest(event.getClickedBlock(), p, "BookShop.admin") == -1) {
								plugin.PlayerLogger(p, plugin.getConfig().getString("Shop.error.notyourshop." + plugin.config.language), "Warning");
								event.setCancelled(true);
							} else if (plugin.ListenerShop.chestHandler.isProtectedChest(event.getClickedBlock(), p, "BookShop.admin") == 1) {
								plugin.ListenerShop.ChestViewers.put(p, (Chest) event.getClickedBlock().getState());
								plugin.ListenerShop.NewspapersViewers.put(p, plugin.ListenerShop.chestHandler.isNewspaper(event.getClickedBlock()));
							}
						}
					}
				} catch (Exception e) {
					plugin.getReportHandler().report(3336, "Error on RightInteract", e.getMessage(), "InteractHandler", e);
				}
				if (!plugin.getConfig().getBoolean("LEFT_CLICK_buy")) {
					try {
						LeftInteract(event);
					} catch (Exception e) {
						plugin.getReportHandler().report(3335, "Error on leftInteract", e.getMessage(), "InteractHandler", e);
					}
				}
			}
		}
	}

	/**
	 * Manages leftklickinteracts
	 * 
	 * @param event
	 */
	public void LeftInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if (plugin.config.debug) {
			plugin.Logger("A left interact Event dected by player: " + p.getName(), "Debug");
		}
		if (!event.hasBlock()) {
			return;
		}
		Block eventblock = event.getClickedBlock();
		if (!(eventblock.getState() instanceof Sign)) {
			return;
		}
		if (!p.isSneaking()) {
			plugin.Logger("Player NOT Sneaking!", "Debug");
			Sign s = (Sign) event.getClickedBlock().getState();
			String[] line = s.getLines();
			if (plugin.config.debug) {
				plugin.Logger("Checking first line!", "Debug");
			}
			if (line[0].equalsIgnoreCase(plugin.SHOP_configuration.getString("FirstLineOfEveryShop"))) {
				signHandler.LinksKlick(event, line, p, s);
			}
		} else if (p.isSneaking()) {
			plugin.Logger("Player sneaking!", "Debug");
			Sign s = (Sign) event.getClickedBlock().getState();
			String[] line = s.getLines();
			if (plugin.config.debug) {
				plugin.Logger("Checking first line!", "Debug");
			}
			if (line[0].equalsIgnoreCase(plugin.SHOP_configuration.getString("FirstLineOfEveryShop"))) {
				Chest chest = null;
				try {
					chest = (Chest) s.getBlock().getRelative(BlockFace.DOWN).getState();
				} catch (Exception e) {
					return;
				}
				if (chest != null) {
					if (chest.getInventory().contains(Material.WRITTEN_BOOK)) {
						int Slot = chest.getInventory().first(Material.WRITTEN_BOOK);
						ItemStack item = chest.getInventory().getItem(Slot);
						if (item != null) {
							BookMeta bm = (BookMeta) item.getItemMeta();
							ItemStack loadedBook = BookLoader.load(plugin, bm.getAuthor(), bm.getTitle());
							BookLoader.save(plugin, loadedBook);
							plugin.PlayerLogger(p, String.format(plugin.getConfig().getString("Shop.success.bookselled." + plugin.config.language), bm.getTitle(), bm.getAuthor()), "");
							plugin.PlayerLogger(p, String.format(plugin.getConfig().getString("Shop.success.bookselled2." + plugin.config.language), plugin.soldbooks.get(item.hashCode())), "");
							plugin.PlayerLogger(p, String.format(plugin.getConfig().getString("Shop.success.bookselled3." + plugin.config.language), bm.getPages().size()), "");
						} else {
							plugin.getLoggerUtility().log(p, "An unknown error occurred!", LoggerUtility.Level.ERROR);
							return;
						}
					}
				}
			}
		}
	}
}
