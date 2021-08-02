package de.ancash.ieconomy.sockets.bank;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import de.ancash.ieconomy.sockets.IEconomySockets;
import de.ancash.misc.MathsUtils;

public class BankInterestWatcher implements Runnable{

	private long lastInterest;
	private long period;
	private final IEconomySockets pl;
	private final double interest;
	
	public BankInterestWatcher(IEconomySockets pl) {
		this.pl = pl;
		setLastInterrest(pl.getConfig().getLong("last-interest"));
		setPeriod(pl.getConfig().getLong("interest-period"));
		interest = MathsUtils.round(pl.getConfig().getDouble("interest"), 2);
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
					return;
				}
			}
			setLastInterrest(nextInterestTimestamp);
			pl.getConfig().set("last-interest", nextInterestTimestamp);
			
			double totalInterest = 0;
			try {
				pl.getReadWriteLock().lock();
				File dir = new File(IEconomySockets.PLAYER_DIR_PATH);
				if(dir.listFiles() != null) {
					for(File playerFile : dir.listFiles()) {
						YamlFile yamlPlayerFile = pl.getYamlFile(playerFile.getPath());
						double currentBank = yamlPlayerFile.getDouble(IEconomySockets.BANK_PATH);
						if(currentBank > 0) {
							double playersInterest = currentBank * (interest / 100);
							yamlPlayerFile.set(IEconomySockets.BANK_PATH, MathsUtils.round(playersInterest + currentBank, 2));
							yamlPlayerFile.set("last-interest", playersInterest);
							totalInterest += playersInterest;
						}
					}
				}
			} catch (InvalidConfigurationException | IOException e) {
				e.printStackTrace();
			} finally {
				pl.getReadWriteLock().unlock();
			}
			System.out.println("Bank interest! Total of " + MathsUtils.round(totalInterest, 2) + " coins! Next at " + nextInterest());
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

	public double getInterest() {
		return interest;
	}
}