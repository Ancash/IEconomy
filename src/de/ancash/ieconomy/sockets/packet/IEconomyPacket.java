package de.ancash.ieconomy.sockets.packet;

import java.io.Serializable;
import java.util.UUID;

import de.ancash.sockets.packet.Packet;

public class IEconomyPacket implements Serializable{

	private static final long serialVersionUID = -4503937579838947335L;

	private UUID uuid;
	private double value;

	public UUID getUUID() {
		return uuid;
	}

	public IEconomyPacket setUUID(UUID uuid) {
		this.uuid = uuid;
		return this;
	}

	public double getValue() {
		return value;
	}

	public IEconomyPacket setValue(double value) {
		this.value = value;
		return this;
	}
	
	public static Packet asPacket(short header, UUID uuid, double value) {
		return new Packet(header).setSerializable(new IEconomyPacket().setUUID(uuid).setValue(value));
	}
}