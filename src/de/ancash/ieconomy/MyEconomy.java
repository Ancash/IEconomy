package de.ancash.ieconomy;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.ILibrary;
import de.ancash.ieconomy.sockets.packet.IEconomyHeader;
import de.ancash.ieconomy.sockets.packet.IEconomyPacket;
import de.ancash.sockets.packet.Packet;

class MyEconomy extends JavaPlugin{
	
	private static final int TIME_OUT_MS = 2000;
	
	@SuppressWarnings("deprecation")
	public double getBalance(String player) throws IOException, InterruptedException {
		return getBalance(Bukkit.getOfflinePlayer(player));
	}
	
	public double getBalance(OfflinePlayer off) throws IOException, InterruptedException {
		return getBalance(off.getUniqueId());
	}
	
	public double getBalance(UUID player) throws IOException, InterruptedException {
		synchronized (player) {
			Packet packet = IEconomyPacket.asPacket(IEconomyHeader.GET_BALANCE, player, 0);
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			ILibrary.getInstance().send(packet);
			return ((IEconomyPacket) packet.awaitResponse(TIME_OUT_MS).get().getSerializable()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double getBank(String player) throws IOException, InterruptedException {
		return getBank(Bukkit.getOfflinePlayer(player));
	}
	
	public double getBank(OfflinePlayer off) throws IOException, InterruptedException {
		return getBank(off.getUniqueId());
	}
	
	public Double getBank(UUID player) throws IOException, InterruptedException {
		synchronized (player) {
			Packet packet = IEconomyPacket.asPacket(IEconomyHeader.GET_BANK, player, 0);
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			ILibrary.getInstance().send(packet);
			return ((IEconomyPacket) packet.awaitResponse(TIME_OUT_MS).get().getSerializable()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double setBalance(String player, double amount, boolean awaitResponse) throws IOException, InterruptedException {
		return setBalance(Bukkit.getOfflinePlayer(player), amount, awaitResponse);
	}
	
	public double setBalance(OfflinePlayer p, double amount, boolean awaitResponse) throws IOException, InterruptedException {
		return setBalance(p.getUniqueId(), amount, awaitResponse);
	}
	
	public double setBalance(UUID uuid, double value, boolean awaitResponse) throws IOException, InterruptedException {
		synchronized (uuid) {
			Packet packet = IEconomyPacket.asPacket(IEconomyHeader.SET_BALANCE, uuid, value);
			packet.setAwaitResponse(awaitResponse);
			packet.isClientTarget(false);
			ILibrary.getInstance().send(packet);
			if(!awaitResponse) return value;
			return ((IEconomyPacket) packet.awaitResponse(TIME_OUT_MS).get().getSerializable()).getValue();
		}
	}
	
	//withdraw
	@SuppressWarnings("deprecation")
	public double withdrawPlayer(String player, double amount) throws IOException, InterruptedException {
		return withdrawPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public double withdrawPlayer(OfflinePlayer off, double amount) throws IOException, InterruptedException {
		return withdrawPlayer(off.getUniqueId(), amount);
	}
	
	public double withdrawPlayer(UUID off, double amount) throws IOException, InterruptedException {
		synchronized (off) {
			Packet packet = IEconomyPacket.asPacket(IEconomyHeader.REMOVE_FROM_BALANCE, off, amount);
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			ILibrary.getInstance().send(packet);
			return ((IEconomyPacket) packet.awaitResponse(TIME_OUT_MS).get().getSerializable()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double withdrawBank(String player, double amount) throws IOException, InterruptedException {
		return withdrawBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public double withdrawBank(OfflinePlayer off, double amount) throws IOException, InterruptedException {
		return withdrawBank(off.getUniqueId(), amount);
	}
	
	public double withdrawBank(UUID off, double amount) throws IOException, InterruptedException {
		synchronized (off) {
			Packet packet = IEconomyPacket.asPacket(IEconomyHeader.REMOVE_FROM_BANK, off, amount);
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			ILibrary.getInstance().send(packet);
			return ((IEconomyPacket) packet.awaitResponse(TIME_OUT_MS).get().getSerializable()).getValue();
		}
	}
	
	//deposit
	@SuppressWarnings("deprecation")
	public double depositPlayer(String player, double amount) throws IOException, InterruptedException {
		return depositPlayer(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public double depositPlayer(OfflinePlayer off, double amount) throws IOException, InterruptedException {
		return depositPlayer(off.getUniqueId(), amount);
	}
	
	public double depositPlayer(UUID off, double amount) throws IOException, InterruptedException {
		synchronized (off) {
			Packet packet = IEconomyPacket.asPacket(IEconomyHeader.ADD_TO_BALANCE, off, amount);
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			ILibrary.getInstance().send(packet);
			return ((IEconomyPacket) packet.awaitResponse(TIME_OUT_MS).get().getSerializable()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double depositBank(String player, double amount) throws IOException, InterruptedException {
		return depositBank(Bukkit.getOfflinePlayer(player), amount);
	}
	
	public double depositBank(OfflinePlayer off, double amount) throws IOException, InterruptedException {
		return depositBank(off.getUniqueId(), amount);
	}
	
	public double depositBank(UUID off, double amount) throws IOException, InterruptedException {
		synchronized (off) {
			Packet packet = IEconomyPacket.asPacket(IEconomyHeader.ADD_TO_BANK, off, amount);
			packet.setAwaitResponse(true);
			packet.isClientTarget(false);
			ILibrary.getInstance().send(packet);
			return ((IEconomyPacket) packet.awaitResponse(TIME_OUT_MS).get().getSerializable()).getValue();
		}
	}
	
	@SuppressWarnings("deprecation")
	public double setBank(String player, double amount, boolean awaitResponse) throws IOException, InterruptedException {
		return setBank(Bukkit.getOfflinePlayer(player), amount, awaitResponse);
	}
	
	public double setBank(OfflinePlayer p, double amount, boolean awaitResponse) throws IOException, InterruptedException {
		return setBank(p.getUniqueId(), amount, awaitResponse);
	}
	
	public double setBank(UUID uuid, double value, boolean awaitResponse) throws IOException, InterruptedException {
		synchronized (uuid) {
			Packet packet = IEconomyPacket.asPacket(IEconomyHeader.SET_BANK, uuid, value);
			packet.setAwaitResponse(awaitResponse);
			packet.isClientTarget(false);
			ILibrary.getInstance().send(packet);
			if(!awaitResponse) return value;
			return ((IEconomyPacket) packet.awaitResponse(TIME_OUT_MS).get().getSerializable()).getValue();
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
}
