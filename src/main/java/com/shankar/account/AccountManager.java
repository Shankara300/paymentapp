package com.shankar.account;

import java.sql.Connection;

import com.shankar.bean.Account;

public interface AccountManager {
	String createAccount(Connection conn, Account acc);
	
}
