package com.shankar.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.shankar.bean.Account;
import com.shankar.bean.Transactions.TxnStatus;
import com.shankar.exceptions.InsufficientBalanceException;
import com.shankar.exceptions.InvalidToAccountException;
import com.shankar.transaction.TransactionManagerImpl;

/**
 * This class is used to handle the CRUD operations of Account table.
 * 
 * @author shankarkumble3@gmail.com
 *
 */

/////////////////////////////////////////////////////////////////////////////////
//      CREATE TABLE Account (                                                 //
//          ActNum varchar(255) not null primary key,                          //
//          CustName varchar(255) not null,                                    //
//          UserId varchar(255) not null,                                      //
//          password varchar(255) not null,                                    //
//          balance varchar(255),                                              //
//          lastupdatedon datetime);                                           //
/////////////////////////////////////////////////////////////////////////////////

public class AccountManagerImpl implements AccountManager {

	static PreparedStatement preparedStmt = null;
	static TransactionManagerImpl tm = new TransactionManagerImpl();

	/**
	 * This Method creates the account in the DB.
	 */
	public String createAccount(Connection conn, Account acc) {

		try {
			String query = "insert into Account values ('" + acc.getAccountNumber() + "', '" + acc.getCustName()
					+ "', '" + acc.getUserId() + "', '" + acc.getPassword() + "', '" + acc.getInitialBalance() + "', '"
					+ acc.getLastUpdatedOn() + "')";

			preparedStmt = conn.prepareStatement(query);
			preparedStmt.execute();
			boolean createTxn = tm.createAccountTxn(conn, acc);

			// If creating entry in Transaction fails, remove the created record from
			// Account table also.
			if (!createTxn) {
				String deletequery = "delete from Account where ActNum = '" + acc.getAccountNumber();
				preparedStmt = conn.prepareStatement(deletequery);
				preparedStmt.execute();
			}
		} catch (SQLException e) {
			return "Account creation failed";
		}
		return "Congrats! Account created. Your account number is " + acc.getAccountNumber();
	}

	/**
	 * This Method credits the account with the supplied amount.
	 * 
	 * @param conn
	 * @param accountNumber
	 * @param amount
	 */
	public static void moveMoney(Connection conn, String fromAccount, String toAccount, double amount) {

		Timestamp ts = new Timestamp(new Date().getTime());
		String TxnId = null;
		double balance = 0;
		double updatedAmout = 0;
		try {
			TxnId = tm.addMoneyToAccountTxn(conn, fromAccount, toAccount, amount, TxnStatus.Pending);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}

		String getBalance = "select balance from Account where  ActNum=?";
		try {
			preparedStmt = conn.prepareStatement(getBalance);
			preparedStmt.setString(1, toAccount);
			ResultSet results = preparedStmt.executeQuery();
			if (results.next()) {
				balance = Double.valueOf(results.getString("balance"));
			}
		} catch (SQLException e) {
			System.out.println("Transaction failed");
		}

		updatedAmout = balance + amount;
		Timestamp tstamp = new Timestamp(new Date().getTime());
		String updateBalanceQuery = "update Account set balance = ? , lastupdatedon = ? where ActNum =?";
		// update balance now.
		Lock queuelock = new ReentrantLock();
		queuelock.lock();
		try {
			preparedStmt = conn.prepareStatement(updateBalanceQuery);
			preparedStmt.setDouble(1, updatedAmout);
			preparedStmt.setTimestamp(2, tstamp);
			preparedStmt.setString(3, toAccount);
			int execute = preparedStmt.executeUpdate();
			if (execute > 0)
				tm.changeStatusofTxn(conn, TxnId, TxnStatus.Success);
			else
				tm.changeStatusofTxn(conn, TxnId, TxnStatus.Failed);
		} catch (SQLException e) {

		} catch (Exception e) {

		}

		queuelock.unlock();
		System.out.println("Amount $" + amount + " got credited to your account " + toAccount
				+ ". Your current balance is $" + updatedAmout + ". Transaction Id: " + TxnId);
	}

