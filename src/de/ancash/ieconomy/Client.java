package de.ancash.ieconomy;

import de.ancash.ilibrary.sockets.Answer;
import de.ancash.ilibrary.sockets.ChatClient;
import de.ancash.ilibrary.sockets.InfoPacket;
import de.ancash.ilibrary.sockets.Request;

class Client extends ChatClient{

	boolean wait = true;
	
	public Client(String serverName, int serverPort, String plugin) {
		super(serverName, serverPort, plugin);
	}

	@Override
	public void onAnswer(Answer arg0) {
		
	}

	@Override
	public void onRequest(Request req) {
		if(req.getRequest().equals("economy pullall")) {
			if(wait) return;
			IEconomy.getInstance().debug("Pushing all Balances!");
			IEconomy.getInstance().pushAll();
		}
	}

	@Override
	public void onInfo(InfoPacket packet) {
		if(packet.getMsg().split(" ")[0].equals("updatebalance")) {
			IEconomy.getInstance().update(packet.getMsg().split(" ")[1], Double.valueOf(packet.getMsg().split(" ")[2]), Long.valueOf(packet.getMsg().split(" ")[3]));
			return;
		}
		System.out.println("Unknown Packet: " + packet.getMsg());
		/*if(packet.getMsg().split(" ")[0].equals("pushall")) {
			IEconomy.getInstance().debug("Received Pull!");
			IEconomy.getInstance().update(packet.getMsg().split(" ")[1], Double.valueOf(packet.getMsg().split(" ")[2]), Long.valueOf(packet.getMsg().split(" ")[3]));
		}*/
	}

}
