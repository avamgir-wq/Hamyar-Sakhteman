package com.ava.hamyarsakhteman;

public class ReportManager {
    public String unitReport(int unit){
        return "گزارش واحد " + unit + "\nشارژ پرداختی:\nقبض آب:\nقبض برق:\nمانده بدهی:";
    }

    public String smsText(int unit, String owner){
        return "مالک محترم واحد " + unit + " " + owner + "\nیادآوری پرداخت شارژ و قبوض ساختمان";
    }
}
