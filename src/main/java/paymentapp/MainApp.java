package paymentapp;

import java.sql.Connection;
import java.sql.DriverManager;

import com.shankar.exceptions.AccountCreationPasswordException;
import com.shankar.transactionList.TransactionOptions;

public class MainApp {
	static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	static final String DB_URL = "jdbc:sqlserver://localhost;databaseName=paymentapp";
	static final String USER = "sa";
	static final String PASS = "RedPrairie1";

	public static void main(String[] args) {
		// Connect to database first
		Connection conn = null;
		try {
			Class.forName(JDBC_DRIVER);
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected");
		} catch (Exception e) {
			System.out.println("Problem in connecting to DB");
			System.exit(0);
		}

		try {
			new TransactionOptions().ListingActions(conn);
		} catch (AccountCreationPasswordException e) {
			System.out.println(e.getMessage());
		}
	}
}
