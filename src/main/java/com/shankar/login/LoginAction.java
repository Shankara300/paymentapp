package com.shankar.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoginAction {
	/**
	 * @param conn
	 * @param accountNumber
	 * @param password
	 * @return map with accountnumber and result of login.
	 */
	public Map<String, Boolean> checkLogin(Connection conn, String accountNumber, String password) {
		String query = "select UserId from Account where  ActNum=? and password=?";
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(query);
			ps.setString(1, accountNumber);
			ps.setString(2, password);
			ResultSet results = ps.executeQuery();
			if (results.next()) {
				map.put(accountNumber, true);
				return map;
			} else {
				map.put(accountNumber, false);
				return map;
			}
		} catch (SQLException e1) {
			map.put(accountNumber, false);
			return map;
		}

	}
}
