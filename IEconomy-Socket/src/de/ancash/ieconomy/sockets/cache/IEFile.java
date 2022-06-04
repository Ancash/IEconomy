package de.ancash.ieconomy.sockets.cache;

import java.io.IOException;
import java.util.logging.Level;

import de.ancash.libs.org.apache.commons.io.FileUtils;
import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;

import de.ancash.ieconomy.sockets.IEconomy;

public class IEFile extends YamlFile implements IECacheable{
	
	private long lastAccess = System.currentTimeMillis();
	private boolean disposable = true;
	
	public IEFile(IEconomy pl, String path, String resource) {
		super(path);
		if(!exists())
			try {
				if(resource != null) {
					FileUtils.copyInputStreamToFile(pl.getResource(resource), getConfigurationFile());
					pl.getLogger().log(Level.FINE, "Copied resource " + resource + " to " + path);
				} else {
					createNewFile(true);
					pl.getLogger().log(Level.FINE, "Created new file " + path);
				}
			} catch (IOException e1) {
				pl.getLogger().log(Level.SEVERE, String.format("Could not load file %s and resource %s. Using empty file", path, resource), e1);
				try {
					deleteFile();
					createNewFile(true);
					pl.getLogger().log(Level.SEVERE, String.format("%s deleted and new file created", getFilePath()));
				} catch (IOException e) {
					throw new IECacheException(String.format("Could not delete and create new file %s", getFilePath()), e);
				}
			}
		try {
			super.load();
			pl.getLogger().log(Level.FINE, String.format("Loaded file %s", getFilePath()));
		} catch (IOException e) {
			throw new IECacheException(String.format("Could not load file %s.", getFilePath()), e);
		}
	}
	
	@Override
	public long getLastAccess() {
		return lastAccess;
	}

	@Override
	public String toString() {
		return getConfigurationFile().getPath();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized IEFile updateLastAccess() {
		this.lastAccess = System.currentTimeMillis();
		return this;
	}

	@Override
	public void dispose() throws Exception{
		save();
	}

	@Override
	public boolean isDisposable() {
		return disposable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IEFile setDisposable(boolean b) {
		this.disposable = b;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IEFile onAccess() {
		//nothing to do
		return this;
	}
}