package de.ancash.ieconomy.commands;

import java.util.concurrent.ExecutorService;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.ieconomy.IEconomy;
import de.ancash.misc.MathsUtils;

public class EcoCommand implements CommandExecutor{
	
	private final ExecutorService executor;
	
	public EcoCommand(ExecutorService executor) {
		this.executor = executor;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		final Player p = sender instanceof Player ? (Player) sender : null;
		
		if(args.length == 0) {
			if(p != null) {
				
				executor.submit(new Runnable() {
					
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
		
		executor.submit(new Runnable() {
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
				
				executor.submit( new Runnable() {
					
					@Override
					public void run() {
						try {
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
						} catch (Exception e) {
							sender.sendMessage("§cSomething went wrong!");
							e.printStackTrace();
						}
					}
				});
				return true;
			case "set":
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						try {
							sender.sendMessage("§a" + target.getName() + "'s new balance: §6" + IEconomy.getInstance().setBalance(target, toPay, true) + " coins");
						} catch(Exception e) {
							sender.sendMessage("§cSomething went wrong!");
							e.printStackTrace();
						}
					}
				});
				return true;
			case "take":		
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						try {
							sender.sendMessage("§aTook §6" + toPay + " coins §afrom " + target.getName() + "! New Balance: §6" + IEconomy.getInstance().withdrawPlayer(target.getUniqueId(), toPay) + " coins");
						} catch(Exception e) {
							sender.sendMessage("§cSomething went wrong!");
							e.printStackTrace();
						}
					}
				});
				return true;
			case "give":					
				
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						try {
							sender.sendMessage("§aGave " + target.getName() + " §6" + toPay + " coins§a! New Balance: §6" + IEconomy.getInstance().depositPlayer(target.getUniqueId(), toPay) + " coins");
						} catch(Exception e) {
							sender.sendMessage("§cSomething went wrong!");
							e.printStackTrace();
						}
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
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						try {
							sender.sendMessage("§aCoin balances of " + target.getName());
							sender.sendMessage("§aPurse: §6" + IEconomy.getInstance().getBalance(target) + " coins");
							sender.sendMessage("§aBank: §6" + IEconomy.getInstance().getBank(target) + " coins");
						} catch(Exception e) {
							sender.sendMessage("§cSomething went wrong!");
							e.printStackTrace();
						}
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
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						try {
							sender.sendMessage("§a" + target.getName() + "'s new bank balance: §6" + IEconomy.getInstance().setBank(target, value, true) + " coins");
						} catch(Exception e) {
							sender.sendMessage("§cSomething went wrong!");
							e.printStackTrace();
						}
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
