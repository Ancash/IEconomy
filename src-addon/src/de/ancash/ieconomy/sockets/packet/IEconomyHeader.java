package de.ancash.ieconomy.sockets.packet;

public class IEconomyHeader{
	
	private static final short BASE = 2000;
	
	public static final short RESPONSE = BASE;
	
	public static final short GET_BALANCE = BASE + 10;
	public static final short SET_BALANCE = BASE + 11;
	public static final short ADD_TO_BALANCE = BASE + 12;
	public static final short REMOVE_FROM_BALANCE = BASE + 13;
	public static final short GET_BANK = BASE + 31;
	public static final short SET_BANK = BASE + 32;
	public static final short ADD_TO_BANK = BASE + 33;
	public static final short REMOVE_FROM_BANK = BASE + 34;
}