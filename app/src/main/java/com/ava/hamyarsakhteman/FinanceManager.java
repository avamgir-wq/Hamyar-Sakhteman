package com.ava.hamyarsakhteman;

public class FinanceManager {
    public int monthlyCharge;
    public int publicElectricity;
    public int waterBill;
    public int totalExpenses;

    public FinanceManager(){
        monthlyCharge=0;
        publicElectricity=0;
        waterBill=0;
        totalExpenses=0;
    }

    public int getBuildingBalance(){
        return monthlyCharge - publicElectricity - waterBill - totalExpenses;
    }
}
