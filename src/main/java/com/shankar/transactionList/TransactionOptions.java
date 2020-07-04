package com.shankar.transactionList;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.shankar.account.AccountManagerImpl;
import com.shankar.account.ChargeAndCommissionCal;
import com.shankar.bean.Account;
import com.shankar.exceptions.AccountCreationPasswordException;
import com.shankar.exceptions.InsufficientBalanceException;
import com.shankar.exceptions.InvalidToAccountException;
import com.shankar.login.Login;
import com.shankar.transaction.TransactionManagerImpl;

/**
 * This class is used to select the list of all options available.
 * 
 * @author shankarkumble3@gmail.com
 *
 */
public class TransactionOptions {
	static Login la = new Login();
	static Map<String, Boolean> checkLogin;

	public void ListingActions(Connection conn) throws AccountCreationPasswordException {
		System.out.println("1. Create Account(Sign-up)");
		System.out.println("2. Add Money to wallet");
		System.out.println("3. Transfer amount");
		System.out.println("4. Compute charges and commission");
		System.out.println("5. Check status of my transaction");
		System.out.println("6. Reversal of transaction");
		System.out.println("7. View Passbook of the user");
		System.out.println("8. Exit");

		boolean enter = true;
		while (enter) {
			System.out.print("Please enter your choice: ");
			Scanner sc = new Scanner(System.in);
			int choice = sc.nextInt();

			switch (choice) {
			case 1:
				System.out.println("Creating account..");
				System.out.print("Please enter your username: ");
				String userName = sc.next();
				System.out.print("Please enter the userId: ");
				String userId = sc.next();
				System.out.print("Please enter a password: ");
				String password = sc.next();
				System.out.print("Confirm the password: ");
				String password2 = sc.next();
				if (!password.equals(password2)) {
					throw new AccountCreationPasswordException("Entered passwords must be same!");
				}

				System.out.print("Please enter the initial balance: ");
				double initialbal = sc.nextDouble();
				Account a = new Account(userName, userId, password, initialbal);
				String createAccountResult = new AccountManagerImpl().createAccount(conn, a);
				System.out.println(createAccountResult);
				break;

			case 2:
				checkLogin = Login.loginProcess(conn);
				Entry<String, Boolean> entry2 = checkLogin.entrySet().iterator().next();
				if (entry2.getValue()) {
					System.out.println("Login Successfull...");
					System.out.println("Enter the amount you want to add: ");
					int amt = sc.nextInt();
					// self moving, so from and to account are same.
					AccountManagerImpl.moveMoney(conn, entry2.getKey(), entry2.getKey(), amt);
				} else {
					System.out.println("Your credentials are wrong");
				}
				break;

			case 3:
				checkLogin = Login.loginProcess(conn);
				Entry<String, Boolean> entry3 = checkLogin.entrySet().iterator().next();
				if (entry3.getValue()) {
					System.out.println("Login Successfull...");
					System.out.print("Enter to account number: ");
					String toAccount = sc.next();
					System.out.print("Enter the amount you wish to transfer: ");
					double amount = sc.nextDouble();
					try {
						AccountManagerImpl.transferMoney(conn, entry3.getKey(), toAccount, amount);
					} catch (InsufficientBalanceException e) {
						System.out.println("Insufficient balance to transfer amount!");
					} catch (InvalidToAccountException e) {
						System.out.println("To account is invalid");
					}
				} else {
					System.out.println("Your credentials are wrong");
				}
				break;

			case 4:
				System.out.print("Enter the amount for which charges and commision needs to be calculated:");
				double amount = sc.nextDouble();
				System.out.println("Charges and commission for transaction amount $" + amount + " is $"
						+ new ChargeAndCommissionCal().findChargeAndCommissionAmount(amount));
				break;

			case 5:
				checkLogin = Login.loginProcess(conn);
				Entry<String, Boolean> entry5 = checkLogin.entrySet().iterator().next();
				if (entry5.getValue()) {
					System.out.println("Login Successfull...");
					System.out.print("Enter the Transaction Id: ");
					String txnId = sc.next();
					try {
						System.out.println("Transaction status: "
								+ TransactionManagerImpl.checkStatus(conn, entry5.getKey(), txnId));
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("Your credentials are wrong");
				}
				break;

			case 6:
				System.out.println("Please note that commision and service charges will not be credited back");
				System.out.print("Enter transaction Id which you want to reverse: ");
				String txnId = sc.next();
				TransactionManagerImpl.reverseTransaction(conn, txnId);
				break;

			case 7:
				checkLogin = Login.loginProcess(conn);
				Entry<String, Boolean> entry7 = checkLogin.entrySet().iterator().next();
				if (entry7.getValue()) {
					System.out.println(
							"Login Successfull...Retrieving all transaction details of account " + entry7.getKey());
					TransactionManagerImpl.getStatement(conn, entry7.getKey());
				} else {
					System.out.println("Your credentials are wrong");
				}
				break;

			case 8:
				enter = false;
			}
		}

		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
