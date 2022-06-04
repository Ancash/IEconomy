package de.ancash.ieconomy.sockets.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import de.ancash.ieconomy.sockets.IEconomy;

import java.util.UUID;

public class IECache implements Runnable{

	private final IEconomy pl;

	private final Map<String, IECacheable> cache = new HashMap<>();
	
	public IECache(IEconomy pl) {
		this.pl = pl;
	}
	
	public IEFile getMMORPGFile(String path) {
		return getMMORPGFile(path, null);
	}
	
	public IEFile getMMORPGFile(String path, String fallback) {
		synchronized (cache) {
			if(!cache.containsKey(path))
				cache.put(path, new IEFile(pl, path, fallback));
			return cache.get(path).updateLastAccess().onAccess();
		}
	}
	
	public String getPlayerFile(UUID uuid) {
		return "plugins/IEconomy/player/" + uuid.toString();
	}

	@Override
	public void run() {
		while(!Thread.interrupted()) {
			try {
				Thread.sleep(1_000);
			} catch (InterruptedException e) {
				pl.warn("Stopping");
				return;
			}
			
			checkCacheables();
		}
	}
	
	private void checkCacheables() {
		long now = System.currentTimeMillis();
		synchronized (cache) {
			Iterator<Entry<String, IECacheable>> keyIter = cache.entrySet().iterator();
			while(keyIter.hasNext()) {
				Entry<String, IECacheable> entry = keyIter.next();
				if(entry.getValue().isDisposable() && entry.getValue().getLastAccess() + 1_000 < now) {
					try {
						entry.getValue().dispose();
						pl.getLogger().log(Level.FINE, String.format("Disposed of cacheable %s", entry.getValue().getClass().getCanonicalName()));
					} catch (Exception e) {
						pl.getLogger().log(Level.SEVERE, String.format("Could not dispose of cacheable %s", entry.getValue().getClass().getCanonicalName()), e);
					} finally {	
						keyIter.remove();	
					}
				}
			}
		}
	}

	public void disposeAll() {
		synchronized (cache) {
			Iterator<IECacheable> iter = cache.values().iterator();
			while(iter.hasNext()) {
				IECacheable c = iter.next();
				if(c instanceof IEFile) {
					try {
						c.dispose();
						pl.getLogger().log(Level.FINE, String.format("Disposed of cacheable %s", c.getClass().getCanonicalName()));
					} catch (Exception e) {
						pl.getLogger().log(Level.SEVERE, String.format("Could not dispose of cacheable %s", c.getClass().getCanonicalName()), e);
					} finally {	
						iter .remove();	
					}
				}
			}
			cache.clear();
		}
	}
}