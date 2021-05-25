package de.ancash.ieconomy;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import de.ancash.ieconomy.commands.EcoCommand;
import de.ancash.ieconomy.listeners.JoinListener;
import net.milkbowl.vault.economy.Economy;

public class IEconomy extends MyEconomy{

	private static IEconomy instance;
	private VaultAPI vaultInstance;
			
	public void onEnable() {
		instance = this;
		
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
			hookVault();
		    System.out.println("Hooked into Vault!");
		}
			
		Bukkit.getOnlinePlayers().stream().map(player -> player.getUniqueId()).forEach(arg0 -> {
			try {
				update(arg0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
		getCommand("eco").setExecutor(new EcoCommand());
	}
	
	public void hookVault() {
		vaultInstance = new VaultAPI();
	    Bukkit.getServicesManager().register(Economy.class, vaultInstance, Bukkit.getPluginManager().getPlugin("Vault"), ServicePriority.Normal);
	}
	
	public static IEconomy getInstance() {
		return instance;
	}
}
