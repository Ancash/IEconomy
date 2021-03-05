package de.ancash.ieconomy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.ilibrary.datastructures.sets.CompactSet;
import de.ancash.ilibrary.datastructures.tuples.Duplet;
import de.ancash.ilibrary.datastructures.tuples.Tuple;
import de.ancash.ilibrary.misc.NumberConversions;
import de.ancash.ilibrary.yaml.configuration.comments.CommentType;
import de.ancash.ilibrary.yaml.configuration.file.YamlFile;
import de.ancash.ilibrary.yaml.exceptions.InvalidConfigurationException;

class MyEconomy extends JavaPlugin{

	private YamlFile playerData;
	private static final HashMap<String, Duplet<Duplet<Double, Long>, Duplet<Double, Long>>> balances = new HashMap<String, Duplet<Duplet<Double,Long>,Duplet<Double,Long>>>();
	
	CompactSet<String> getAllUUIDs() {
		CompactSet<String> uuids = new CompactSet<String>();
		balances.keySet().forEach(uuid -> uuids.add(uuid));
		return uuids;
	}
	
	void init() throws IOException, InvalidConfigurationException {
		playerData = new YamlFile(new File("plugins/IEconomy/player.yml"));
		if(!playerData.exists()) {
			new File("plugins/IEconomy/player.yml").mkdirs();
			new File("plugins/IEconomy/player.yml").delete();
		}
		playerData.createNewFile(false);
		playerData.loadWithComments();
	}
	
	void save() throws InvalidConfigurationException, IOException {
		playerData.save();
		playerData.loadWithComments();
		for(String uuid : balances.keySet()) {
			Duplet<Double, Long> purse = balances.get(uuid).getFirst();
			Duplet<Double, Long> bank = balances.get(uuid).getSecond();
			setDoubleInFile(uuid + ".balance", purse.getFirst(), purse.getSecond());
			setDoubleInFile(uuid + ".bank", bank.getFirst(), bank.getSecond());
		}
		playerData.save();
	}
	
	void setDoubleInFile(String path, double value, long timestamp) {
		String comment = playerData.getComment(path, CommentType.SIDE);
		if(comment == null || Long.valueOf(comment) < timestamp) {
			playerData.set(path, value);
			playerData.setComment(path, timestamp + "", CommentType.SIDE);
		}
	}
	
	public void create(String uuid) {
		Duplet<Double, Long> bank = Tuple.of(playerData.getDouble(uuid + ".bank"), getCommentFromFile(uuid + ".bank"));
		Duplet<Double, Long> balance = Tuple.of(playerData.getDouble(uuid + ".balance"), getCommentFromFile(uuid + ".balance"));
		Duplet<Duplet<Double, Long>, Duplet<Double, Long>> all= Tuple.of(balance, bank);
		balances.put(uuid, all);
	}
	
	public void debug(String msg) {
		System.out.println("[IEconomy] " + msg);
	}
	
	long getCommentFromFile(String path) {
		String comment = playerData.getComment(path, CommentType.SIDE);
		if(comment == null) return 0;
		return Long.valueOf(comment);
	}
	
	long getComment(String path) {
		if(path.contains("balance")) return balances.get(path.split("\\.")[0]).getFirst().getSecond();
		if(path.contains("bank")) return balances.get(path.split("\\.")[0]).getSecond().getSecond();
		return 0;
	}
	
	public void update(String path, double value, long timestamp) {
		if(!check(path, timestamp)) {
			/*if(timestamp != 0) {
				debug("Received out dated data!");
			}*/
			return;
		}
		setDouble(path, value, false);
	}
	
	//bank stuff
	@SuppressWarnings("deprecation")
	public boolean exists(String player) {
		return exists(Bukkit.getOfflinePlayer(player));
	}
	
	public boolean exists(OfflinePlayer p) {
		return p != null && balances.containsKey(p.getUniqueId() + "");
	}
	
	@SuppressWarnings("deprecation")
	public double getBalance(String player) {
		return getBalance(Bukkit.getOfflinePlayer(player));
	}
	
	public double getBalance(UUID player) {
		return balances.get(player + "").getFirst().getFirst();
	}
	
	public double getBalance(OfflinePlayer off) {
		return balances.get(off.getUniqueId() + "").getFirst().getFirst();
	}
	
	@SuppressWarnings("deprecation")
	public double getBank(String player) {
		return getBank(Bukkit.getOfflinePlayer(player));
	}
	
	public double getBank(UUID id) {
		return balances.get(id + "").getSecond().getFirst();
	}
	
