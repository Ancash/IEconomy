package de.ancash.ieconomy.listeners;

import java.io.IOException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.ancash.ieconomy.IEconomy;

public class JoinListener implements Listener{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) throws IOException {
		IEconomy.getInstance().update(event.getPlayer().getUniqueId());
	}
	
}