	/**
	 * This method tranfers the amount from one account to other.
	 * 
	 * @param conn
	 * @param fromAccount
	 * @param toAccount
	 * @param amount
	 * @return
	 * @throws InsufficientBalanceException
	 * @throws InvalidToAccountException
	 */
	public static String transferMoney(Connection conn, String fromAccount, String toAccount, double amount)
			throws InsufficientBalanceException, InvalidToAccountException {
		System.out.println("Trying to transfer from " + fromAccount + " to " + toAccount);
		if (amount <= 0) {
			return "Please enter valid amount!";
		}

		// first check to transfer fromAccount hash sufficient balance
		double availBal = 0;
		String getBalance = "select balance from Account where  ActNum=?";
		try {
			preparedStmt = conn.prepareStatement(getBalance);
			preparedStmt.setString(1, fromAccount);
			ResultSet results = preparedStmt.executeQuery();
			if (results.next()) {
				availBal = Double.valueOf(results.getString("balance"));
			}

			if (amount > availBal) {
				throw new InsufficientBalanceException(
						"You can't transfer $" + amount + ". Your balance is $" + availBal);
			} else {
				// Check toAccount exists in DB. Put accoutnumber, balance in map.
				Map<String, Double> allAccounts = new ConcurrentHashMap<String, Double>();
				String getAllAccount = "select ActNum, balance from Account";
				preparedStmt = conn.prepareStatement(getAllAccount);
				ResultSet rs = preparedStmt.executeQuery();
				while (rs.next()) {
					allAccounts.put(rs.getString("ActNum"), rs.getDouble("balance"));
				}

				if (!allAccounts.containsKey(toAccount)) {
					throw new InvalidToAccountException("To account is invalid");
				}

				// Deduct amount from, fromAccount now.
				double finalAmountTodebit = availBal - amount;
				double finalAmountToCredit = allAccounts.get(toAccount) + amount;
				performsTransfer(conn, fromAccount, toAccount, finalAmountTodebit, finalAmountToCredit, amount);

			}

		} catch (SQLException e) {
			return "Transaction failed";
		}

		return "Transaction successfull";
	}

	/**
	 * Helper function to transfer the amount. While Transfering amount, we assume
	 * that charges and commission are applicable to sender.
	 * 
	 * @param conn
	 * @param fromAccount
	 * @param amount
	 * @param finalAmount
	 */
	private static void performsTransfer(Connection conn, String fromAccount, String toAccount,
			double finalAmountTodebit, double finalAmountToCredit, double amount) {
		Timestamp tstamp = new Timestamp(new Date().getTime());
		double chargeAndCommissionAmount = new ChargeAndCommissionCal().findChargeAndCommissionAmount(amount);
		String TxnId = null;
		try {
			TxnId = tm.removeMoneyFromAccount(conn, fromAccount, toAccount, amount);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return;
		}

		// Updating Accounts table.
		String updateDebit = "update Account set balance = ? , lastupdatedon = ? where ActNum =?";
		Lock queuelock = new ReentrantLock();
		double ammountToSaveForFrom = finalAmountTodebit - chargeAndCommissionAmount;
		queuelock.lock();
		int execute1 = 0, execute2 = 0;
		try {
			preparedStmt = conn.prepareStatement(updateDebit);
			preparedStmt.setDouble(1, ammountToSaveForFrom);
			preparedStmt.setTimestamp(2, tstamp);
			preparedStmt.setString(3, fromAccount);
			execute1 = preparedStmt.executeUpdate();
		} catch (SQLException e) {

		}

		String updateCredit = "update Account set balance = ? , lastupdatedon = ? where ActNum =?";
		try {
			Timestamp ttamp = new Timestamp(new Date().getTime());
			preparedStmt = conn.prepareStatement(updateCredit);
			preparedStmt.setDouble(1, finalAmountToCredit);
			preparedStmt.setTimestamp(2, ttamp);
			preparedStmt.setString(3, toAccount);
			execute2 = preparedStmt.executeUpdate();
		} catch (SQLException e) {

		}

		if (execute1 > 0 && execute2 > 0)
			try {
				tm.changeStatusofTxn(conn, TxnId, TxnStatus.Success);
			} catch (SQLException e) {
				// TODO Auto-generated catch block

			}
		else {
			try {
				tm.changeStatusofTxn(conn, TxnId, TxnStatus.Failed);
			} catch (SQLException e) {
				// TODO Auto-generated catch block

			}
		}

		queuelock.unlock();
		double d = amount + chargeAndCommissionAmount;
		System.out.println("Amount $" + d + " got debited from your account " + fromAccount
				+ ". Your current balance is $" + ammountToSaveForFrom + ". Charge and commission amount is $"
				+ chargeAndCommissionAmount + ". Transaction Id: " + TxnId);
		System.out.println("Amount $" + amount + " got credit to your account " + toAccount
				+ ". Your current balance is $" + finalAmountToCredit + ". Transaction Id: " + TxnId);

		// Add the chargeAndCommissionAmount to table ChargeAndCommission.
		TransactionManagerImpl.insertChargeAndCommission(conn, TxnId, chargeAndCommissionAmount);
	}

}
