package de.ancash.ieconomy.async;

import java.util.UUID;

public abstract class IERunnable implements Runnable{

	private final UUID uuid;
	
	public IERunnable(UUID player) {
		this.uuid = player;
	}

	public UUID getUUID() {
		return uuid;
	}	
}