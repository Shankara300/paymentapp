package com.shankar.account;

public class ChargeAndCommissionCal {
	private static final double chargeRate = 0.2 / 100;
	private static final double commissionRate = 0.05 / 100;

	public static double findChargeAndCommissionAmount(double amount) {
		return amount * chargeRate + amount * commissionRate;
	}

}
