package de.ancash.ieconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.ieconomy.IEconomy;
import de.ancash.ieconomy.async.IERunnable;
import de.ancash.misc.MathsUtils;

public class EcoCommand implements CommandExecutor{
	
	private final IEconomy pl;
	public EcoCommand(IEconomy pl) {
		this.pl = pl;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(!pl.isConnected()) {
			sender.sendMessage("§cNot connected to server socket!");
			pl.getLogger().severe("Not connected to server socket!");
			return true;
		}
		final Player p = sender instanceof Player ? (Player) sender : null;
		
		if(args.length == 0) {
			if(p != null) {
				
				pl.getThreadPool().execute(new IERunnable(p.getUniqueId()) {
					
					@Override
					public void run() {
						
						try {
							sender.sendMessage("§aYour balance: §6" + IEconomy.getInstance().getBalance(p.getUniqueId()) + " coins");
						} catch (Exception e) {
							sender.sendMessage("§cSomething went wrong!");
							e.printStackTrace();
						}
					}
				});
				return true;
			}
			return false;
		}
		
		pl.getThreadPool().execute(new IERunnable(null) {
			@Override
			public void run() {
				if(!handle(p == null ? sender : p, args, p != null)) {
					sender.sendMessage("§7/eco §fCheck your balance.");
					sender.sendMessage("§7/eco check [player] §fCheck balance for a player.");
					sender.sendMessage("§7/eco pay [player] [amount] §fPay your money to other player.");
					sender.sendMessage("§7/eco give [player] [amount] §fGive money to player.");
					sender.sendMessage("§7/eco take [player] [amount] §fTake money from player.");
					sender.sendMessage("§7/eco set [player] [amount] §fSet balance to player.");
					sender.sendMessage("§7/eco bank set [player] [amount] §fSet balance to player.");
				}
			}
		});
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean handle(CommandSender sender, String[] args, boolean isPlayer) {
		if(!isPlayer && args[0].toLowerCase().equals("pay")) return false;
		if(args.length == 1) return false;
		final OfflinePlayer target = args.length == 4 ? Bukkit.getOfflinePlayer(args[2]) : Bukkit.getOfflinePlayer(args[1]);
		
		if(args.length == 3 && !args[0].toLowerCase().equals("bank")) {
			
			if(!sender.hasPermission("eco." + args[0].toLowerCase())) {
				sender.sendMessage("§cYou don't have permissions to do that!");
				return true;
			}
			
			double temp = 0;
			try {
				temp = MathsUtils.round(Double.valueOf(args[2]), 2);
			} catch(NumberFormatException e) {
				sender.sendMessage("§cInvalid value: " + args[2]);
				return true;
			}
			final double toPay = temp;
				
			switch (args[0].toLowerCase()) {
			
			case "pay":
				if(!isPlayer) return false;
				
				pl.getThreadPool().execute(new IERunnable(((Player) sender).getUniqueId()) {
					
					@Override
					public void run() {
						double payerBalance= IEconomy.getInstance().getBalance(((Player) sender).getUniqueId());
						if(payerBalance < toPay) {
							sender.sendMessage("§cYou don't have enough money!");
							return;
						}
						IEconomy.getInstance().withdrawPlayer(((Player) sender).getUniqueId(), toPay);
						IEconomy.getInstance().depositPlayer(target, toPay);
						sender.sendMessage("§aYou payed " + target.getName() + " §6" + toPay + " coins§a!");
						if(target.isOnline()) {
							target.getPlayer().sendMessage("§a" + ((Player) sender).getDisplayName() + " §apayed you §6" + toPay + " coins§a!");
						}
					}
				});
				return true;
			case "set":
				pl.getThreadPool().execute(new IERunnable(null) {
					
					@Override
					public void run() {
						sender.sendMessage("§a" + target.getName() + "'s new balance: §6" + IEconomy.getInstance().setBalance(target, toPay, true) + " coins");
					}
				});
				return true;
			case "take":		
				pl.getThreadPool().execute(new IERunnable(null) {
					
					@Override
					public void run() {
						sender.sendMessage("§aTook §6" + toPay + " coins §afrom " + target.getName() + "! New Balance: §6" + IEconomy.getInstance().withdrawPlayer(target.getUniqueId(), toPay) + " coins");
					}
				});
				return true;
			case "give":					
				
				pl.getThreadPool().execute(new IERunnable(null) {
					
					@Override
					public void run() {
						sender.sendMessage("§aGave " + target.getName() + " §6" + toPay + " coins§a! New Balance: §6" + IEconomy.getInstance().depositPlayer(target.getUniqueId(), toPay) + " coins");
					}
				});
				return true;
			default:
				break;
			}
			return false;
		}
		if(args.length == 2) {
			if(args[0].toLowerCase().equals("check")) {
				if(!sender.hasPermission("eco.check")) {
					sender.sendMessage("§cYou don't have permissions to do that!");
					return true;
				}
				pl.getThreadPool().execute(new IERunnable(null) {
					
					@Override
					public void run() {
						sender.sendMessage("§aCoin balances of " + target.getName());
						sender.sendMessage("§aPurse: §6" + IEconomy.getInstance().getBalance(target) + " coins");
						sender.sendMessage("§aBank: §6" + IEconomy.getInstance().getBank(target) + " coins");
					}
				});
				return true;
			}
			return false;
		}
		if(args.length == 4 && "bank".equals(args[0].toLowerCase())) {
			if(!sender.hasPermission("eco.bank." + args[1].toLowerCase())) {
				sender.sendMessage("§cYou don't have permissions to do that!");
				return true;
			}
			double temp = 0;
			try {
				temp = Double.valueOf(args[3]);
			} catch(NumberFormatException ex) {
				sender.sendMessage("§cInvalid value: " + args[2]);
				return true;
			}
			final double value = temp;
			switch (args[1].toLowerCase()) {
			case "set":
				pl.getThreadPool().execute(new IERunnable(null) {
					
					@Override
					public void run() {
						sender.sendMessage("§a" + target.getName() + "'s new bank balance: §6" + IEconomy.getInstance().setBank(target, value, true) + " coins");
					}
				});
				return true;
			default:
				break;
			}
		}
		return false;
	}
}
