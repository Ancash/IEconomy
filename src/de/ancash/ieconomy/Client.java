package de.ancash.ieconomy;

import de.ancash.ilibrary.sockets.NIOClient;
import de.ancash.ilibrary.sockets.Packet;

class Client extends NIOClient{

	boolean wait = true;
	
	public Client(String serverName, int serverPort, String plugin) {
		super(serverName, serverPort, plugin);
	}

	@Override
	public void onPacket(Packet packet) {
		if(!(packet.getObject() instanceof String)) {
			System.out.println("Unknown Packet: " + packet.getObject());
			return;
		}
		String msg = (String) packet.getObject();
		if(msg.equals("economy pullall")) {
			if(wait) return;
			IEconomy.getInstance().debug("Pushing all Balances!");
			IEconomy.getInstance().pushAll();
			return;
		}
		if(msg.split(" ").length == 4 && msg.split(" ")[0].equals("updatebalance")) {
			IEconomy.getInstance().update(msg.split(" ")[1], Double.valueOf(msg.split(" ")[2]), Long.valueOf(msg.split(" ")[3]));
			return;
		}
		System.out.println("Unknown Packet: " + packet.getObject());
	}

}
