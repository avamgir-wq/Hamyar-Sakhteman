package com.ava.hamyarsakhteman;

public class WaterCalculator {
    public static double calculateMeteredUnit(double totalBill, double totalConsumption, double unitConsumption) {
        if (totalConsumption <= 0) return 0;
        return totalBill * unitConsumption / totalConsumption;
    }

    public static double calculateSharedUnits(double remainingBill, int unitCount) {
        if (unitCount <= 0) return 0;
        return remainingBill / unitCount;
    }
}
