package de.ancash.ieconomy.sockets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.Sockets;
import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.sockets.events.ServerPacketReceiveEvent;
import de.ancash.ieconomy.sockets.bank.BankInterestWatcher;
import de.ancash.ieconomy.sockets.packet.IEconomyHeader;
import de.ancash.ieconomy.sockets.packet.IEconomyPacket;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.EventManager;
import de.ancash.libs.org.bukkit.event.Listener;
import de.ancash.misc.MathsUtils;
import de.ancash.sockets.packet.Packet;

public class IEconomySockets extends JavaPlugin implements Listener{
	
	public static final String DIR_PATH = "plugins/IEconomy";
	public static final String PLAYER_DIR_PATH = DIR_PATH + "/player";
	public static final String BALANCE_PATH = "balance";
	public static final String BANK_PATH = "bank";
	public static final int CACHE_TIMEOUT_MINUTES = 10;
	
	private Thread bankInterrestThread;
	private Thread cacheWatcherThread;
	
	private YamlFile config = new YamlFile(DIR_PATH + "/config.yml");
	private ReentrantLock readWriteLock = new ReentrantLock(true);
	ReentrantLock fileLock = new ReentrantLock(true);
	final Map<String, Duplet<Long, YamlFile>> cachedFiles = new HashMap<>();
	
	@Override
	public void onEnable() {

		EventManager.registerEvents(this, this);
		try {
			config.createOrLoad();
			if(!config.contains("last-interest") || config.getLong("last-interest") <= 0) config.set("last-interest", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			if(!config.contains("interest-period") || config.getLong("interest-period") <= 0) config.set("interest-period", TimeUnit.HOURS.toSeconds(12));
			if(!config.contains("interest")) config.set("interest", 2);
			
			cacheWatcherThread = new Thread(new CacheWatcherThread(this));
			cacheWatcherThread.start();
			bankInterrestThread = new Thread(new BankInterestWatcher(this));
			bankInterrestThread.start();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		bankInterrestThread.interrupt();
		cacheWatcherThread.interrupt();
		try {
			config.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cachedFiles.values().stream().map(Duplet::getSecond).forEach(arg0 -> {
			try {
				arg0.save();
			} catch (IOException e) {
				System.err.println("Could not save file " + arg0.getName() + " : " + e);
			}
		});
	}
	
	public YamlFile getConfig() {
		return config;
	}
	
	public ReentrantLock getReadWriteLock() {
		return readWriteLock;
	}
	
	public YamlFile getYamlFile(String path) throws InvalidConfigurationException, IOException {
		YamlFile file = null;
		try {
			fileLock.lock();
			if(!cachedFiles.containsKey(path)) {
				file = new YamlFile(path);
				file.createOrLoad();
				cachedFiles.put(path, Tuple.of(0L, file));
			} else {
				file = cachedFiles.get(path).getSecond();
			}
			cachedFiles.get(path).setFirst(System.currentTimeMillis());
		} finally {
			fileLock.unlock();
		}
		return file;
	}
	
	@EventHandler
	public void onPacket(ServerPacketReceiveEvent event) throws IOException, InvalidConfigurationException {
		Packet packet = event.getPacket();
		if(packet.getSerializable() instanceof IEconomyPacket) {
			IEconomyPacket iep = (IEconomyPacket) packet.getSerializable();
			try {
				readWriteLock.lock();
				switch (packet.getHeader()) {
				case IEconomyHeader.GET_BALANCE:
					Sockets.write(getBalance(iep.getUUID(), packet.getTimeStamp()), event.getKey());
					break;
				case IEconomyHeader.SET_BALANCE:
					Sockets.write(setBalance(iep.getUUID(), packet.getTimeStamp(), iep.getValue()), event.getKey());
					break;
				case IEconomyHeader.ADD_TO_BALANCE:
					Sockets.write(addToBalance(iep.getUUID(), packet.getTimeStamp(), iep.getValue()), event.getKey());
					break;
				case IEconomyHeader.REMOVE_FROM_BALANCE:
					Sockets.write(removeFromBalance(iep.getUUID(), packet.getTimeStamp(), iep.getValue()), event.getKey());
					break;
				case IEconomyHeader.GET_BANK:
					Sockets.write(getBank(iep.getUUID(), packet.getTimeStamp()), event.getKey());
					break;
				case IEconomyHeader.SET_BANK:
					Sockets.write(setBank(iep.getUUID(), packet.getTimeStamp(), iep.getValue()), event.getKey());
					break;
				case IEconomyHeader.ADD_TO_BANK:
					Sockets.write(addToBank(iep.getUUID(), packet.getTimeStamp(), iep.getValue()), event.getKey());
					break;
				case IEconomyHeader.REMOVE_FROM_BANK:
					Sockets.write(removeFromBank(iep.getUUID(), packet.getTimeStamp(), iep.getValue()), event.getKey());
					break;
				default:
					break;
				}
			} finally {
				readWriteLock.unlock();
			}
		}
	}
	
	private Packet getBalance(UUID uuid, long timestamp) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.GET_BALANCE, uuid, getDouble(PLAYER_DIR_PATH + "/" + uuid, BALANCE_PATH), timestamp);
	}
	
	private Packet setBalance(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.SET_BALANCE, uuid, setDouble(PLAYER_DIR_PATH + "/" + uuid, BALANCE_PATH, value), timestamp);
	}

	private Packet addToBalance(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.ADD_TO_BALANCE, uuid, setDouble(PLAYER_DIR_PATH + "/" + uuid, BALANCE_PATH, getDouble(PLAYER_DIR_PATH + "/" + uuid, BALANCE_PATH) + value), timestamp);
	}
	
	
	private Packet removeFromBalance(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.REMOVE_FROM_BALANCE, uuid, setDouble(PLAYER_DIR_PATH + "/" + uuid, BALANCE_PATH, getDouble(PLAYER_DIR_PATH + "/" + uuid, BALANCE_PATH) - value), timestamp);
	}
	
	
	
	private Packet getBank(UUID uuid, long timestamp) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.GET_BANK, uuid, getDouble(PLAYER_DIR_PATH + "/" + uuid, BANK_PATH), timestamp);
	}
	
	private Packet setBank(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.SET_BANK, uuid, setDouble(PLAYER_DIR_PATH + "/" + uuid, BANK_PATH, value), timestamp);
	}
	
	private Packet addToBank(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.ADD_TO_BANK, uuid, setDouble(PLAYER_DIR_PATH + "/" + uuid, BANK_PATH, getDouble(PLAYER_DIR_PATH + "/" + uuid, BANK_PATH) + value), timestamp);
	}
	
	
	private Packet removeFromBank(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.REMOVE_FROM_BANK, uuid, setDouble(PLAYER_DIR_PATH + "/" + uuid, BANK_PATH, getDouble(PLAYER_DIR_PATH + "/" + uuid, BANK_PATH) - value), timestamp);
	}
	
	
	
	private double getDouble(String filePath, String path) throws IOException, InvalidConfigurationException {
		return MathsUtils.round(getDouble(getYamlFile(filePath), path), 2);
	}
	
	private double getDouble(YamlFile file, String path) throws IOException, InvalidConfigurationException {
		return file.getDouble(path);
	}
	
	private double setDouble(String filePath, String path, double value) throws IOException, InvalidConfigurationException {
		return setDouble(getYamlFile(filePath), path, MathsUtils.round(value, 2));
	}
	
	private double setDouble(YamlFile file, String path, double value) {
		file.set(path, MathsUtils.round(value, 2));
		return value;
	}
}