package de.ancash.ieconomy.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.ancash.ieconomy.IEconomy;

public class EcoCommand implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		Player p = sender instanceof Player ? (Player) sender : null;
		
		if(args.length == 0) {
			if(p != null) {
				p.sendMessage("§fYour balance: " + IEconomy.getInstance().format(IEconomy.getInstance().getBalance(p)));
			}
			return false;
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!handle(p == null ? sender : p, args, p != null)) {
					sender.sendMessage("§7/eco §fCheck yourr balance.");
					sender.sendMessage("§7/eco check [player] §fCheck player balance.");
					sender.sendMessage("§7/eco pay [player] [amount] §fPay your money to other player.");
					sender.sendMessage("§7/eco give [player] [amount] §fGive money to player.");
					sender.sendMessage("§7/eco take [player] [amount] §fTake money from player.");
					sender.sendMessage("§7/eco set [player] [amount] §fSet balance to player.");
				}
			}
		}.runTaskAsynchronously(IEconomy.getInstance());
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean handle(CommandSender p, String[] args, boolean isPlayer) {
		if(!isPlayer && args[0].toLowerCase().equals("pay")) return true;
		OfflinePlayer target = null;
		if(args.length == 2 || args.length == 3) target = Bukkit.getOfflinePlayer(args[1]);
		
		if(args.length == 3) {
			
			
			if(target == null || (p instanceof Player && !IEconomy.getInstance().exists((Player)p))) {
				p.sendMessage("§cCould not find " + args[1]);
				return true;
			}
			
			if(!p.hasPermission("eco." + args[0].toLowerCase())) {
				p.sendMessage("§cYou don't have permissions to do that!");
				return true;
			}
			
			double toPay = 0;
			try {
				toPay = Double.valueOf(args[2]);
			} catch(Exception e) {
				p.sendMessage("§cUnknown value: " + args[2]);
				return true;
			}
			
			switch (args[0].toLowerCase()) {
			
			
			case "pay":
				if(!isPlayer) return true;
				
				if(!IEconomy.getInstance().exists(target)) {
					p.sendMessage("§cThat player is not registered!");
					return true;
				}
				
				if(!(IEconomy.getInstance().getBalance((OfflinePlayer) p) >= toPay)) {
					p.sendMessage("§cYou don't have enough money!");
					return true;
				}
				
				IEconomy.getInstance().depositPlayer(target, toPay);
				IEconomy.getInstance().withdrawPlayer((Player) p, toPay);
				
				if(target.isOnline()) {
					target.getPlayer().sendMessage("§b" + ((Player) p).getDisplayName() + " §apayed you §6" + IEconomy.getInstance().format(toPay));
				}
				return true;
			case "set":
				IEconomy.getInstance().setBalance(target, toPay);
				return true;
			case "take":		
				if(!IEconomy.getInstance().exists(target)) {
					p.sendMessage("§cThat player is not registered!");
					return true;
				}
				
				if(!(IEconomy.getInstance().getBalance(target) >= toPay)) {
					p.sendMessage("§cThat player has only §6" + IEconomy.getInstance().format(IEconomy.getInstance().getBalance(target)) + "§c!");
					return true;
				}
				
				IEconomy.getInstance().withdrawPlayer(target, toPay);
				
				
				p.sendMessage("§aTook §6" + toPay + " §afrom " + target.getName() + "! New Balance: §6" + IEconomy.getInstance().getBalance(target));
				return true;
			case "give":					
				if(!IEconomy.getInstance().exists(target)) {
					p.sendMessage("§cThat player is not registered!");
					return true;
				}
				
				IEconomy.getInstance().depositPlayer(target, toPay);
				
				p.sendMessage("§aTook §6" + toPay + " §afrom " + target.getName() + "! New Balance: §6" + IEconomy.getInstance().getBalance(target));
				return true;
			default:
				break;
			}
			return false;
		}
		if(args.length == 2) {
			if(args[0].toLowerCase().equals("check")) {
				if(!p.hasPermission("eco.check")) {
					p.sendMessage("§cYou don't have permissions to do that!");
					return true;
				}
				p.sendMessage("" + target.getName() + "'s balance: " + IEconomy.getInstance().getBalance(target));
				return true;
			}
			return false;
		}
		return false;
	}
}
