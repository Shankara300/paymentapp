package com.shankar.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import com.shankar.bean.Account;
import com.shankar.bean.Transactions.TxnStatus;

public interface TransactionManager {
	boolean createAccountTxn(Connection conn, Account a);
	String addMoneyToAccountTxn(Connection conn, String fromAccount, String toAccount, double amount,
			TxnStatus status) throws SQLException;
	void changeStatusofTxn(Connection conn, String txnid, TxnStatus tx) throws SQLException;
	String removeMoneyFromAccount(Connection conn, String fromAccount, String toAccount, double debit) throws SQLException;
}
