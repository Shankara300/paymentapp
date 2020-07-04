package com.shankar.login;

import java.sql.Connection;
import java.util.Map;
import java.util.Scanner;

public class Login {
	public static Map<String,Boolean> loginProcess(Connection conn) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter credentials(Account number and password) to do this action");
		System.out.print("Enter your account number: ");
		String accno = sc.next();
		System.out.print("Enter password: ");
		String pswd = sc.next();
		LoginAction la = new LoginAction();
		return la.checkLogin(conn, accno, pswd);
	}
	

}
