package de.ancash.ieconomy.sockets;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.ieconomy.sockets.cache.IECache;
import de.ancash.ieconomy.sockets.listeners.MMORPGPacketListener;
import de.ancash.libs.org.bukkit.event.EventManager;
import de.ancash.misc.IFileLogger;
import de.ancash.misc.IPrintStream.ConsoleColor;

public class IEconomy extends JavaPlugin{

	public final String PREFIX = "IEconomy - ";
	
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final IFileLogger logger;
	private final IECache cache = new IECache(this);
	
	
	public IEconomy() throws SecurityException, IOException {
		logger = new IFileLogger(getClass().getSimpleName(), "plugins/IEconomy/logs", false);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.SEVERE);
		getLogger().addHandler(consoleHandler);
	}
	
	@Override
	public void onEnable() {
		EventManager.registerEvents(new MMORPGPacketListener(this), this);
		threadPool.execute(cache);
		info("Enabled!");
	}
	
	@Override
	public void onDisable() {
		cache.disposeAll();
		threadPool.shutdownNow();
	}
	
	public void info(String str) {
		System.out.println(ConsoleColor.GREEN_BOLD_BRIGHT + PREFIX + str + ConsoleColor.RESET);
		getLogger().info(str);
	}
	
	public void warn(String str) {
		System.out.println(ConsoleColor.YELLOW_BOLD_BRIGHT + PREFIX + str + ConsoleColor.RESET);
		getLogger().warning(str);
	}
	
	public Logger getLogger() {
		return logger.getLogger();
	}

	public IECache getCache() {
		return cache;
	}
}