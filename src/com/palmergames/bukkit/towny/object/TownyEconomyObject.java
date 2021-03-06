package com.palmergames.bukkit.towny.object;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyLogger;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.util.StringMgmt;

/**
 * Economy object which provides an interface with the Economy Handler.
 * 
 * @author ElgarL, Shade
 * 
 */
public class TownyEconomyObject extends TownyObject {

	private static final String townAccountPrefix = "town-";
	private static final String nationAccountPrefix = "nation-";

	/**
	 * Tries to pay from the players holdings
	 * 
	 * @param amount
	 * @param reason
	 * @return true if successfull
	 * @throws EconomyException
	 */
	public boolean pay(double amount, String reason) throws EconomyException {

		boolean payed = _pay(amount);
		if (payed)
			TownyLogger.logMoneyTransaction(this, amount, null, reason);
		return payed;
	}

	public boolean pay(double amount) throws EconomyException {

		return pay(amount, null);
	}

	private boolean _pay(double amount) throws EconomyException {

		if (canPayFromHoldings(amount)) {
			if (TownyEconomyHandler.isActive())
				return TownyEconomyHandler.subtract(getEconomyName(), amount, getBukkitWorld());
		}
		return false;
	}

	/**
	 * When collecting money add it to the Accounts bank
	 * 
	 * @param amount
	 * @param reason
	 * @throws EconomyException
	 */
	public void collect(double amount, String reason) throws EconomyException {

		TownyEconomyHandler.add(getEconomyName(), amount, getBukkitWorld());
		TownyLogger.logMoneyTransaction(null, amount, this, reason);
	}

	public void collect(double amount) throws EconomyException {

		collect(amount, null);
	}

	/**
	 * When one account is paying another account(Taxes/Plot Purchasing)
	 * 
	 * @param amount
	 * @param collector
	 * @param reason
	 * @return true if successfully payed amount to collector.
	 * @throws EconomyException
	 */
	public boolean payTo(double amount, TownyEconomyObject collector, String reason) throws EconomyException {

		boolean payed = _payTo(amount, collector);
		if (payed)
			TownyLogger.logMoneyTransaction(this, amount, collector, reason);
		return payed;
	}

	public boolean payTo(double amount, TownyEconomyObject collector) throws EconomyException {

		return payTo(amount, collector, null);
	}

	private boolean _payTo(double amount, TownyEconomyObject collector) throws EconomyException {

		if (_pay(amount)) {
			collector.collect(amount);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get a valid economy account name for this object.
	 * 
	 * @return account name
	 */
	public String getEconomyName() {

		// TODO: Make this less hard coded.
		if (this instanceof Nation)
			return StringMgmt.trimMaxLength(nationAccountPrefix + getName(), 32);
		else if (this instanceof Town)
			return StringMgmt.trimMaxLength(townAccountPrefix + getName(), 32);
		else
			return getName();
	}

	/**
	 * Fetch the current world for this object
	 * 
	 * @return Bukkit world for the object
	 */
	private World getBukkitWorld() {

		try {
			if (this instanceof Nation)
				return Bukkit.getWorld(TownyUniverse.getDataSource().getNation(this.getName()).getCapital().getWorld().getName());

			if (this instanceof Town)
				return Bukkit.getWorld(TownyUniverse.getDataSource().getTown(this.getName()).getWorld().getName());

			if (this instanceof Resident) {
				Player player = Bukkit.getPlayer(this.getName());
				return (player != null) ? player.getWorld() : Bukkit.getWorlds().get(0);
			}
		} catch (NotRegisteredException e) {
			// Failed to fetch world
		}

		return Bukkit.getWorlds().get(0);
	}

	/**
	 * Set balance and log this action
	 * 
	 * @param amount
	 * @param reason
	 */
	public void setBalance(double amount, String reason) {

		setBalance(amount);
		TownyLogger.logMoneyTransaction(null, amount, this, reason);
	}

	/**
	 * Set balance without logging the action
	 * 
	 * @param amount
	 */
	public void setBalance(double amount) {

		TownyEconomyHandler.setBalance(getEconomyName(), amount, getBukkitWorld());
	}

	public double getHoldingBalance() throws EconomyException {

		try {
			return TownyEconomyHandler.getBalance(getEconomyName(), getBukkitWorld());
		} catch (NoClassDefFoundError e) {
			e.printStackTrace();
			throw new EconomyException("Economy error getting holdings for " + getEconomyName());
		}
	}

	/**
	 * Does this object have enough in it's economy account to pay?
	 * 
	 * @param amount
	 * @return true if there is enough.
	 * @throws EconomyException
	 */
	public boolean canPayFromHoldings(double amount) throws EconomyException {

		return TownyEconomyHandler.hasEnough(getEconomyName(), amount, getBukkitWorld());
	}

	/**
	 * Used To Get Balance of Players holdings in String format for printing
	 * 
	 * @return current account balance formatted in a string.
	 */
	public String getHoldingFormattedBalance() {

		try {
			return TownyEconomyHandler.getFormattedBalance(getHoldingBalance());
		} catch (EconomyException e) {
			return "Error Accessing Bank Account";
		}
	}

	/**
	 * Attempt to delete the economy account.
	 */
	public void removeAccount() {

		TownyEconomyHandler.removeAccount(getEconomyName());

	}

}
