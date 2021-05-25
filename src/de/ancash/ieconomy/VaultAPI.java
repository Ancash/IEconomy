package de.ancash.ieconomy;

import java.io.IOException;
import java.util.List;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;

public class VaultAPI extends AbstractEconomy{

	@Override
	public EconomyResponse bankBalance(String arg0) {//
		return null; 
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {//
		try {
			IEconomy.getInstance().depositBank(arg0, arg1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {//
		return null;
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {//
		try {
			IEconomy.getInstance().withdrawBank(arg0, arg1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {//
		return null;
	}

	@Override
	public boolean createPlayerAccount(String arg0) {//
		return false;
	}

	@Override
	public boolean createPlayerAccount(String arg0, String arg1) {//
		return false;
	}

	@Override
	public String currencyNamePlural() {//
		return "Coins";
	}

	@Override
	public String currencyNameSingular() {//
		return "Coin";
	}

	@Override
	public EconomyResponse deleteBank(String arg0) {//
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, double arg1) {//
		try {
			IEconomy.getInstance().depositPlayer(arg0, arg1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, String arg1, double arg2) {//
		return null;
	}

	@Override
	public String format(double arg0) {//
		return IEconomy.getInstance().format(arg0);
	}

	@Override
	public int fractionalDigits() {//
		return 2;
	}

	@Override
	public double getBalance(String arg0) {//
		try {
			return IEconomy.getInstance().getBalance(arg0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public double getBalance(String arg0, String arg1) {//
		return 0;
	}

	@Override
	public List<String> getBanks() {//
		return null;
	}

	@Override
	public String getName() {//
		return "Economy";
	}

	@Override
	public boolean has(String arg0, double arg1) {
		try {
			return IEconomy.getInstance().getBalance(arg0)  > arg1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean has(String arg0, String arg1, double arg2) {	
		return false;
	}

	@Override
	public boolean hasAccount(String arg0) {
		return false;
	}

	@Override
	public boolean hasAccount(String arg0, String arg1) {
		return false;
	}

	@Override
	public boolean hasBankSupport() {
		return true;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, double arg1) {
		try {
			IEconomy.getInstance().withdrawPlayer(arg0, arg1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
		return null;
	}

}
