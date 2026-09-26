package com.ava.hamyarsakhteman;

public class ReportManager {
    public String unitReport(int unit){
        return "گزارش واحد " + unit + "\nشارژ پرداختی:\nقبض آب:\nقبض برق:\nمانده بدهی:";
    }

    public String monthlySummary(String month){
        return "گزارش ماه " + month + "\n"+
                "جمع شارژ دریافتی:\n"+
                "قبوض آب و برق عمومی:\n"+
                "هزینه های ساختمان:\n"+
                "مانده صندوق:";
    }

    public String smsText(int unit, String owner){
        return "مالک محترم واحد " + unit + " " + owner + "\nیادآوری پرداخت شارژ و قبوض ساختمان";
    }
}
