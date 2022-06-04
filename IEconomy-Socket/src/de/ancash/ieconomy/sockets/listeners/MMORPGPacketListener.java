package de.ancash.ieconomy.sockets.listeners;

import java.io.IOException;

import de.ancash.libs.org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.ieconomy.sockets.IEconomy;
import de.ancash.ieconomy.sockets.packets.AbstractIEPacket;
import de.ancash.ieconomy.sockets.packets.IEGetFilePacket;
import de.ancash.ieconomy.sockets.packets.IEPacket;
import de.ancash.ieconomy.sockets.packets.IEServerConnectPacket;
import de.ancash.libs.org.bukkit.event.EventHandler;
import de.ancash.libs.org.bukkit.event.Listener;
import de.ancash.misc.MathsUtils;
import de.ancash.sockets.events.ServerPacketReceiveEvent;
import de.ancash.sockets.packet.Packet;

public class MMORPGPacketListener implements Listener{

	private final IEconomy pl;

	public static final String BALANCE_PATH = "balance";
	public static final String BANK_PATH = "bank";
	
	public MMORPGPacketListener(IEconomy pl) {
		this.pl = pl;
	}
	
	@EventHandler
	public void onPacket(ServerPacketReceiveEvent event) throws ClassNotFoundException, IOException, InvalidConfigurationException {
		Packet packet = event.getPacket();
		
		if(packet.getSerializable() instanceof AbstractIEPacket) {
			AbstractIEPacket mmorpgPacket = (AbstractIEPacket) packet.getSerializable();
			if(mmorpgPacket instanceof IEServerConnectPacket ) {
				onServerConnectPacket(event);
			} else if(mmorpgPacket instanceof IEGetFilePacket) {
				getFilePacket(event);
			} else if(mmorpgPacket instanceof IEPacket) {
				economyPacket(event);
			}
		}
	}	
	
	private void economyPacket(ServerPacketReceiveEvent event) {
		IEPacket iep = (IEPacket) event.getPacket().getSerializable();
		iep.setValue(MathsUtils.round(iep.getValue(), 2));
		switch (event.getPacket().getHeader()) {
		case AbstractIEPacket.GET_BALANCE:
			iep.setValue(pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).getDouble(BALANCE_PATH));
			event.getClient().putWrite(event.getPacket().toBytes());
			break;
		case AbstractIEPacket.SET_BALANCE:
			pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).set(BALANCE_PATH, iep.getValue());
			event.getClient().putWrite(event.getPacket().toBytes());
			break;
		case AbstractIEPacket.ADD_TO_BALANCE:
			iep.setValue(MathsUtils.round(pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).getDouble(BALANCE_PATH) + iep.getValue(), 2));
			pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).set(BALANCE_PATH, iep.getValue());
			event.getClient().putWrite(event.getPacket().toBytes());
			break;
		case AbstractIEPacket.REMOVE_FROM_BALANCE:
			iep.setValue(MathsUtils.round(pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).getDouble(BALANCE_PATH) - iep.getValue(), 2));
			pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).set(BALANCE_PATH, iep.getValue());
			event.getClient().putWrite(event.getPacket().toBytes());
			break;
		case AbstractIEPacket.GET_BANK:
			iep.setValue(pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).getDouble(BANK_PATH));
			event.getClient().putWrite(event.getPacket().toBytes());
			break;
		case AbstractIEPacket.SET_BANK:
			pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).set(BANK_PATH, iep.getValue());
			event.getClient().putWrite(event.getPacket().toBytes());
			break;
		case AbstractIEPacket.ADD_TO_BANK:
			iep.setValue(MathsUtils.round(pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).getDouble(BANK_PATH) + iep.getValue(), 2));
			pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).set(BANK_PATH, iep.getValue());
			event.getClient().putWrite(event.getPacket().toBytes());
			break;
		case AbstractIEPacket.REMOVE_FROM_BANK:
			iep.setValue(MathsUtils.round(pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).getDouble(BANK_PATH) - iep.getValue(), 2));
			pl.getCache().getMMORPGFile(pl.getCache().getPlayerFile(iep.getUUID())).set(BANK_PATH, iep.getValue());
			event.getClient().putWrite(event.getPacket().toBytes());
			break;
		default:
			break;
		}
	}
	
	private void getFilePacket(ServerPacketReceiveEvent event) {
		IEGetFilePacket f = (IEGetFilePacket) event.getPacket().getSerializable();
		f.setFile(pl.getCache().getMMORPGFile("plugins/IEconomy/" + f.getPath()));
		event.getClient().putWrite(event.getPacket().toBytes());
	}

	private void onServerConnectPacket(ServerPacketReceiveEvent event) {
		event.getPacket().setSerializable(true);
		event.getClient().putWrite(event.getPacket().toBytes());
	}
}