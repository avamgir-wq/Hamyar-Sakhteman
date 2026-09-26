package com.ava.hamyarsakhteman;

public class ChargeManager {
    public static double calculateBalance(double received, double expenses) {
        return received - expenses;
    }

    public static double calculateWaterShare(double totalBill, double totalMeterUsage, double unitUsage, boolean hasSubMeter) {
        if (hasSubMeter && totalMeterUsage > 0) {
            return totalBill * (unitUsage / totalMeterUsage);
        }
        return 0;
    }
}
