package de.ancash.ieconomy;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import de.ancash.ieconomy.commands.EcoCommand;
import de.ancash.ieconomy.listeners.JoinQuitListener;
import de.ancash.ilibrary.ILibrary;
import de.ancash.ilibrary.misc.FileUtils;
import de.ancash.ilibrary.sockets.ChatClient;
import de.ancash.ilibrary.sockets.Packet;
import de.ancash.ilibrary.yaml.configuration.file.YamlFile;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

public class IEconomy extends MyEconomy{

	private static IEconomy instance;
	private YamlFile file;
	private de.ancash.ieconomy.VaultAPI vaultInstance;
	
	private Client chatClient;
	private int port;
	private String address;
	
	public void onEnable() {
		instance = this;
		
		if(!new File("plugins/IEconomy/config.yml").exists())
			try {
				FileUtils.copyInputStreamToFile(getResource("config.yml"), new File("plugins/IEconomy/config.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		file = new YamlFile(new File("plugins/IEconomy/config.yml"));
		try {
			file.load();
			file.save();
		} catch (InvalidConfigurationException | IOException e) {
			e.printStackTrace();
		}
		
		address = file.getString("address");
		port = file.getInt("port");
		
		
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
		    hookVault();
		    debug("Hooked into Vault!");
		}
			
		getCommand("eco").setExecutor(new EcoCommand());
		Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
		
		
		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			
			@Override
			public void run() {
				if(file.getBoolean("enableSockets")) {
					debug("Using Sockets!");
						
					if(!ILibrary.getInstance().isDefaultSocketRunning()) {
						debug("No Server Socket running on this Server!");
						debug("Trying to connect to with port " + port + " and address " + address);
						chatClient = new Client(address, port, "Economy");
					} else {
						debug("Server Socket running on this Server!");
						debug("Trying to connect to with port " + port + " and address " + address);
						chatClient = new Client(address, port, "Economy");
					}					
				} else {
					chatClient = null;
				}
			}
		}, 20);
		Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
			
			@Override
			public void run() {
				if(chatClient != null && chatClient.isActive()) {
					debug("Fetching all balances!");
					chatClient.send(new Packet("Economy", "economy pullall"));
					Bukkit.getScheduler().runTaskLater(getInstance(), new Runnable() {
						
						@Override
						public void run() {
							chatClient.wait = false;
						}
					}, 20);
				}
			}
		}, 40);
		
		try {
			init();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		Bukkit.getOnlinePlayers().forEach(p ->{
			if(!exists(p)) create(p.getUniqueId() + "");
		});
	}
	
	public void hookVault() {
		vaultInstance = new de.ancash.ieconomy.VaultAPI();
	    Bukkit.getServicesManager().register(net.milkbowl.vault.economy.Economy.class, vaultInstance, Bukkit.getPluginManager().getPlugin("Vault"), ServicePriority.Normal);
	}
	
	public void onDisable() {
		try {
			save();
		} catch (InvalidConfigurationException | IOException e) {
			e.printStackTrace();
		}
		chatClient.wait = true;
		chatClient.stop();
	}
	
	public void pushAll() {
		getAllUUIDs().forEach(uuid ->{
			pushAll(UUID.fromString(uuid));
		});
	}
	
	public void pushAll(UUID id) {
		push(id + ".balance", getBalance(id), getComment(id + ".balance"), chatClient);
		push(id + ".bank", getBank(id), getComment(id + ".bank"), chatClient);
	}
	
	public void pushAll(OfflinePlayer p) {
		debug("Pushed " + p.getName() + "'s Balances!");
		push(p.getUniqueId() + ".balance", getBalance(p), getComment(p.getUniqueId() + ".balance"), chatClient);
		push(p.getUniqueId() + ".bank", getBank(p), getComment(p.getUniqueId() + ".bank"), chatClient);
	}
	
	private void push(String path, double value, long timeStamp, ChatClient chatClient) {
		if(chatClient.isActive()) chatClient.send(new Packet("Economy", "updatebalance " + path + " " + value + " " + timeStamp));
	}	
	
	ChatClient getChatClient() {
		return chatClient;
	}
	
	public static IEconomy getInstance() {
		return instance;
	}
}
