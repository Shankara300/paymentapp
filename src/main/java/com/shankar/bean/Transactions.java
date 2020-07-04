package com.shankar.bean;

import java.sql.Timestamp;
import java.util.Date;

public class Transactions {

	private String TxnNum;
	private String FromActNum;
	private String ToActNum;
	private double Amount;
	private TxnStatus txnStat;
	private Timestamp TxnTime;

	public Transactions(String FromActNum, String ToActNum, double Amount, TxnStatus txnStat) {
		this.TxnNum = 'T' + String.valueOf(System.nanoTime());
		this.FromActNum = FromActNum;
		this.ToActNum = ToActNum;
		this.Amount = Amount;
		this.txnStat = txnStat;
		this.TxnTime = new Timestamp(new Date().getTime());
	}

	public enum TxnStatus {
		Pending, Failed, Success, ReversedSuccess,ReversedFailure
	}

	@Override
	public String toString() {
		return "Transactions [TxnNum=" + TxnNum + ", FromActNum=" + FromActNum + ", ToActNum=" + ToActNum + ", Amount="
				+ Amount + ", txnStat=" + txnStat + ", TxnTime=" + TxnTime + "]";
	}

	public String getTxnNum() {
		return TxnNum;
	}

	public void setTxnNum(String txnNum) {
		TxnNum = txnNum;
	}

	public String getFromActNum() {
		return FromActNum;
	}

	public void setFromActNum(String fromActNum) {
		FromActNum = fromActNum;
	}

	public String getToActNum() {
		return ToActNum;
	}

	public void setToActNum(String toActNum) {
		ToActNum = toActNum;
	}

	public double getAmount() {
		return Amount;
	}

	public void setAmount(double amount) {
		Amount = amount;
	}

	public TxnStatus getTxnStat() {
		return txnStat;
	}

	public void setTxnStat(TxnStatus txnStat) {
		this.txnStat = txnStat;
	}

	public Timestamp getTxnTime() {
		return TxnTime;
	}

	public void setTxnTime(Timestamp txnTime) {
		TxnTime = txnTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(Amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((FromActNum == null) ? 0 : FromActNum.hashCode());
		result = prime * result + ((ToActNum == null) ? 0 : ToActNum.hashCode());
		result = prime * result + ((TxnNum == null) ? 0 : TxnNum.hashCode());
		result = prime * result + ((TxnTime == null) ? 0 : TxnTime.hashCode());
		result = prime * result + ((txnStat == null) ? 0 : txnStat.hashCode());
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
		Transactions other = (Transactions) obj;
		if (Double.doubleToLongBits(Amount) != Double.doubleToLongBits(other.Amount))
			return false;
		if (FromActNum == null) {
			if (other.FromActNum != null)
				return false;
		} else if (!FromActNum.equals(other.FromActNum))
			return false;
		if (ToActNum == null) {
			if (other.ToActNum != null)
				return false;
		} else if (!ToActNum.equals(other.ToActNum))
			return false;
		if (TxnNum == null) {
			if (other.TxnNum != null)
				return false;
		} else if (!TxnNum.equals(other.TxnNum))
			return false;
		if (TxnTime == null) {
			if (other.TxnTime != null)
				return false;
		} else if (!TxnTime.equals(other.TxnTime))
			return false;
		if (txnStat != other.txnStat)
			return false;
		return true;
	}

}
