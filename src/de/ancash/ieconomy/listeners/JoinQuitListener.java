package de.ancash.ieconomy.listeners;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.ancash.ieconomy.IEconomy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JoinQuitListener implements Listener{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if(!IEconomy.getInstance().exists(e.getPlayer())) {
			IEconomy.getInstance().create(e.getPlayer().getUniqueId() + "");
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		IEconomy.getInstance().pushAll(e.getPlayer());
	}
}
