package de.ancash.ieconomy.sockets.bank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import de.ancash.yaml.configuration.file.YamlFile;

public class BankInterestWatcher implements Runnable{

	private long lastInterest;
	private long period;
	private final YamlFile file;
	
	public BankInterestWatcher(long lastInterrest, long period, YamlFile file) {
		setLastInterrest(lastInterrest);
		setPeriod(period);
		this.file = file;
		System.out.println("Set interest-period to " + period + "s. Next in " + untileNextInterest() + "s.");
	}
	
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted() && Thread.currentThread().isAlive()) {
			long nextInterestTimestamp = lastInterest + period;
			long sleep = untileNextInterest();
			if(sleep > 0) {
				try {
					Thread.sleep(TimeUnit.SECONDS.toMillis(sleep));
				} catch (InterruptedException e) {
					System.err.println("Stopping BankInterestWatcher...");
					return;
				}
			}
			setLastInterrest(nextInterestTimestamp);
			file.set("last-interest", nextInterestTimestamp);
			System.out.println("Bank interest! Next at " + nextInterest());
		}
	}

	public long untileNextInterest() {
		long next = lastInterest + period - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		return next <= 0 ? 0L : next;
	}
	
	public String nextInterest() {
		return LocalDateTime.now().plusSeconds(untileNextInterest()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	}
	
	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public long getLastInterrest() {
		return lastInterest;
	}

	public void setLastInterrest(long lastInterrest) {
		this.lastInterest = lastInterrest;
	}
}