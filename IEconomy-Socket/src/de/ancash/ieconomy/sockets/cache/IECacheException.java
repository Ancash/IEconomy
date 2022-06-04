package de.ancash.ieconomy.sockets.cache;

public class IECacheException extends RuntimeException{

	private static final long serialVersionUID = -7831157942938796486L;
	
	public IECacheException(String e, Throwable t) {
		super(e, t);
	}
}