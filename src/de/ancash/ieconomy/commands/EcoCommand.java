package de.ancash.ieconomy.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.ILibrary;
import de.ancash.ieconomy.IEconomy;
import de.ancash.misc.MathsUtils;
import de.ancash.sockets.storage.StorageAction;
import de.ancash.sockets.storage.StorageCallback;
import de.ancash.sockets.storage.StoragePacket;
import de.ancash.sockets.storage.StorageResult;

public class EcoCommand implements CommandExecutor{
	
	private static final String PATH = "IEconomy/player";
	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		final Player p = sender instanceof Player ? (Player) sender : null;
		
		if(args.length == 0) {
			if(p != null) {
				StoragePacket sp = new StoragePacket(StorageAction.GET_DOUBLE, PATH + "/" + p.getUniqueId(), IEconomy.getInstance().getBalancePath(), null, new StorageCallback() {
					
					@Override
					public void call(StorageResult arg0) {
						p.sendMessage("§aYour balance: §6" + arg0.getValue() + " coins");
					}
				});
				try {
					ILibrary.getInstance().send(sp.getPacket());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
			return false;
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if(!handle(p == null ? sender : p, args, p != null)) {
						sender.sendMessage("§7/eco §fCheck your balance.");
						sender.sendMessage("§7/eco check [player] §fCheck player balance.");
						sender.sendMessage("§7/eco pay [player] [amount] §fPay your money to other player.");
						sender.sendMessage("§7/eco give [player] [amount] §fGive money to player.");
						sender.sendMessage("§7/eco take [player] [amount] §fTake money from player.");
						sender.sendMessage("§7/eco set [player] [amount] §fSet balance to player.");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(IEconomy.getInstance());
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean handle(CommandSender sender, String[] args, boolean isPlayer) throws IOException {
		if(!isPlayer && args[0].toLowerCase().equals("pay")) return true;
		if(args.length == 1) return false;
		final OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
		
		if(args.length == 3) {
			
			if(!sender.hasPermission("eco." + args[0].toLowerCase())) {
				sender.sendMessage("§cYou don't have permissions to do that!");
				IEconomy.getInstance().update(((Player) sender).getUniqueId());
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
			
			StoragePacket sp = null;
			
			switch (args[0].toLowerCase()) {
			
			case "pay":
				if(!isPlayer) return false;
				
				StoragePacket payerMoney = new StoragePacket(StorageAction.GET_DOUBLE, PATH + "/" + ((Player) sender).getUniqueId(), IEconomy.getInstance().getBalancePath(), null, new StorageCallback() {
					
					@Override
					public void call(final StorageResult result) {
						final double payerMoney = (double) result.getValue();
						if(payerMoney < toPay) {
							sender.sendMessage("§cYou don't have enough money!");
							return;
						} else {
							try {
								StoragePacket withdraw = new StoragePacket(StorageAction.SET_DOUBLE, PATH + "/" + ((Player) sender).getUniqueId(), IEconomy.getInstance().getBalancePath(), MathsUtils.round(payerMoney - toPay, 2));
								ILibrary.getInstance().send(withdraw.getPacket());
								StoragePacket deposit = new StoragePacket(StorageAction.ADD_TO_DOUBLE, PATH + "/" + target.getUniqueId(), IEconomy.getInstance().getBalancePath(), toPay);
								ILibrary.getInstance().send(deposit.getPacket());
								sender.sendMessage("§aYou payed " + target.getName() + " §6" + toPay + " coins§a!");
								if(target.isOnline()) {
									target.getPlayer().sendMessage("§a" + ((Player) sender).getDisplayName() + " §apayed you §6" + toPay + " coins§a!");
								}
								IEconomy.getInstance().update(((OfflinePlayer) sender).getUniqueId());
								IEconomy.getInstance().update(target.getUniqueId());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
					}
				});
				
				ILibrary.getInstance().send(payerMoney.getPacket());
				return true;
			case "set":
				IEconomy.getInstance().setBalance(target, toPay);
				sender.sendMessage("§a" + target.getName() + "'s new balance: §6" + toPay + " coins");
				return true;
			case "take":		
				if(!(IEconomy.getInstance().getBalance(target) >= toPay)) {
					sender.sendMessage("§cThat player only has §6" + IEconomy.getInstance().getBalance(target) + " coins§c!");
					IEconomy.getInstance().update(target.getUniqueId());
					return true;
				}
				
				StoragePacket targetBal = new StoragePacket(StorageAction.GET_DOUBLE, PATH + "/" + target.getUniqueId(), IEconomy.getInstance().getBalancePath(), null, new StorageCallback() {
					
					@Override
					public void call(StorageResult result) {
						
						try {
							final double targetBal = (double) result.getValue();
							if(targetBal < toPay) {
								sender.sendMessage("§cThat player only has §6" + IEconomy.getInstance().getBalance(target) + " coins§c!");
							} else {
								StoragePacket setBal = new StoragePacket(StorageAction.SET_DOUBLE, PATH + "/" + target.getUniqueId(), IEconomy.getInstance().getBalancePath(), MathsUtils.round(targetBal - toPay, 2));
								ILibrary.getInstance().send(setBal.getPacket());
								sender.sendMessage("§aTook §6" + toPay + " coins §afrom " + target.getName() + "! New Balance: §6" + (targetBal - toPay) + " coins");
							}
							IEconomy.getInstance().update(target.getUniqueId());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				ILibrary.getInstance().send(targetBal.getPacket());
				return true;
			case "give":					
				
				IEconomy.getInstance().depositPlayer(target, toPay);
				sp = new StoragePacket(StorageAction.GET_DOUBLE, PATH + "/" + target.getUniqueId(), IEconomy.getInstance().getBalancePath(), null, new StorageCallback() {
					
					@Override
					public void call(StorageResult arg0) {
						sender.sendMessage("§aGave " + target.getName() + " §6" + toPay + " coins§a! New Balance: §6" + arg0.getValue() + " coins");
					}
				});
				ILibrary.getInstance().send(sp.getPacket());
				return true;
			default:
				break;
			}
			return false;
		}
		if(args.length == 2) {
			if(args[0].toLowerCase().equals("check")) {
				IEconomy.getInstance().update(target.getUniqueId());
				if(!sender.hasPermission("eco.check")) {
					sender.sendMessage("§cYou don't have permissions to do that!");
					return true;
				}
				StoragePacket sp = new StoragePacket(StorageAction.GET_DOUBLE, PATH + "/" + target.getUniqueId(), IEconomy.getInstance().getBalancePath(), null, new StorageCallback() {
					
					@Override
					public void call(StorageResult arg0) {
						sender.sendMessage("§a" + target.getName() + "'s balance: §6" + arg0.getValue() + " coins");
					}
				});
				ILibrary.getInstance().send(sp.getPacket());
				return true;
			}
			return false;
		}
		return false;
	}
}
