package de.ancash.ieconomy.sockets.cache;

public interface IECacheable {

	public <T extends IECacheable> T updateLastAccess();
	
	public long getLastAccess();
	
	public <T extends IECacheable> T onAccess();
	
	public boolean isDisposable();
	
	public <T extends IECacheable> T setDisposable(boolean b);
	
	public void dispose() throws Exception;
}