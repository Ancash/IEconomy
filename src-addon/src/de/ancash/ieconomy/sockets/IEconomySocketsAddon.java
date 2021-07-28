package de.ancash.ieconomy.sockets;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.Sockets;
import de.ancash.events.ServerPacketReceiveEvent;
import de.ancash.ieconomy.sockets.bank.BankInterestWatcher;
import de.ancash.ieconomy.sockets.packet.IEconomyHeader;
import de.ancash.ieconomy.sockets.packet.IEconomyPacket;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.EventManager;
import de.ancash.libs.org.bukkit.event.Listener;
import de.ancash.misc.MathsUtils;
import de.ancash.sockets.packet.Packet;
import de.ancash.yaml.configuration.file.YamlFile;
import de.ancash.yaml.exceptions.InvalidConfigurationException;

public class IEconomySocketsAddon extends JavaPlugin implements Listener{
	
	private final String FILE_PATH = "plugins/IEconomy";
	private final String BALANCE_PATH = "balance";
	private final String BANK_PATH = "bank";
	private Thread bankInterrestThread;
	private YamlFile config = new YamlFile(FILE_PATH + "/config.yml");
	
	@Override
	public void onEnable() {
		EventManager.registerEvents(this, this);
		try {
			config.createOrLoad();
			if(!config.contains("last-interest") || config.getLong("last-interest") <= 0) config.set("last-interest", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			if(!config.contains("interest-period") || config.getLong("interest-period") <= 0) config.set("interest-period", TimeUnit.HOURS.toSeconds(12));
			bankInterrestThread = new Thread(new BankInterestWatcher(config.getLong("last-interest"), config.getLong("interest-period"), config));
			bankInterrestThread.start();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		bankInterrestThread.interrupt();
		try {
			config.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public synchronized void onPacket(ServerPacketReceiveEvent event) throws IOException, InvalidConfigurationException {
		Packet packet = event.getPacket();
		if(packet.getSerializable() instanceof IEconomyPacket) {
			IEconomyPacket iep = (IEconomyPacket) packet.getSerializable();
			
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
		}
	}
	
	private Packet getBalance(UUID uuid, long timestamp) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.GET_BALANCE, uuid, getDouble(FILE_PATH + "/" + uuid, BALANCE_PATH), timestamp);
	}
	
	private Packet setBalance(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.SET_BALANCE, uuid, setDouble(FILE_PATH + "/" + uuid, BALANCE_PATH, value), timestamp);
	}

	private Packet addToBalance(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.ADD_TO_BALANCE, uuid, setDouble(FILE_PATH + "/" + uuid, BALANCE_PATH, getDouble(FILE_PATH + "/" + uuid, BALANCE_PATH) + value), timestamp);
	}
	
	
	private Packet removeFromBalance(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.REMOVE_FROM_BALANCE, uuid, setDouble(FILE_PATH + "/" + uuid, BALANCE_PATH, getDouble(FILE_PATH + "/" + uuid, BALANCE_PATH) - value), timestamp);
	}
	
	
	
	private Packet getBank(UUID uuid, long timestamp) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.GET_BANK, uuid, getDouble(FILE_PATH + "/" + uuid, BANK_PATH), timestamp);
	}
	
	private Packet setBank(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.SET_BANK, uuid, setDouble(FILE_PATH + "/" + uuid, BANK_PATH, value), timestamp);
	}
	
	private Packet addToBank(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.ADD_TO_BANK, uuid, setDouble(FILE_PATH + "/" + uuid, BANK_PATH, getDouble(FILE_PATH + "/" + uuid, BANK_PATH) + value), timestamp);
	}
	
	
	private Packet removeFromBank(UUID uuid, long timestamp, double value) throws IOException, InvalidConfigurationException {
		return IEconomyPacket.asPacket(IEconomyHeader.REMOVE_FROM_BANK, uuid, setDouble(FILE_PATH + "/" + uuid, BANK_PATH, getDouble(FILE_PATH + "/" + uuid, BANK_PATH) - value), timestamp);
	}
	
	
	
	private double getDouble(String filePath, String path) throws IOException, InvalidConfigurationException {
		YamlFile file = new YamlFile(filePath);
		file.createOrLoad();
		double val = getDouble(file, path);
		file.save();
		return val;
	}
	
	private double getDouble(YamlFile file, String path) throws IOException, InvalidConfigurationException {
		return file.getDouble(path);
	}
	
	private double setDouble(String filePath, String path, double value) throws IOException, InvalidConfigurationException {
		YamlFile file = new YamlFile(filePath);
		file.createOrLoad();
		setDouble(file, path, value);
		file.save();
		return value;
	}
	
	private double setDouble(YamlFile file, String path, double value) {
		file.set(path, MathsUtils.round(value, 2));
		return value;
	}
}