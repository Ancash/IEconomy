package de.ancash.ieconomy.sockets.packets;

import java.io.IOException;

import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;

import de.ancash.sockets.packet.Packet;

public class IEGetFilePacket extends AbstractIEPacket{

	private static final long serialVersionUID = -4908163149774994723L;

	private final String path;
	@SuppressWarnings("unused")
	private String file;
	
	public IEGetFilePacket(String path) {
		this.path = path;
	}
	
	@Override
	public Packet toPacket() {
		throw new UnsupportedOperationException();
	}
	
	public String getPath() {
		return path;
	}
	
	public void setFile(YamlFile file) {
		try {
			this.file = file.saveToString();
		} catch (IOException e) {
			this.file = null;
		}
	}
}