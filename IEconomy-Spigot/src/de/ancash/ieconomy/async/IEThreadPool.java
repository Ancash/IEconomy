package de.ancash.ieconomy.async;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import de.ancash.ieconomy.IEconomy;
import de.ancash.ieconomy.exception.AsyncIEException;

public class IEThreadPool {
	
	private ExecutorService executor = null;
	
	private final IEconomy pl;
	private final Map<IERunnable, Long> delayedExecutions = new HashMap<>();
	
	public IEThreadPool(IEconomy pl) {
		this.pl = pl;
	}
	
	private void submitDelaySubmitterThread() {
		executor.submit(new Runnable() {
			
			@Override
			public void run() {
				Thread.currentThread().setName("MMORPG-DelayedSubmitter");
				while(true) {
					try {
						Thread.sleep(1);
						long now = System.currentTimeMillis();
						synchronized (delayedExecutions) {
							for(IERunnable key : delayedExecutions.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet())) {
								if(now > delayedExecutions.get(key)) {
									delayedExecutions.remove(key);
									execute(key);
								}
							}
						}
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
	}
	
	public boolean isRunning() {
		return executor != null;
	}
	
	public synchronized void start() {
		stop();
		pl.getLogger().info("Starting " + getClass().getSimpleName());
		executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);
		submitDelaySubmitterThread();
	}
	
	public synchronized void stop() {
		if(executor == null) return;
		pl.getLogger().info("Stopping " + getClass().getSimpleName());
		executor.shutdownNow();
		executor = null;
	}
	
	public void executeDelayed(long ms, IERunnable r) {
		synchronized (delayedExecutions) {
			delayedExecutions.put(r, System.currentTimeMillis() + ms);
		}
	}
	
	public boolean execute(IERunnable r) {
		if(!isRunning()) {
			pl.getLogger().warning("Tried to execute runnable although not connected to server socket!");
			return false;
		}
		executor.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					r.run();
				} catch(Exception ex) {
					if(r.getUUID() != null) 
						Bukkit.getPlayer(r.getUUID()).sendMessage("§cSomething went wrong!");
					new AsyncIEException("Error while executing async runnable:", ex).printStackTrace();	
				}
			}
		});
		return true;
	}
}