	public double getBank(OfflinePlayer off) {
		return balances.get(off.getUniqueId() + "").getSecond().getFirst();
	}
	
	
	@SuppressWarnings("deprecation")
	public void setBalance(String player, double amount) {
		setBalance(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void setBalance(UUID player, double amount) {
		setDouble(player + ".balance", amount, false);
		IEconomy.getInstance().pushAll(player);
	}
	
	public void setBalance(OfflinePlayer p, double amount) {
		setDouble(p.getUniqueId().toString() + ".balance", amount, false);
		IEconomy.getInstance().pushAll(p);
	}
	
	@SuppressWarnings("deprecation")
	public void setBank(String player, double amount) {
		setBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void setBank(UUID p, double amount) {
		setDouble(p + ".bank", amount, false);
		IEconomy.getInstance().pushAll(p);
	}
	
	public void setBank(OfflinePlayer p, double amount) {
		setDouble(p.getUniqueId().toString() + ".bank", amount, false);
		IEconomy.getInstance().pushAll(p);
	}
	
	//withdraw
	@SuppressWarnings("deprecation")
	public void withdrawPlayer(String player, double amount) {
		withdrawPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void withdrawPlayer(UUID off, double amount) {
		setDouble(off + ".balance", getBalance(off) - amount, false);
		IEconomy.getInstance().pushAll(off);
	}
	
	public void withdrawPlayer(OfflinePlayer off, double amount) {
		setDouble(off.getUniqueId().toString() + ".balance", getBalance(off) - amount, false);
		IEconomy.getInstance().pushAll(off);
	}
	
	@SuppressWarnings("deprecation")
	public void withdrawBank(String player, double amount) {
		withdrawBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void withdrawBank(UUID off, double amount) {
		setDouble(off + ".bank", getBank(off) - amount, false);
		IEconomy.getInstance().pushAll(off);
	}
	
	public void withdrawBank(OfflinePlayer off, double amount) {
		setDouble(off.getUniqueId().toString() + ".bank", getBank(off) - amount, false);
		IEconomy.getInstance().pushAll(off);
	}
	
	
	//deposit
	@SuppressWarnings("deprecation")
	public void depositPlayer(String player, double amount) {
		depositPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void depositPlayer(UUID off, double amount) {
		setDouble(off + ".balance", getBalance(off) + amount, false);
		IEconomy.getInstance().pushAll(off);
	}
	
	public void depositPlayer(OfflinePlayer off, double amount) {
		setDouble(off.getUniqueId().toString() + ".balance", getBalance(off) + amount, false);
		IEconomy.getInstance().pushAll(off);
	}
	
	@SuppressWarnings("deprecation")
	public void depositBank(String player, double amount) {
		depositBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public void depositBank(UUID off, double amount) {
		setDouble(off + ".bank", getBank(off) + amount, false);
		IEconomy.getInstance().pushAll(off);
	}
	
	public void depositBank(OfflinePlayer off, double amount) {
		setDouble(off.getUniqueId().toString() + ".bank", getBank(off) + amount, false);
		IEconomy.getInstance().pushAll(off);
	}
	
	//misc
	private void setDouble(String path, double value, boolean b) {
		long now = System.currentTimeMillis();
		if(!check(path, now)) return;
		if(path.contains("balance")) {
			balances.get(path.split("\\.")[0]).getFirst().setFirst(NumberConversions.round(value, 2));
			balances.get(path.split("\\.")[0]).getFirst().setSecond(now);
		} else if(path.contains("bank")) {
			balances.get(path.split("\\.")[0]).getSecond().setFirst(NumberConversions.round(value, 2));
			balances.get(path.split("\\.")[0]).getSecond().setSecond(now);
		}
		if(b) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					OfflinePlayer off = Bukkit.getOfflinePlayer(UUID.fromString(path.split("\\.")[0]));
					if(!off.isOnline()) IEconomy.getInstance().pushAll(off);
				}
			}.runTaskAsynchronously(IEconomy.getInstance());
		}
	}
	
	boolean check(String path, long stamp) {
		if(!balances.containsKey(path.split("\\.")[0])) {
			create(path.split("\\.")[0]);
			return true;
		}
		if(path.contains("balance")) {
			return balances.get(path.split("\\.")[0]).getFirst().getSecond() < stamp;
		}
		if(path.contains("bank")) {
			return balances.get(path.split("\\.")[0]).getSecond().getSecond() < stamp;
		}
		return false;
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
