package de.ancash.ieconomy.sockets.packets;

import java.util.UUID;

import de.ancash.sockets.packet.Packet;

public class IEPacket extends AbstractIEPacket{

	private static final long serialVersionUID = -2637365827245477501L;
	
	@SuppressWarnings("unused")
	private final UUID uuid;
	private double value;
	private transient final short header;
	private transient final boolean awaitResponse;
	
	public IEPacket(boolean awaitResponse, short header, UUID uuid, double value) {
		this.uuid = uuid;
		this.value = value;
		this.header = header;
		this.awaitResponse = awaitResponse;
	}
	
	public double getValue() {
		return value;
	}
	
	@Override
	public Packet toPacket() {
		Packet p = new Packet(header);
		p.setSerializable(this);
		p.isClientTarget(false);
		p.setAwaitResponse(awaitResponse);
		return p;
	}

}