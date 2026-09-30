package ir.avamedia.hamyarbuilding.a1;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataStore {
    private static final String PREFS = "hamyar_a1_data_v2";
    private static final String KEY_DATA = "data";

    public static final class UnitInfo {
        public int number;
        public int floor;
        public String owner = "";
        public String ownerPhone = "";
        public String tenant = "";
        public String tenantPhone = "";
        public int occupants = 1;
        public boolean submeter = false;
        public double lastWaterReading = 0d;
        public String notes = "";

        public UnitInfo copy() {
            UnitInfo x = new UnitInfo();
            x.number = number;
            x.floor = floor;
            x.owner = owner;
            x.ownerPhone = ownerPhone;
            x.tenant = tenant;
            x.tenantPhone = tenantPhone;
            x.occupants = occupants;
            x.submeter = submeter;
            x.lastWaterReading = lastWaterReading;
            x.notes = notes;
            return x;
        }
    }

    public static final class MonthData {
        public String key = "";
        public long chargeAmount = 0L;
        public long waterBillAmount = 0L;
        public double mainWaterConsumption = 0d;
        public final Map<Integer, Boolean> chargePaid = new HashMap<>();
        public final Map<Integer, Boolean> waterPaid = new HashMap<>();
        public final Map<Integer, Long> waterShare = new HashMap<>();
        public final Map<Integer, Double> waterConsumption = new HashMap<>();
        public final Map<Integer, Double> waterPrevious = new HashMap<>();
        public final Map<Integer, Double> waterCurrent = new HashMap<>();
    }

    public static final class Expense {
        public long id;
        public String monthKey = "";
        public String title = "";
        public String date = "";
        public long amount;
    }

    public static final class Reading {
        public double previous;
        public double current;

        public Reading(double previous, double current) {
            this.previous = previous;
            this.current = current;
        }
    }

    public static final class WaterCalculation {
        public long totalAmount;
        public double totalConsumption;
        public double meteredConsumption;
        public long meteredAmount;
        public long unmeteredAmount;
        public final Map<Integer, Long> shares = new HashMap<>();
        public final Map<Integer, Double> consumptions = new HashMap<>();
    }

    private final SharedPreferences prefs;
    private final List<UnitInfo> units = new ArrayList<>();
    private final Map<String, MonthData> months = new HashMap<>();
    private final List<Expense> expenses = new ArrayList<>();
    private long initialFund = 0L;
    private String smsTemplate = "مالک/ساکن محترم {نام}، بدهی واحد {واحد} بابت شارژ و آب {ماه} مبلغ {مبلغ} تومان است. با تشکر - مدیریت بلوک A1 مجتمع فرهیختگان";

    public DataStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        units.clear();
        months.clear();
        expenses.clear();
        String raw = prefs.getString(KEY_DATA, null);
        if (raw == null || raw.trim().isEmpty()) {
            createDefaults();
            save();
            return;
        }
        try {
            JSONObject root = new JSONObject(raw);
            initialFund = root.optLong("initialFund", 0L);
            smsTemplate = root.optString("smsTemplate", smsTemplate);

            JSONArray u = root.optJSONArray("units");
            if (u != null) {
                for (int i = 0; i < u.length(); i++) {
                    JSONObject o = u.optJSONObject(i);
                    if (o == null) continue;
                    UnitInfo x = new UnitInfo();
                    x.number = o.optInt("number", i + 1);
                    x.floor = o.optInt("floor", ((x.number - 1) / 3) + 1);
                    x.owner = o.optString("owner", "");
                    x.ownerPhone = o.optString("ownerPhone", "");
                    x.tenant = o.optString("tenant", "");
                    x.tenantPhone = o.optString("tenantPhone", "");
                    x.occupants = Math.max(0, o.optInt("occupants", 1));
                    x.submeter = o.optBoolean("submeter", false);
                    x.lastWaterReading = o.optDouble("lastWaterReading", 0d);
                    x.notes = o.optString("notes", "");
                    units.add(x);
                }
            }

            JSONObject m = root.optJSONObject("months");
            if (m != null) {
                JSONArray names = m.names();
                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        String key = names.optString(i, "");
                        JSONObject mo = m.optJSONObject(key);
                        if (mo == null) continue;
                        MonthData md = new MonthData();
                        md.key = key;
                        md.chargeAmount = mo.optLong("chargeAmount", 0L);
                        md.waterBillAmount = mo.optLong("waterBillAmount", 0L);
                        md.mainWaterConsumption = mo.optDouble("mainWaterConsumption", 0d);
                        readBoolMap(mo.optJSONObject("chargePaid"), md.chargePaid);
                        readBoolMap(mo.optJSONObject("waterPaid"), md.waterPaid);
                        readLongMap(mo.optJSONObject("waterShare"), md.waterShare);
                        readDoubleMap(mo.optJSONObject("waterConsumption"), md.waterConsumption);
                        readDoubleMap(mo.optJSONObject("waterPrevious"), md.waterPrevious);
                        readDoubleMap(mo.optJSONObject("waterCurrent"), md.waterCurrent);
                        months.put(key, md);
                    }
                }
            }

            JSONArray e = root.optJSONArray("expenses");
            if (e != null) {
                for (int i = 0; i < e.length(); i++) {
                    JSONObject o = e.optJSONObject(i);
                    if (o == null) continue;
                    Expense ex = new Expense();
                    ex.id = o.optLong("id", System.currentTimeMillis() + i);
                    ex.monthKey = o.optString("monthKey", "");
                    ex.title = o.optString("title", "");
                    ex.date = o.optString("date", "");
                    ex.amount = o.optLong("amount", 0L);
                    expenses.add(ex);
                }
            }

            normalizeUnits();
        } catch (Exception ignored) {
            createDefaults();
            save();
        }
    }

    private static void readBoolMap(JSONObject o, Map<Integer, Boolean> out) {
        if (o == null) return;
        JSONArray names = o.names();
        if (names == null) return;
        for (int i = 0; i < names.length(); i++) {
            String k = names.optString(i, "");
            try { out.put(Integer.parseInt(k), o.optBoolean(k, false)); } catch (Exception ignored) {}
        }
    }

    private static void readLongMap(JSONObject o, Map<Integer, Long> out) {
        if (o == null) return;
        JSONArray names = o.names();
        if (names == null) return;
        for (int i = 0; i < names.length(); i++) {
            String k = names.optString(i, "");
            try { out.put(Integer.parseInt(k), o.optLong(k, 0L)); } catch (Exception ignored) {}
        }
    }

    private static void readDoubleMap(JSONObject o, Map<Integer, Double> out) {
        if (o == null) return;
        JSONArray names = o.names();
        if (names == null) return;
        for (int i = 0; i < names.length(); i++) {
            String k = names.optString(i, "");
            try { out.put(Integer.parseInt(k), o.optDouble(k, 0d)); } catch (Exception ignored) {}
        }
    }

    private void createDefaults() {
        units.clear();
        months.clear();
        expenses.clear();
        initialFund = 0L;
        for (int n = 1; n <= 12; n++) {
            UnitInfo u = new UnitInfo();
            u.number = n;
            u.floor = ((n - 1) / 3) + 1;
            units.add(u);
        }
    }

    private void normalizeUnits() {
        Map<Integer, UnitInfo> byNumber = new HashMap<>();
        for (UnitInfo u : units) {
            if (u.number >= 1 && u.number <= 12) byNumber.put(u.number, u);
        }
        units.clear();
        for (int n = 1; n <= 12; n++) {
            UnitInfo u = byNumber.get(n);
            if (u == null) {
                u = new UnitInfo();
                u.number = n;
                u.floor = ((n - 1) / 3) + 1;
            }
            units.add(u);
        }
    }

    public synchronized void save() {
        try {
            JSONObject root = new JSONObject();
            root.put("initialFund", initialFund);
            root.put("smsTemplate", smsTemplate);

            JSONArray ua = new JSONArray();
            for (UnitInfo u : units) {
                JSONObject o = new JSONObject();
                o.put("number", u.number);
                o.put("floor", u.floor);
                o.put("owner", u.owner);
                o.put("ownerPhone", u.ownerPhone);
                o.put("tenant", u.tenant);
                o.put("tenantPhone", u.tenantPhone);
                o.put("occupants", u.occupants);
                o.put("submeter", u.submeter);
                o.put("lastWaterReading", u.lastWaterReading);
                o.put("notes", u.notes);
                ua.put(o);
            }
            root.put("units", ua);

            JSONObject mo = new JSONObject();
            for (Map.Entry<String, MonthData> entry : months.entrySet()) {
                MonthData md = entry.getValue();
                JSONObject o = new JSONObject();
                o.put("chargeAmount", md.chargeAmount);
                o.put("waterBillAmount", md.waterBillAmount);
                o.put("mainWaterConsumption", md.mainWaterConsumption);
                o.put("chargePaid", boolMapJson(md.chargePaid));
                o.put("waterPaid", boolMapJson(md.waterPaid));
                o.put("waterShare", longMapJson(md.waterShare));
                o.put("waterConsumption", doubleMapJson(md.waterConsumption));
                o.put("waterPrevious", doubleMapJson(md.waterPrevious));
                o.put("waterCurrent", doubleMapJson(md.waterCurrent));
                mo.put(entry.getKey(), o);
            }
            root.put("months", mo);

            JSONArray ea = new JSONArray();
            for (Expense ex : expenses) {
                JSONObject o = new JSONObject();
                o.put("id", ex.id);
                o.put("monthKey", ex.monthKey);
                o.put("title", ex.title);
                o.put("date", ex.date);
                o.put("amount", ex.amount);
                ea.put(o);
            }
            root.put("expenses", ea);

            prefs.edit().putString(KEY_DATA, root.toString()).apply();
        } catch (JSONException ignored) {}
    }

    private static JSONObject boolMapJson(Map<Integer, Boolean> map) throws JSONException {
        JSONObject o = new JSONObject();
        for (Map.Entry<Integer, Boolean> e : map.entrySet()) o.put(String.valueOf(e.getKey()), e.getValue());
        return o;
    }

    private static JSONObject longMapJson(Map<Integer, Long> map) throws JSONException {
        JSONObject o = new JSONObject();
        for (Map.Entry<Integer, Long> e : map.entrySet()) o.put(String.valueOf(e.getKey()), e.getValue());
        return o;
    }

    private static JSONObject doubleMapJson(Map<Integer, Double> map) throws JSONException {
        JSONObject o = new JSONObject();
        for (Map.Entry<Integer, Double> e : map.entrySet()) o.put(String.valueOf(e.getKey()), e.getValue());
        return o;
    }

    public List<UnitInfo> getUnits() {
        List<UnitInfo> copy = new ArrayList<>();
        for (UnitInfo u : units) copy.add(u.copy());
        return copy;
    }

    public UnitInfo getUnit(int number) {
        for (UnitInfo u : units) if (u.number == number) return u.copy();
        return null;
    }

    public void updateUnit(UnitInfo unit) {
        if (unit == null || unit.number < 1 || unit.number > 12) return;
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).number == unit.number) {
                units.set(i, unit.copy());
                save();
                return;
            }
        }
        units.add(unit.copy());
        normalizeUnits();
        save();
    }

    public MonthData getMonth(String key) {
        MonthData md = months.get(key);
        if (md == null) {
            md = new MonthData();
            md.key = key;
            months.put(key, md);
        }
        return md;
    }

    public void setChargeAmount(String monthKey, long amount) {
        MonthData md = getMonth(monthKey);
        md.chargeAmount = Math.max(0L, amount);
        save();
    }

    public boolean isChargePaid(String monthKey, int unit) {
        return Boolean.TRUE.equals(getMonth(monthKey).chargePaid.get(unit));
    }

    public void setChargePaid(String monthKey, int unit, boolean paid) {
        getMonth(monthKey).chargePaid.put(unit, paid);
        save();
    }

    public boolean isWaterPaid(String monthKey, int unit) {
        return Boolean.TRUE.equals(getMonth(monthKey).waterPaid.get(unit));
    }

    public void setWaterPaid(String monthKey, int unit, boolean paid) {
        getMonth(monthKey).waterPaid.put(unit, paid);
        save();
    }

    public long waterShare(String monthKey, int unit) {
        Long v = getMonth(monthKey).waterShare.get(unit);
        return v == null ? 0L : v;
    }

    public double savedPreviousReading(String monthKey, int unit, double fallback) {
        Double v = getMonth(monthKey).waterPrevious.get(unit);
        return v == null ? fallback : v;
    }

    public double savedCurrentReading(String monthKey, int unit, double fallback) {
        Double v = getMonth(monthKey).waterCurrent.get(unit);
        return v == null ? fallback : v;
    }

    public WaterCalculation calculateWater(long totalAmount, double mainConsumption, Map<Integer, Reading> readings) {
        if (totalAmount < 0L) throw new IllegalArgumentException("مبلغ قبض نمی‌تواند منفی باشد.");
        if (mainConsumption <= 0d) throw new IllegalArgumentException("کارکرد کل کنتور اصلی را وارد کنید.");

        List<UnitInfo> metered = new ArrayList<>();
        List<UnitInfo> unmetered = new ArrayList<>();
        for (UnitInfo u : units) {
            if (u.submeter) metered.add(u); else unmetered.add(u);
        }

        WaterCalculation result = new WaterCalculation();
        result.totalAmount = totalAmount;
        result.totalConsumption = mainConsumption;
        double rate = totalAmount / mainConsumption;
        long allocated = 0L;
        double measured = 0d;

        for (UnitInfo u : metered) {
            Reading r = readings == null ? null : readings.get(u.number);
            if (r == null) throw new IllegalArgumentException("عدد کنتور واحد " + u.number + " وارد نشده است.");
            if (r.previous < 0d || r.current < 0d) throw new IllegalArgumentException("عدد کنتور نمی‌تواند منفی باشد.");
            if (r.current < r.previous) throw new IllegalArgumentException("عدد جدید کنتور واحد " + u.number + " از عدد قبلی کمتر است.");
            double consumption = r.current - r.previous;
            measured += consumption;
            long share = Math.round(consumption * rate);
            allocated += share;
            result.shares.put(u.number, share);
            result.consumptions.put(u.number, consumption);
        }

        if (measured - mainConsumption > 0.0001d) {
            throw new IllegalArgumentException("مجموع مصرف کنتورهای فرعی از کارکرد کل کنتور اصلی بیشتر شده است.");
        }

        long remaining = totalAmount - allocated;
        if (remaining < 0L) remaining = 0L;
        if (unmetered.isEmpty()) {
            if (remaining > Math.max(10L, Math.round(totalAmount * 0.01d))) {
                throw new IllegalArgumentException("همه واحدها کنتور فرعی دارند اما بخشی از قبض بدون تخصیص مانده است.");
            }
            if (!metered.isEmpty() && remaining > 0L) {
                UnitInfo last = metered.get(metered.size() - 1);
                result.shares.put(last.number, result.shares.get(last.number) + remaining);
                allocated += remaining;
                remaining = 0L;
            }
        } else {
            long each = remaining / unmetered.size();
            long remainder = remaining % unmetered.size();
            for (int i = 0; i < unmetered.size(); i++) {
                long share = each + (i < remainder ? 1L : 0L);
                result.shares.put(unmetered.get(i).number, share);
                result.consumptions.put(unmetered.get(i).number, 0d);
            }
        }

        result.meteredConsumption = measured;
        result.meteredAmount = allocated;
        result.unmeteredAmount = remaining;
        return result;
    }

    public WaterCalculation saveWaterBill(String monthKey, long totalAmount, double mainConsumption, Map<Integer, Reading> readings) {
        WaterCalculation result = calculateWater(totalAmount, mainConsumption, readings);
        MonthData md = getMonth(monthKey);
        md.waterBillAmount = totalAmount;
        md.mainWaterConsumption = mainConsumption;
        md.waterShare.clear();
        md.waterShare.putAll(result.shares);
        md.waterConsumption.clear();
        md.waterConsumption.putAll(result.consumptions);
        md.waterPrevious.clear();
        md.waterCurrent.clear();
        if (readings != null) {
            for (Map.Entry<Integer, Reading> e : readings.entrySet()) {
                md.waterPrevious.put(e.getKey(), e.getValue().previous);
                md.waterCurrent.put(e.getKey(), e.getValue().current);
                for (UnitInfo u : units) {
                    if (u.number == e.getKey() && u.submeter) {
                        u.lastWaterReading = e.getValue().current;
                        break;
                    }
                }
            }
        }
        save();
        return result;
    }

    public void addExpense(String monthKey, String title, long amount, String date) {
        Expense ex = new Expense();
        ex.id = System.currentTimeMillis();
        ex.monthKey = monthKey;
        ex.title = title == null ? "" : title.trim();
        ex.amount = Math.max(0L, amount);
        ex.date = date == null ? "" : date.trim();
        expenses.add(ex);
        save();
    }

    public void deleteExpense(long id) {
        for (int i = expenses.size() - 1; i >= 0; i--) {
            if (expenses.get(i).id == id) expenses.remove(i);
        }
        save();
    }

    public List<Expense> expensesForMonth(String monthKey) {
        List<Expense> out = new ArrayList<>();
        for (Expense e : expenses) if (monthKey.equals(e.monthKey)) out.add(e);
        Collections.sort(out, new Comparator<Expense>() {
            @Override public int compare(Expense a, Expense b) { return Long.compare(b.id, a.id); }
        });
        return out;
    }

    public long monthExpenses(String monthKey) {
        long sum = 0L;
        for (Expense e : expenses) if (monthKey.equals(e.monthKey)) sum += e.amount;
        return sum;
    }

    public long totalExpenses() {
        long sum = 0L;
        for (Expense e : expenses) sum += e.amount;
        return sum;
    }

    public long monthChargeDue(String monthKey) {
        return getMonth(monthKey).chargeAmount * 12L;
    }

    public long monthChargePaid(String monthKey) {
        MonthData md = getMonth(monthKey);
        long count = 0L;
        for (int i = 1; i <= 12; i++) if (Boolean.TRUE.equals(md.chargePaid.get(i))) count++;
        return count * md.chargeAmount;
    }

    public long monthWaterDue(String monthKey) {
        long sum = 0L;
        for (Long v : getMonth(monthKey).waterShare.values()) if (v != null) sum += v;
        return sum;
    }

    public long monthWaterPaid(String monthKey) {
        MonthData md = getMonth(monthKey);
        long sum = 0L;
        for (int i = 1; i <= 12; i++) {
            if (Boolean.TRUE.equals(md.waterPaid.get(i))) {
                Long v = md.waterShare.get(i);
                if (v != null) sum += v;
            }
        }
        return sum;
    }

    public long monthDebt(String monthKey) {
        return Math.max(0L, (monthChargeDue(monthKey) - monthChargePaid(monthKey)) + (monthWaterDue(monthKey) - monthWaterPaid(monthKey)));
    }

    public long unitDebt(String monthKey, int unit) {
        MonthData md = getMonth(monthKey);
        long debt = 0L;
        if (md.chargeAmount > 0L && !Boolean.TRUE.equals(md.chargePaid.get(unit))) debt += md.chargeAmount;
        Long water = md.waterShare.get(unit);
        if (water != null && water > 0L && !Boolean.TRUE.equals(md.waterPaid.get(unit))) debt += water;
        return debt;
    }

    public long totalIncome() {
        long sum = 0L;
        for (String key : months.keySet()) {
            sum += monthChargePaid(key);
            sum += monthWaterPaid(key);
        }
        return sum;
    }

    public long getInitialFund() { return initialFund; }

    public void setInitialFund(long value) {
        initialFund = Math.max(0L, value);
        save();
    }

    public long fundBalance() {
        return initialFund + totalIncome() - totalExpenses();
    }

    public String getSmsTemplate() { return smsTemplate; }

    public void setSmsTemplate(String value) {
        if (value != null && !value.trim().isEmpty()) smsTemplate = value.trim();
        save();
    }
}
