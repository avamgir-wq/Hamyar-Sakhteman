package com.ava.hamyarsakhteman;

public class FinancialDashboard {
    public double totalChargeReceived;
    public double totalWaterBill;
    public double totalElectricBill;
    public double totalExpenses;

    public double getBuildingBalance(){
        return totalChargeReceived - totalWaterBill - totalElectricBill - totalExpenses;
    }
}
