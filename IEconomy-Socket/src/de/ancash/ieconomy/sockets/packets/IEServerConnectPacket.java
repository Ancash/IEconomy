package de.ancash.ieconomy.sockets.packets;

import de.ancash.sockets.packet.Packet;

public class IEServerConnectPacket extends AbstractIEPacket{

	private static final long serialVersionUID = 745815847038186177L;

	@Override
	public Packet toPacket() {
		Packet packet = new Packet(CONNECT);
		packet.setSerializable(this);
		packet.isClientTarget(false);
		packet.setAwaitResponse(true);
		return packet;
	}	
}