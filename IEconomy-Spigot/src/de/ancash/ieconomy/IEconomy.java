package de.ancash.ieconomy;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.ILibrary;
import de.ancash.ieconomy.async.IEThreadPool;
import de.ancash.ieconomy.commands.EcoCommand;
import de.ancash.ieconomy.sockets.packets.AbstractIEPacket;
import de.ancash.ieconomy.sockets.packets.IEPacket;
import de.ancash.ieconomy.sockets.packets.IEServerConnectPacket;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.sockets.packet.PacketFuture;
import de.ancash.sockets.async.client.AbstractAsyncClient;
import de.ancash.sockets.async.plugin.AbstractJavaSocketPlugin;
import de.ancash.sockets.events.ClientConnectEvent;
import de.ancash.sockets.events.ClientDisconnectEvent;
import de.ancash.sockets.events.ClientPacketReceiveEvent;
import de.ancash.sockets.packet.Packet;
import net.milkbowl.vault.economy.Economy;

public class IEconomy extends AbstractJavaSocketPlugin{

	public static final String BASE_DIR = "plugins/MMORPG";
	private static IEconomy INSTANCE;
	
	private final long TIMEOUT = 2000;
	private final IEThreadPool threadPool = new IEThreadPool(this);
	private VaultAPI vaultInstance;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		getLogger().info("Registering commands...");
		getCommand("eco").setExecutor(new EcoCommand(this));
		
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
			hookVault();
			getLogger().info("Hooked into Vault!");
		}
		
		connectWithDelay(0);
	}
	
	public void hookVault() {
		vaultInstance = new VaultAPI();
	    Bukkit.getServicesManager().register(Economy.class, vaultInstance, Bukkit.getPluginManager().getPlugin("Vault"), ServicePriority.Normal);
	}
	
	@Override
	public void onDisable() {
		if(!isConnected()) return;
		super.chatClient.onDisconnect(new IllegalStateException("Plugin disabling!"));
	}
	
	@EventHandler
	public void onPacket(ClientPacketReceiveEvent event) {
		super.onPacket(event);
	}
	
	@EventHandler
	public void onClientDisconnect(ClientDisconnectEvent event) {
		super.onClientDisconnect(event);
	}
	
	@EventHandler
	public void onClientConnect(ClientConnectEvent event) {
		super.onClientConnect(event);
	}
	
	@Override
	public void onPacketReceive(Packet packet) {}
	
	@Override
	public synchronized void onClientDisconnect(AbstractAsyncClient client) {
		if(!client.equals(super.chatClient)) return;
		if(!threadPool.isRunning()) {
			if(isEnabled()) {
				getLogger().warning("Could not connect to " + ILibrary.getInstance().getAddress() + ":" + ILibrary.getInstance().getPort());
				getLogger().info("Trying again in 10 seconds...");
				connectWithDelay(200);
			}
			return;
		}
		threadPool.stop();
		getLogger().warning("Disconnected from " + ILibrary.getInstance().getAddress() + ":" + ILibrary.getInstance().getPort());
		if(isEnabled()) {
			getLogger().info("Trying again in 10 seconds...");
			connectWithDelay(200);
		}
	}

	@Override
	public synchronized void onClientConnect(AbstractAsyncClient client) {
		if(!client.equals(super.chatClient)) return;
		getLogger().info("Connected to " + ILibrary.getInstance().getAddress() + ":" + ILibrary.getInstance().getPort());
		getLogger().info("Sending " + IEServerConnectPacket.class.getSimpleName());
		Optional<Boolean> resp = sendPacket(new IEServerConnectPacket().toPacket()).get(TIMEOUT, TimeUnit.MILLISECONDS);
		if(resp.isPresent()) {
			if(resp.get()) {
				getLogger().info("Connection accepted!");
				threadPool.start();
			} else {
				getLogger().severe("Connection refused!");
				super.chatClient.onDisconnect(new IOException("Refused"));
			}
		} else {
			getLogger().severe("Could not connect to Server Socket! (Timeout)");
			if(!chatClient.isConnected()) {
				chatClient = null;
				this.onClientDisconnect(chatClient);
			} else {
				super.chatClient.onDisconnect(new IOException("Timeout"));		
			}
		}
	}
	
	private void connectWithDelay(int delayInTicks) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				getLogger().info("Connecting to server socket...");
				connect(ILibrary.getInstance().getAddress(), ILibrary.getInstance().getPort());
			}
		}.runTaskLater(this, delayInTicks);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	public double getBalance(String player) {
		return getBalance(Bukkit.getOfflinePlayer(player));
	}
	
	public double getBalance(OfflinePlayer off) {
		return getBalance(off.getUniqueId());
	}
	
	public double getBalance(UUID player) {
		synchronized (player) {
			Packet packet = new IEPacket(true, AbstractIEPacket.GET_BALANCE, player, 0).toPacket();
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			return ((IEPacket) sendPacket(packet, player).get(TIMEOUT, TimeUnit.MILLISECONDS).get()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double getBank(String player) {
		return getBank(Bukkit.getOfflinePlayer(player));
	}
	
	public double getBank(OfflinePlayer off) {
		return getBank(off.getUniqueId());
	}
	
	public Double getBank(UUID player) {
		synchronized (player) {
			Packet packet = new IEPacket(true, AbstractIEPacket.GET_BANK, player, 0).toPacket();
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			return ((IEPacket) sendPacket(packet, player).get(TIMEOUT, TimeUnit.MILLISECONDS).get()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double setBalance(String player, double amount, boolean awaitResponse) {
		return setBalance(Bukkit.getOfflinePlayer(player), amount, awaitResponse);
	}
	
	public double setBalance(OfflinePlayer p, double amount, boolean awaitResponse) {
		return setBalance(p.getUniqueId(), amount, awaitResponse);
	}
	
	public double setBalance(UUID uuid, double value, boolean awaitResponse) {
		synchronized (uuid) {
			Packet packet = new IEPacket(awaitResponse, AbstractIEPacket.SET_BALANCE, uuid, value).toPacket();
			packet.setAwaitResponse(awaitResponse);
			packet.isClientTarget(false);
			PacketFuture future = sendPacket(packet, uuid);
			if(!awaitResponse) return value;
			return ((IEPacket) future.get(TIMEOUT, TimeUnit.MILLISECONDS).get()).getValue();
		}
	}
	
	//withdraw
	@SuppressWarnings("deprecation")
	public double withdrawPlayer(String player, double amount) {
		return withdrawPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public double withdrawPlayer(OfflinePlayer off, double amount) {
		return withdrawPlayer(off.getUniqueId(), amount);
	}
	
	public double withdrawPlayer(UUID off, double amount) {
		synchronized (off) {
			Packet packet = new IEPacket(true, AbstractIEPacket.REMOVE_FROM_BALANCE, off, amount).toPacket();
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			return ((IEPacket) sendPacket(packet, off).get(TIMEOUT, TimeUnit.MILLISECONDS).get()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double withdrawBank(String player, double amount) {
		return withdrawBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public double withdrawBank(OfflinePlayer off, double amount) {
		return withdrawBank(off.getUniqueId(), amount);
	}
	
	public double withdrawBank(UUID off, double amount) {
		synchronized (off) {
			Packet packet = new IEPacket(true, AbstractIEPacket.REMOVE_FROM_BANK, off, amount).toPacket();
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			return ((IEPacket) sendPacket(packet, off).get(TIMEOUT, TimeUnit.MILLISECONDS).get()).getValue();
		}
	}
	
	//deposit
	@SuppressWarnings("deprecation")
	public double depositPlayer(String player, double amount) {
		return depositPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public double depositPlayer(OfflinePlayer off, double amount) {
		return depositPlayer(off.getUniqueId(), amount);
	}
	
	public double depositPlayer(UUID off, double amount) {
		synchronized (off) {
			Packet packet = new IEPacket(true, AbstractIEPacket.ADD_TO_BALANCE, off, amount).toPacket();
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			return ((IEPacket) sendPacket(packet, off).get(TIMEOUT, TimeUnit.MILLISECONDS).get()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double depositBank(String player, double amount) {
		return depositBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public double depositBank(OfflinePlayer off, double amount) {
		return depositBank(off.getUniqueId(), amount);
	}
	
	public double depositBank(UUID off, double amount) {
		synchronized (off) {
			Packet packet = new IEPacket(true, AbstractIEPacket.ADD_TO_BANK, off, amount).toPacket();
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			return ((IEPacket) sendPacket(packet, off).get(TIMEOUT, TimeUnit.MILLISECONDS).get()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double setBank(String player, double amount, boolean awaitResponse) {
		return setBank(Bukkit.getOfflinePlayer(player), amount, awaitResponse);
	}
	
	public double setBank(OfflinePlayer p, double amount, boolean awaitResponse) {
		return setBank(p.getUniqueId(), amount, awaitResponse);
	}
	
	public double setBank(UUID uuid, double value, boolean awaitResponse) {
		synchronized (uuid) {
			Packet packet = new IEPacket(awaitResponse, AbstractIEPacket.SET_BANK, uuid, value).toPacket();
			packet.setAwaitResponse(awaitResponse);
			packet.isClientTarget(false);
			PacketFuture future = sendPacket(packet, uuid);
			if(!awaitResponse) return value;
			return ((IEPacket) future.get(TIMEOUT, TimeUnit.MILLISECONDS).get()).getValue();
		}
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
	
	
	

	
	
	public IEThreadPool getThreadPool() {
		return threadPool;
	}

	public boolean isConnected() {
		return threadPool.isRunning();
	}
	
	public static IEconomy getInstance() {
		return INSTANCE;
	}
}