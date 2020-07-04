package com.shankar.bean;

import java.sql.Timestamp;
import java.util.Date;

public class Account {

	private String accountNumber;
	private String custName;
	private String userId;
	private String password;
	private double initialBalance;
	private Timestamp lastUpdatedOn;

	public Account(String custName, String userId, String password, double initialBalance) {
		super();
		// Auto generated account number starts with "A".
		this.accountNumber = 'A' + String.valueOf(System.nanoTime());
		this.custName = custName;
		this.userId = userId;
		this.password = password;
		this.initialBalance = initialBalance;
		this.lastUpdatedOn = new Timestamp(new Date().getTime());
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public double getInitialBalance() {
		return initialBalance;
	}

	public void setInitialBalance(double initialBalance) {
		this.initialBalance = initialBalance;
	}

	public Timestamp getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Timestamp lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	@Override
	public String toString() {
		return "Account [accountNumber=" + accountNumber + ", custName=" + custName + ", userId=" + userId
				+ ", password=" + password + ", initialBalance=" + initialBalance + ", lastUpdatedOn=" + lastUpdatedOn
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
		result = prime * result + ((custName == null) ? 0 : custName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(initialBalance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((lastUpdatedOn == null) ? 0 : lastUpdatedOn.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (accountNumber == null) {
			if (other.accountNumber != null)
				return false;
		} else if (!accountNumber.equals(other.accountNumber))
			return false;
		if (custName == null) {
			if (other.custName != null)
				return false;
		} else if (!custName.equals(other.custName))
			return false;
		if (Double.doubleToLongBits(initialBalance) != Double.doubleToLongBits(other.initialBalance))
			return false;
		if (lastUpdatedOn == null) {
			if (other.lastUpdatedOn != null)
				return false;
		} else if (!lastUpdatedOn.equals(other.lastUpdatedOn))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

}