package de.ancash.ieconomy.sockets;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.simpleyaml.configuration.file.YamlFile;

import de.ancash.datastructures.tuples.Duplet;

public class CacheWatcherThread implements Runnable {

	private final IEconomySockets pl;
	
	public CacheWatcherThread(IEconomySockets pl) {
		this.pl = pl;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				pl.fileLock.lock();
				int cnt = 0;
				for(String key : pl.cachedFiles.keySet().stream().collect(Collectors.toList())) {
					Duplet<Long, YamlFile> duplet = pl.cachedFiles.get(key);
					long minutesSinceLastUse = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - duplet.getFirst());
					if(minutesSinceLastUse >= IEconomySockets.CACHE_TIMEOUT_MINUTES) {
						pl.cachedFiles.remove(key);
						try {
							duplet.getSecond().save();
							cnt++;
						} catch (IOException e) {
							System.err.println("Could not save file " + duplet.getSecond().getFilePath() + " : " + e);
						}
					}
				}
				if(cnt != 0) System.out.println("Saved and deleted " + cnt + " files out of cache!");
			} finally {
				pl.fileLock.unlock();
			}
			try {
				Thread.sleep(TimeUnit.MINUTES.toMillis(1));
			} catch (InterruptedException e) {}
		}
	}
}
