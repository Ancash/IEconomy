package de.ancash.ieconomy.sockets.packets;

import java.io.IOException;
import java.util.UUID;

import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;

import de.ancash.sockets.packet.Packet;

public class IEGetFilePacket extends AbstractIEPacket{

	private static final long serialVersionUID = -4908163149774994723L;

	public static IEGetFilePacket getPlayerFile(UUID id) {
		return new IEGetFilePacket("player/" + id.toString());
	}
	
	@SuppressWarnings("unused")
	private final String path;
	private String file;
	
	public IEGetFilePacket(String path) {
		this.path = path;
	}
	
	@Override
	public Packet toPacket() {
		Packet packet = new Packet(GET_FILE);
		packet.setSerializable(this);
		packet.isClientTarget(false);
		packet.setAwaitResponse(true);
		return packet;
	}
	
	public YamlFile getFile() {
		YamlFile f = new YamlFile();
		try {
			f.loadFromString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}
}