package com.shankar.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.shankar.account.ChargeAndCommissionCal;
import com.shankar.bean.Account;
import com.shankar.bean.Transactions;
import com.shankar.bean.Transactions.TxnStatus;

/**
 * This class is used to handle the CRUD operations of Transactions table.
 * 
 * @author shankarkumble3@gmail.com
 *
 */

/////////////////////////////////////////////////////////////////////////////////
//     	CREATE TABLE Transactions (                                            //
//          TxnNum varchar(255) not null primary key,                          //
//          FromActNum varchar(255) not null references Account(ActNum),       //
//          ToActNum varchar(255) not null references Account(ActNum),         //
//          Amount varchar(255),                                               //
//          TxnStatus varchar(255),                                            //
//          TxnTime datetime);                                                //
/////////////////////////////////////////////////////////////////////////////////

public class TransactionManagerImpl implements TransactionManager {

	static PreparedStatement preparedStmt = null;

	/**
	 * This method inserts record in to Transactions table when account gets
	 * created.
	 * 
	 * @param conn
	 * @param acc
	 * @return
	 */
	public boolean createAccountTxn(Connection conn, Account acc) {
		// Transaction number will be nano seconds value starting with character T.
		Transactions ts = new Transactions(acc.getAccountNumber(), acc.getAccountNumber(), acc.getInitialBalance(),
				TxnStatus.Success);
		try {
			String query = "insert into Transactions values ('" + ts.getTxnNum() + "', '" + acc.getAccountNumber()
					+ "', '" + acc.getAccountNumber() + "' ,'" + acc.getInitialBalance() + "', '" + TxnStatus.Success
					+ "', + '" + acc.getLastUpdatedOn() + "')";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.execute();
		} catch (SQLException e) {
			return false;
		}

		return true;
	}

	/**
	 * This method records the transaction when user adds money to his account.
	 * 
	 * @param conn
	 * @param accountNumber
	 * @param amount
	 * @param status
	 * @return
	 * @throws SQLException
	 */
	public String addMoneyToAccountTxn(Connection conn, String fromAccount, String toAccount, double amount,
			TxnStatus status) throws SQLException {
		String txnNumber = 'T' + String.valueOf(System.nanoTime());
		Timestamp ts = new Timestamp(new Date().getTime());

		String query = "insert into Transactions values ('" + txnNumber + "', '" + fromAccount + "', '" + toAccount
				+ "','" + amount + "', '" + status + "', + '" + ts + "')";
		preparedStmt = conn.prepareStatement(query);
		preparedStmt.execute();

		return txnNumber;
	}

	/**
	 * @param conn
	 * @param txnId
	 * @param status
	 * @throws SQLException
	 */
	public void changeStatusofTxn(Connection conn, String txnId, TxnStatus status) throws SQLException {
		String updateStatus = "update Transactions set TxnStatus=? where TxnNum=?";
		preparedStmt = conn.prepareStatement(updateStatus);
		preparedStmt.setString(1, String.valueOf(status));
		preparedStmt.setString(2, txnId);
		preparedStmt.executeQuery();
	}

	/**
	 * This method is used to check the status of the transaction. User can check
	 * status of a transaction when he is part of it. I.e either recieving party or
	 * sending party.
	 * 
	 * @param conn
	 * @param accountNumber
	 * @param txnId
	 * @return
	 * @throws SQLException
	 */
	public static String checkStatus(Connection conn, String accountNumber, String txnId) throws SQLException {
		String getTxnStatus = "select TxnStatus from Transactions where TxnNum=? and (FromActNum = ? or ToActNum = ?)";
		preparedStmt = conn.prepareStatement(getTxnStatus);
		preparedStmt.setString(1, txnId);
		preparedStmt.setString(2, accountNumber);
		preparedStmt.setString(3, accountNumber);
		ResultSet results = preparedStmt.executeQuery();
		if (results.next()) {
			return results.getString("TxnStatus");
		} else {
			return "No such transaction exists for your account!";
		}
	}

	/**
	 * @param conn
	 * @param fromAccount
	 * @param toAccount
	 * @param debit
	 * @return
	 * @throws SQLException
	 */
	public String removeMoneyFromAccount(Connection conn, String fromAccount, String toAccount, double debit)
			throws SQLException {
		String txnNumber = 'T' + String.valueOf(System.nanoTime());
		Timestamp ts = new Timestamp(new Date().getTime());

		String query = "insert into Transactions values ('" + txnNumber + "', '" + fromAccount + "', '" + toAccount
				+ "','" + debit + "', 'Pending', + '" + ts + "')";
		preparedStmt = conn.prepareStatement(query);
		preparedStmt.execute();
		return txnNumber;
	}

