package com.ava.hamyarsakhteman;

public class DashboardUnits {
    public static final int TOTAL_UNITS = 12;
    public static final String BUILDING = "بلوک A1 مجتمع فرهیختگان";

    public static String[] units(){
        String[] result = new String[TOTAL_UNITS];
        for(int i=0;i<TOTAL_UNITS;i++){
            result[i] = "واحد " + (i+1);
        }
        return result;
    }
}
