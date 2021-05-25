package de.ancash.ieconomy;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.ILibrary;
import de.ancash.datastructures.maps.CompactMap;
import de.ancash.misc.MathsUtils;
import de.ancash.sockets.storage.StorageAction;
import de.ancash.sockets.storage.StorageCallback;
import de.ancash.sockets.storage.StoragePacket;
import de.ancash.sockets.storage.StorageResult;

class MyEconomy extends JavaPlugin{

	private static final String PATH = "IEconomy/player";
	
	CompactMap<UUID, Double> balance = new CompactMap<>();
	CompactMap<UUID, Double> bank = new CompactMap<>();
	
	public final void update(UUID uuid) throws IOException {
		StoragePacket sp = new StoragePacket(StorageAction.GET_DOUBLE, PATH + "/" + uuid, getBalancePath(), null, new StorageCallback() {
			
			@Override
			public void call(StorageResult arg0) {
				updateBalance(uuid, (double) arg0.getValue());
			}
		});
		ILibrary.getInstance().send(sp.getPacket());
		sp = new StoragePacket(StorageAction.GET_DOUBLE, PATH + "/" + uuid, getBankPath(), null, new StorageCallback() {
			
			@Override
			public void call(StorageResult arg0) {
				updateBank(uuid, (double) arg0.getValue());
			}
		});
		ILibrary.getInstance().send(sp.getPacket());
	}
	
	public String getBankPath() {
		return "bank";
	}

	public String getBalancePath() {
		return "balance";
	}
	
	private synchronized void updateBalance(UUID uuid, double val) {
		balance.put(uuid, val);
	}
	
	private synchronized void updateBank(UUID uuid, double val) {
		bank.put(uuid, val);
	}
	
	public boolean isCached(UUID id) throws IOException {
		if(!balance.containsKey(id)) {
			update(id);
			return false;
		} else {
			return true;
		}
	}
	
	@SuppressWarnings("deprecation")
	public double getBalance(String player) throws IOException {
		return getBalance(Bukkit.getOfflinePlayer(player));
	}
	
	public double getBalance(OfflinePlayer off) throws IOException {
		return getBalance(off.getUniqueId());
	}
	
	public double getBalance(UUID player) throws IOException {
		if(!balance.containsKey(player)) {
			update(player);
		}
		while(!balance.containsKey(player)) 
			Thread.yield();
		
		return balance.get(player);
	}
	
	@SuppressWarnings("deprecation")
	public double getBank(String player) throws IOException {
		return getBank(Bukkit.getOfflinePlayer(player));
	}
	
	public double getBank(OfflinePlayer off) throws IOException {
		return getBank(off.getUniqueId());
	}
	
	public double getBank(UUID id) throws IOException {
		if(!bank.containsKey(id)) update(id);
		while(!bank.containsKey(id)) 
			Thread.yield();
		return bank.get(id);
	}
	
	public void setBalance(OfflinePlayer p, double amount) throws IOException {
		setBalance(p.getUniqueId(), amount);
	}
	
	@SuppressWarnings("deprecation")
	public void setBalance(String player, double amount) throws IOException {
		setBalance(Bukkit.getOfflinePlayer(player), amount);
	}
	
	//withdraw
	@SuppressWarnings("deprecation")
	public void withdrawPlayer(String player, double amount) throws IOException {
		withdrawPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void withdrawPlayer(OfflinePlayer off, double amount) throws IOException {
		withdrawPlayer(off.getUniqueId(), amount);
	}
	
	@SuppressWarnings("deprecation")
	public void withdrawBank(String player, double amount) throws IOException {
		withdrawBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void withdrawBank(OfflinePlayer off, double amount) throws IOException {
		withdrawBank(off.getUniqueId(), amount);
	}
	
	
	//deposit
	@SuppressWarnings("deprecation")
	public void depositPlayer(String player, double amount) throws IOException {
		depositPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void depositPlayer(OfflinePlayer off, double amount) throws IOException {
		depositPlayer(off.getUniqueId(), amount);
	}
	
	@SuppressWarnings("deprecation")
	public void depositBank(String player, double amount) throws IOException {
		depositBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void depositBank(OfflinePlayer off, double amount) throws IOException {
		depositBank(off.getUniqueId(), amount);
	}
	
	public void withdrawBank(UUID off, double amount) throws IOException {
		StoragePacket sp = new StoragePacket(StorageAction.ADD_TO_DOUBLE, PATH + "/" + off, getBankPath(), -amount);
		ILibrary.getInstance().send(sp.getPacket());
		update(off);
	}
	
	public void withdrawPlayer(UUID off, double amount) throws IOException {
		StoragePacket sp = new StoragePacket(StorageAction.ADD_TO_DOUBLE, PATH + "/" + off, getBalancePath(), -amount);
		ILibrary.getInstance().send(sp.getPacket());
		update(off);
	}
	
	public void depositPlayer(UUID off, double amount) throws IOException {
		StoragePacket sp = new StoragePacket(StorageAction.ADD_TO_DOUBLE, PATH + "/" + off, getBalancePath(), MathsUtils.round(amount, 2));
		ILibrary.getInstance().send(sp.getPacket());
		update(off);
	}
	
	public void depositBank(UUID off, double amount) throws IOException {
		StoragePacket sp = new StoragePacket(StorageAction.ADD_TO_DOUBLE, PATH + "/" + off, getBankPath(), MathsUtils.round(amount, 2));
		ILibrary.getInstance().send(sp.getPacket());
		update(off);
	}
	
	public void setBalance(UUID uuid, double value) throws IOException {
		StoragePacket sp = new StoragePacket(StorageAction.SET_DOUBLE, PATH + "/" + uuid, getBalancePath(), MathsUtils.round(value, 2));
		ILibrary.getInstance().send(sp.getPacket());
		update(uuid);
	}
	
	public void setBank(UUID uuid, double value) throws IOException {
		StoragePacket sp = new StoragePacket(StorageAction.SET_DOUBLE, PATH + "/" + uuid, getBankPath(), MathsUtils.round(value, 2));
		ILibrary.getInstance().send(sp.getPacket());
		update(uuid);
	}
	
	public String format(double d) {
		StringBuilder sb = new StringBuilder();
		sb = sb.reverse();
		String value = String.format("%.2f", d);
		for(int i = value.length() - 1; i > value.length() - 4; i--) {
			sb.append(value.charAt(i));
		}
		int point = 0;
		for(int i = value.length() - 4; i>= 0; i--) {
			sb.append(value.charAt(i));
			point++;
			if(point > 2) {
				if(i != 0) sb.append(".");
				point = 0;
			}
		}
		return sb.reverse().toString();
	}
}
