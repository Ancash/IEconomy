package de.ancash.ieconomy.sockets.packets;

import java.util.UUID;

import de.ancash.sockets.packet.Packet;

public class IEPacket extends AbstractIEPacket{

	private static final long serialVersionUID = -2637365827245477501L;
	
	private final UUID uuid;
	private double value;
	
	public IEPacket() {
		throw new UnsupportedOperationException();
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double d) {
		this.value = d;
	}

	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public Packet toPacket() {
		throw new UnsupportedOperationException();
	}

}