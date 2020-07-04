package com.shankar.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	 * This method
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
		Map<String, List<String>> myMaps = new TreeMap<String, List<String>>();
		String query = "Select TxnNum, FromActNum, ToActNum, Amount, TxnStatus, TxnTime from Transactions where "
				+ "FromActNum = ? or ToActNum = ?";
		try {
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, accountNumber);
			preparedStmt.setString(2, accountNumber);
			ResultSet results = preparedStmt.executeQuery();
			while (results.next()) {
				ArrayList<String> list = new ArrayList<String>();
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
		System.out.println("---------------------------------------------------------------------------------------------");
		System.out.println("Txn Id           |From Account Num  | To Account Number| Amount| Txn Status| Txn Time");
		System.out.println("---------------------------------------------------------------------------------------------");
		for (Map.Entry<String, List<String>> map : myMaps.entrySet()) {
			List<String> details = map.getValue();
			System.out.println(map.getKey() + "| " + details.get(0) + "| " + details.get(1) + "| " + details.get(2)
					+ "| " + details.get(3) + "| " + details.get(4));
		}
		System.out.println("----------------------------------------------------------------------------------------------");
	}

	/**
	 * This method is used to reverse the transaction. It deletes the rows from Transactions table and reverts the
	 *  amount in Accounts table.
	 * @param conn
	 * @param txnId
	 */
	public static void reverseTransaction(Connection conn, String txnId) {
		// First delete the entry from ChargeAndCommission if any, since it's reffering Transactions table
		String query = "delete from ChargeAndCommission where TxnNum = ?";
		try {
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, txnId);
			preparedStmt.executeUpdate();
		} catch(SQLException e) {
			
		}
		
		// now get the amount from Transactions table
		double amount = 0;
		String query2 = "select Amount from Transactions where TxnNum = ?";
		try {
			preparedStmt = conn.prepareStatement(query2);
			preparedStmt.setString(1, txnId);
			ResultSet results = preparedStmt.executeQuery();
			if (results.next()) {
				amount = results.getDouble(0);
				System.out.println(amount);
			}
		} catch(SQLException e) {
		}
		
		// add the amout back to senders account and deduct from recievers account.
		
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	// CREATE TABLE ChargeAndCommission (                                                    //
	//     TxnNum varchar(255) not null foreign key references Transactions(TxnNum),         //
	//     Amount varchar(255))                                                              //
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
