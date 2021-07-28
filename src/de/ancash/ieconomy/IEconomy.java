package de.ancash.ieconomy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import de.ancash.ieconomy.commands.EcoCommand;

import net.milkbowl.vault.economy.Economy;

public class IEconomy extends MyEconomy{

	private static IEconomy instance;
	private VaultAPI vaultInstance;
	private ExecutorService executor;		
	
	public void onEnable() {
		instance = this;
		executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
			hookVault();
		    System.out.println("Hooked into Vault!");
		}
		getCommand("eco").setExecutor(new EcoCommand(executor));
	}
	
	@Override
	public void onDisable() {
		executor.shutdownNow();	
	}
	
	public void hookVault() {
		vaultInstance = new VaultAPI();
	    Bukkit.getServicesManager().register(Economy.class, vaultInstance, Bukkit.getPluginManager().getPlugin("Vault"), ServicePriority.Normal);
	}
	
	public static IEconomy getInstance() {
		return instance;
	}
}
