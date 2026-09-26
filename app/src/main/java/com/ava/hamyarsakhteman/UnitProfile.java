package com.ava.hamyarsakhteman;

public class UnitProfile {
    public int unitNumber;
    public String ownerName = "";
    public String phone = "";
    public String tenantName = "";
    public String parkingNumber = "";
    public String storageNumber = "";
    public boolean hasSubWaterMeter = false;
    public double waterConsumption = 0;
    public double chargeBalance = 0;

    public UnitProfile(int number){
        this.unitNumber = number;
    }
}
