package ir.avamedia.hamyarbuilding.a1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class PersianDate {
    private static final String[] MONTHS = {
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    };

    private PersianDate() {}

    public static final class Jalali {
        public final int year;
        public final int month;
        public final int day;

        public Jalali(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }

    public static Jalali today() {
        Calendar c = Calendar.getInstance();
        return gregorianToJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    public static String currentMonthKey() {
        Jalali j = today();
        return monthKey(j.year, j.month);
    }

    public static String todayKey() {
        Jalali j = today();
        return String.format(java.util.Locale.US, "%04d/%02d/%02d", j.year, j.month, j.day);
    }

    public static String monthKey(int year, int month) {
        return String.format(java.util.Locale.US, "%04d/%02d", year, month);
    }

    public static String monthLabel(String key) {
        try {
            String[] p = key.split("/");
            int y = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            if (m >= 1 && m <= 12) return MONTHS[m - 1] + " " + toFaDigits(String.valueOf(y));
        } catch (Exception ignored) {}
        return key;
    }

    public static List<String> nearbyMonthKeys() {
        Jalali now = today();
        List<String> list = new ArrayList<>();
        int serial = now.year * 12 + (now.month - 1);
        for (int d = -18; d <= 18; d++) {
            int s = serial + d;
            int y = floorDiv(s, 12);
            int m = floorMod(s, 12) + 1;
            list.add(monthKey(y, m));
        }
        return list;
    }

    private static int floorDiv(int a, int b) {
        int q = a / b;
        int r = a % b;
        if (r != 0 && ((a ^ b) < 0)) q--;
        return q;
    }

    private static int floorMod(int a, int b) {
        return a - floorDiv(a, b) * b;
    }

    public static String toFaDigits(String s) {
        if (s == null) return "";
        char[] en = "0123456789".toCharArray();
        char[] fa = "۰۱۲۳۴۵۶۷۸۹".toCharArray();
        String out = s;
        for (int i = 0; i < 10; i++) out = out.replace(en[i], fa[i]);
        return out;
    }

    public static String normalizeDigits(String s) {
        if (s == null) return "";
        String out = s.trim();
        String fa = "۰۱۲۳۴۵۶۷۸۹";
        String ar = "٠١٢٣٤٥٦٧٨٩";
        for (int i = 0; i < 10; i++) {
            out = out.replace(fa.charAt(i), (char) ('0' + i));
            out = out.replace(ar.charAt(i), (char) ('0' + i));
        }
        return out;
    }

    public static Jalali gregorianToJalali(int gy, int gm, int gd) {
        int[] gdm = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        int gy2 = (gm > 2) ? gy + 1 : gy;
        int days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1];
        int jy = -1595 + (33 * (days / 12053));
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }
        int jm;
        int jd;
        if (days < 186) {
            jm = 1 + (days / 31);
            jd = 1 + (days % 31);
        } else {
            jm = 7 + ((days - 186) / 30);
            jd = 1 + ((days - 186) % 30);
        }
        return new Jalali(jy, jm, jd);
    }
}