	/**
	 * @param conn
	 * @param accountNumber
	 */
	public static void getStatement(Connection conn, String accountNumber) {
		Map<String, List<String>> myMaps = new HashMap<>();
		String query = "Select TxnNum, FromActNum, ToActNum, Amount, TxnStatus, TxnTime from Transactions where "
				+ "FromActNum = ? or ToActNum = ?";
		try {
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, accountNumber);
			preparedStmt.setString(2, accountNumber);
			ResultSet results = preparedStmt.executeQuery();
			while (results.next()) {
				ArrayList<String> list = new ArrayList<>();
				list.add(results.getString("FromActNum"));
				list.add(results.getString("ToActNum"));
				list.add(String.valueOf(results.getDouble("Amount")));
				list.add(results.getString("TxnStatus"));
				list.add(String.valueOf(results.getTimestamp("TxnTime")));
				myMaps.put(results.getString("TxnNum"), list);
			}
		} catch (SQLException e) {
			System.out.println("Transaction failed");
		}
		System.out.println(
				"---------------------------------------------------------------------------------------------");
		System.out.println("Txn Id           |From Account Num  | To Account Number| Amount| Txn Status| Txn Time");
		System.out.println(
				"---------------------------------------------------------------------------------------------");
		for (Map.Entry<String, List<String>> map : myMaps.entrySet()) {
			List<String> details = map.getValue();
			System.out.println(map.getKey() + "| " + details.get(0) + "| " + details.get(1) + "| " + details.get(2)
					+ "| " + details.get(3) + "| " + details.get(4));
		}
		System.out.println(
				"----------------------------------------------------------------------------------------------");
	}

	/**
	 * This method is used to reverse the transaction. It deletes the rows from
	 * Transactions table and reverts the amount in Accounts table. A transaction
	 * can be reversed only if money is transfered from one account to other.
	 * 
	 * @param conn
	 * @param txnId
	 */
	public static void reverseTransaction(Connection conn, String txnId) {

		double amountToReverse = 0;
		String fromAccountNum = null;
		String toAccountNum = null;
		String query1 = "select FromActNum, ToActNum, Amount from Transactions where TxnNum = ?";
		try {
			preparedStmt = conn.prepareStatement(query1);
			preparedStmt.setString(1, txnId);
			ResultSet results = preparedStmt.executeQuery();
			while (results.next()) {
				amountToReverse = results.getDouble("Amount");
				fromAccountNum = results.getString("FromActNum");
				toAccountNum = results.getString("ToActNum");
				if (fromAccountNum.equals(toAccountNum)) {
					System.out.println("This transaction was self added. Reverting back not possible");
					return;
				}
			}
		} catch (SQLException e) {
		}

		// First delete the entry from ChargeAndCommission if any, since it's reffering
		// Transactions table
		String query2 = "delete from ChargeAndCommission where TxnNum = ?";
		try {
			preparedStmt = conn.prepareStatement(query2);
			preparedStmt.setString(1, txnId);
			preparedStmt.executeUpdate();
		} catch (SQLException e) {

		}

		// Delete TxnId from Transactions
		String query3 = "delete from Transactions where TxnNum = ?";
		try {
			preparedStmt = conn.prepareStatement(query3);
			preparedStmt.setString(1, txnId);
			preparedStmt.executeUpdate();
		} catch (SQLException e) {

		}

		String TxnId = null;
		TransactionManagerImpl tm = new TransactionManagerImpl();
		try {
			TxnId = tm.removeMoneyFromAccount(conn, fromAccountNum, toAccountNum, amountToReverse);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return;
		}
		
		// First, get the current balances of both accounts.
		String query = "select ActNum, balance from Account";
		Map<String, Double> allAccounts = new ConcurrentHashMap<String, Double>();
		try {
			preparedStmt = conn.prepareStatement(query);
			ResultSet rs = preparedStmt.executeQuery();
			while (rs.next()) {
				allAccounts.put(rs.getString("ActNum"), rs.getDouble("balance"));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		double fromBalance = allAccounts.get(fromAccountNum);
		double toBalance = allAccounts.get(toAccountNum);
		double fromAmount = amountToReverse - ChargeAndCommissionCal.findChargeAndCommissionAmount(amountToReverse);

		fromBalance = fromAmount + fromBalance;
		toBalance = toBalance - amountToReverse;
		// Update fromAccountNum with fromAmount value and and toAccountNum with amount.
		String fromAccountUpdate = "update Account set balance = ? , lastupdatedon = ? where ActNum =?";
		Timestamp tstamp = new Timestamp(new Date().getTime());
		Lock queuelock = new ReentrantLock();
		queuelock.lock();
		int execute1 = 0, execute2 = 0;
		try {
			preparedStmt = conn.prepareStatement(fromAccountUpdate);
			preparedStmt.setDouble(1, fromBalance);
			preparedStmt.setTimestamp(2, tstamp);
			preparedStmt.setString(3, fromAccountNum);
			execute1 = preparedStmt.executeUpdate();
		} catch (SQLException e) {

		}

		String toAccountUpdate = "update Account set balance = ? , lastupdatedon = ? where ActNum =?";
		try {
			preparedStmt = conn.prepareStatement(toAccountUpdate);
			preparedStmt.setDouble(1, toBalance);
			preparedStmt.setTimestamp(2, tstamp);
			preparedStmt.setString(3, toAccountNum);
			execute2 = preparedStmt.executeUpdate();
		} catch (SQLException e) {

		}

		if (execute1 > 0 && execute2 > 0)
			try {
				tm.changeStatusofTxn(conn, TxnId, TxnStatus.ReversedSuccess);
			} catch (SQLException e) {

			}
		else {
			try {
				tm.changeStatusofTxn(conn, TxnId, TxnStatus.ReversedFailure);
			} catch (SQLException e) {

			}
		}

		queuelock.unlock();
		System.out.println("Transaction has been reversed.");
		System.out.println(
				"Amount $" + amountToReverse + " got debited from your account " + toAccountNum + 
				". Your current balance is "+toBalance+
				". Transaction Id: " + TxnId);
		System.out.println("Amount $" + fromAmount + " got credited to your account " + fromAccountNum + 
				". Your current balance is "+ fromBalance
				+ ". Transaction Id: " + TxnId);
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//         CREATE TABLE ChargeAndCommission (                                            //
	//             TxnNum varchar(255) not null foreign key references Transactions(TxnNum), //
	//             Amount varchar(255))                                                      //
	///////////////////////////////////////////////////////////////////////////////////////////
	public static void insertChargeAndCommission(Connection conn, String txnId, double chargeAndCommissionAmount) {
		String query = "insert into ChargeAndCommission values ('" + txnId + "', '" + chargeAndCommissionAmount + "')";

		try {
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
