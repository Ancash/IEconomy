package de.ancash.ieconomy.exception;

public class AsyncIEException extends RuntimeException{

	private static final long serialVersionUID = 8790435020735545883L;

	public AsyncIEException(String str, Exception ex) {
		super(str, ex);
	}
}