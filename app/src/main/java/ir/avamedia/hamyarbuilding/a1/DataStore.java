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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataStore {
    private static final String PREFS = "hamyar_a1_data_v2";
    private static final String KEY_DATA = "data";

    public static final String OCC_OWNER = "OWNER";
    public static final String OCC_TENANT = "TENANT";
    public static final String OCC_VACANT = "VACANT";

    public static final class UnitInfo {
        public int number;
        public int floor;
        public String owner = "";
        public String ownerPhone = "";
        public String tenant = "";
        public String tenantPhone = "";
        public String occupancy = OCC_OWNER;
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
            x.occupancy = occupancy;
            x.occupants = occupants;
            x.submeter = submeter;
            x.lastWaterReading = lastWaterReading;
            x.notes = notes;
            return x;
        }

        public String residentName() {
            if (OCC_TENANT.equals(occupancy) && tenant != null && !tenant.trim().isEmpty()) return tenant;
            if (OCC_VACANT.equals(occupancy)) return "واحد خالی";
            return owner == null ? "" : owner;
        }

        public String residentPhone() {
            if (OCC_TENANT.equals(occupancy) && tenantPhone != null && !tenantPhone.trim().isEmpty()) return tenantPhone;
            return ownerPhone == null ? "" : ownerPhone;
        }
    }

    public static final class MonthData {
        public String key = "";
        public long chargeAmount = 0L;

        public long waterConsumptionAmount = 0L;
        public long waterGeneralAmount = 0L;
        public double mainWaterConsumption = 0d;
        public boolean waterSplitByOccupants = false;

        public final Map<Integer, Boolean> legacyChargePaid = new HashMap<>();
        public final Map<Integer, Boolean> waterPaid = new HashMap<>();
        public final Map<Integer, Long> waterShare = new HashMap<>();
        public final Map<Integer, Long> waterConsumptionShare = new HashMap<>();
        public final Map<Integer, Long> waterGeneralShare = new HashMap<>();
        public final Map<Integer, Double> waterConsumption = new HashMap<>();
        public final Map<Integer, Double> waterCurrent = new HashMap<>();
    }

    public static final class Expense {
        public long id;
        public String monthKey = "";
        public String title = "";
        public String date = "";
        public long amount;
    }

    public static final class ChargePayment {
        public long id;
        public int unit;
        public String date = "";
        public String startMonth = "";
        public long amount;
        public long credit;
        public final Map<String, Long> allocations = new LinkedHashMap<>();
    }

    public static final class ChargePreview {
        public long amount;
        public long credit;
        public final Map<String, Long> allocations = new LinkedHashMap<>();
    }

    public static final class WaterCalculation {
        public long consumptionAmount;
        public long generalAmount;
        public double mainConsumption;
        public double meteredConsumption;
        public long remainingConsumptionAmount;
        public final Map<Integer, Long> finalShares = new HashMap<>();
        public final Map<Integer, Long> consumptionShares = new HashMap<>();
        public final Map<Integer, Long> generalShares = new HashMap<>();
        public final Map<Integer, Double> consumptions = new HashMap<>();
        public final Map<Integer, Double> previousReadings = new HashMap<>();
    }

    private final SharedPreferences prefs;
    private final List<UnitInfo> units = new ArrayList<>();
    private final Map<String, MonthData> months = new HashMap<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final List<ChargePayment> chargePayments = new ArrayList<>();

    private long initialFund = 0L;
    private String smsTemplate = "مالک/ساکن محترم {نام}، بدهی واحد {واحد} بابت شارژ و آب تا {ماه} مبلغ {مبلغ} تومان است. با تشکر - مدیریت بلوک A1 مجتمع فرهیختگان";

    public DataStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        String raw = prefs.getString(KEY_DATA, null);
        if (raw == null || raw.trim().isEmpty()) {
            createDefaults();
            save();
            return;
        }
        try {
            loadFromRoot(new JSONObject(raw));
        } catch (Exception e) {
            createDefaults();
            save();
        }
    }

    private void loadFromRoot(JSONObject root) {
        units.clear();
        months.clear();
        expenses.clear();
        chargePayments.clear();

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
                x.occupancy = o.optString("occupancy", "");
                if (x.occupancy.isEmpty()) {
                    x.occupancy = (!x.tenant.trim().isEmpty() || !x.tenantPhone.trim().isEmpty()) ? OCC_TENANT : OCC_OWNER;
                }
                x.occupants = Math.max(0, o.optInt("occupants", 1));
                x.submeter = o.optBoolean("submeter", false);
                x.lastWaterReading = Math.max(0d, o.optDouble("lastWaterReading", 0d));
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

                    long legacyWater = mo.optLong("waterBillAmount", 0L);
                    md.waterConsumptionAmount = mo.has("waterConsumptionAmount") ? mo.optLong("waterConsumptionAmount", 0L) : legacyWater;
                    md.waterGeneralAmount = mo.optLong("waterGeneralAmount", 0L);
                    md.mainWaterConsumption = mo.optDouble("mainWaterConsumption", 0d);
                    md.waterSplitByOccupants = mo.optBoolean("waterSplitByOccupants", false);

                    readBoolMap(mo.optJSONObject("chargePaid"), md.legacyChargePaid);
                    readBoolMap(mo.optJSONObject("legacyChargePaid"), md.legacyChargePaid);
                    readBoolMap(mo.optJSONObject("waterPaid"), md.waterPaid);
                    readLongMap(mo.optJSONObject("waterShare"), md.waterShare);
                    readLongMap(mo.optJSONObject("waterConsumptionShare"), md.waterConsumptionShare);
                    readLongMap(mo.optJSONObject("waterGeneralShare"), md.waterGeneralShare);
                    readDoubleMap(mo.optJSONObject("waterConsumption"), md.waterConsumption);
                    readDoubleMap(mo.optJSONObject("waterCurrent"), md.waterCurrent);
                    if (md.waterCurrent.isEmpty()) {
                        readDoubleMap(mo.optJSONObject("waterCurrent"), md.waterCurrent);
                    }
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
                ex.amount = Math.max(0L, o.optLong("amount", 0L));
                expenses.add(ex);
            }
        }

        JSONArray cp = root.optJSONArray("chargePayments");
        if (cp != null) {
            for (int i = 0; i < cp.length(); i++) {
                JSONObject o = cp.optJSONObject(i);
                if (o == null) continue;
                ChargePayment p = new ChargePayment();
                p.id = o.optLong("id", System.currentTimeMillis() + i);
                p.unit = o.optInt("unit", 0);
                p.date = o.optString("date", "");
                p.startMonth = o.optString("startMonth", "");
                p.amount = Math.max(0L, o.optLong("amount", 0L));
                p.credit = Math.max(0L, o.optLong("credit", 0L));
                JSONObject alloc = o.optJSONObject("allocations");
                if (alloc != null) {
                    JSONArray an = alloc.names();
                    if (an != null) for (int j = 0; j < an.length(); j++) {
                        String mk = an.optString(j, "");
                        p.allocations.put(mk, Math.max(0L, alloc.optLong(mk, 0L)));
                    }
                }
                if (p.unit >= 1 && p.unit <= 12 && p.amount > 0L) chargePayments.add(p);
            }
        }

        normalizeUnits();
    }

    private void createDefaults() {
        units.clear();
        months.clear();
        expenses.clear();
        chargePayments.clear();
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
        for (UnitInfo u : units) if (u.number >= 1 && u.number <= 12) byNumber.put(u.number, u);
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
            prefs.edit().putString(KEY_DATA, buildRoot().toString()).apply();
        } catch (Exception ignored) {}
    }

    public synchronized String exportJson() {
        try { return buildRoot().toString(2); }
        catch (Exception e) { return "{}"; }
    }

    public synchronized void importJson(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        loadFromRoot(root);
        save();
    }

    private JSONObject buildRoot() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("schema", 3);
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
            o.put("occupancy", u.occupancy);
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
            o.put("waterConsumptionAmount", md.waterConsumptionAmount);
            o.put("waterGeneralAmount", md.waterGeneralAmount);
            o.put("mainWaterConsumption", md.mainWaterConsumption);
            o.put("waterSplitByOccupants", md.waterSplitByOccupants);
            o.put("legacyChargePaid", boolMapJson(md.legacyChargePaid));
            o.put("waterPaid", boolMapJson(md.waterPaid));
            o.put("waterShare", longMapJson(md.waterShare));
            o.put("waterConsumptionShare", longMapJson(md.waterConsumptionShare));
            o.put("waterGeneralShare", longMapJson(md.waterGeneralShare));
            o.put("waterConsumption", doubleMapJson(md.waterConsumption));
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

        JSONArray pa = new JSONArray();
        for (ChargePayment p : chargePayments) {
            JSONObject o = new JSONObject();
            o.put("id", p.id);
            o.put("unit", p.unit);
            o.put("date", p.date);
            o.put("startMonth", p.startMonth);
            o.put("amount", p.amount);
            o.put("credit", p.credit);
            JSONObject a = new JSONObject();
            for (Map.Entry<String, Long> x : p.allocations.entrySet()) a.put(x.getKey(), x.getValue());
            o.put("allocations", a);
            pa.put(o);
        }
        root.put("chargePayments", pa);
        return root;
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
        List<UnitInfo> out = new ArrayList<>();
        for (UnitInfo u : units) out.add(u.copy());
        return out;
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
        getMonth(monthKey).chargeAmount = Math.max(0L, amount);
        save();
    }

    public long chargeDue(String monthKey, int unit) {
        return Math.max(0L, getMonth(monthKey).chargeAmount);
    }

    public long chargePaidAmount(String monthKey, int unit) {
        MonthData md = getMonth(monthKey);
        long sum = 0L;
        if (Boolean.TRUE.equals(md.legacyChargePaid.get(unit))) sum += md.chargeAmount;
        for (ChargePayment p : chargePayments) {
            if (p.unit != unit) continue;
            Long a = p.allocations.get(monthKey);
            if (a != null) sum += a;
        }
        return Math.max(0L, sum);
    }

    public int chargeStatus(String monthKey, int unit) {
        long due = chargeDue(monthKey, unit);
        if (due <= 0L) return 0;
        long paid = chargePaidAmount(monthKey, unit);
        if (paid <= 0L) return 1;
        if (paid < due) return 2;
        return 3;
    }

    public long chargeRemaining(String monthKey, int unit) {
        return Math.max(0L, chargeDue(monthKey, unit) - chargePaidAmount(monthKey, unit));
    }

    public ChargePreview previewChargePayment(int unit, String startMonth, long amount) {
        return allocateCharge(unit, startMonth, amount, false);
    }

    public ChargePayment recordChargePayment(int unit, String date, String startMonth, long amount) {
        if (unit < 1 || unit > 12 || amount <= 0L) throw new IllegalArgumentException("مبلغ پرداخت صحیح نیست.");
        ChargePreview preview = allocateCharge(unit, startMonth, amount, true);
        ChargePayment p = new ChargePayment();
        p.id = System.currentTimeMillis();
        p.unit = unit;
        p.date = date == null ? "" : date.trim();
        p.startMonth = startMonth;
        p.amount = amount;
        p.credit = preview.credit;
        p.allocations.putAll(preview.allocations);
        chargePayments.add(p);
        save();
        return p;
    }

    private ChargePreview allocateCharge(int unit, String startMonth, long amount, boolean createFutureCharges) {
        ChargePreview result = new ChargePreview();
        result.amount = Math.max(0L, amount);
        long remaining = result.amount;
        long base = getMonth(startMonth).chargeAmount;
        if (base <= 0L) throw new IllegalArgumentException("ابتدا مبلغ شارژ ماه شروع را تعیین کنید.");

        for (int i = 0; i < 36 && remaining > 0L; i++) {
            String mk = PersianDate.shiftMonth(startMonth, i);
            MonthData md = getMonth(mk);
            long due = md.chargeAmount > 0L ? md.chargeAmount : base;
            if (createFutureCharges && md.chargeAmount <= 0L) md.chargeAmount = base;

            long already = chargePaidAmount(mk, unit);
            long outstanding = Math.max(0L, due - already);
            if (outstanding <= 0L) continue;

            long a = Math.min(remaining, outstanding);
            result.allocations.put(mk, a);
            remaining -= a;
        }
        result.credit = remaining;
        return result;
    }

    public List<ChargePayment> paymentsForUnit(int unit) {
        List<ChargePayment> out = new ArrayList<>();
        for (ChargePayment p : chargePayments) if (p.unit == unit) out.add(p);
        Collections.sort(out, new Comparator<ChargePayment>() {
            @Override public int compare(ChargePayment a, ChargePayment b) { return Long.compare(b.id, a.id); }
        });
        return out;
    }

    public void deleteChargePayment(long id) {
        for (int i = chargePayments.size() - 1; i >= 0; i--) {
            if (chargePayments.get(i).id == id) chargePayments.remove(i);
        }
        save();
    }

    public double previousReadingForUnit(String monthKey, int unitNumber) {
        int best = Integer.MIN_VALUE;
        Double value = null;
        int current = PersianDate.monthSerial(monthKey);
        for (Map.Entry<String, MonthData> e : months.entrySet()) {
            int s = PersianDate.monthSerial(e.getKey());
            if (s >= current || s <= best) continue;
            Double x = e.getValue().waterCurrent.get(unitNumber);
            if (x != null) {
                best = s;
                value = x;
            }
        }
        if (value != null) return value;
        UnitInfo u = getUnit(unitNumber);
        return u == null ? 0d : Math.max(0d, u.lastWaterReading);
    }

    public double savedCurrentReading(String monthKey, int unitNumber) {
        Double x = getMonth(monthKey).waterCurrent.get(unitNumber);
        return x == null ? previousReadingForUnit(monthKey, unitNumber) : x;
    }

    public WaterCalculation calculateWater(String monthKey, long consumptionAmount, long generalAmount,
                                           double mainConsumption, boolean splitByOccupants,
                                           Map<Integer, Double> currentReadings) {
        if (consumptionAmount < 0L || generalAmount < 0L) throw new IllegalArgumentException("مبلغ قبض نمی‌تواند منفی باشد.");
        if (mainConsumption <= 0d && consumptionAmount > 0L) throw new IllegalArgumentException("کارکرد کل درج‌شده روی قبض را وارد کنید.");

        List<UnitInfo> metered = new ArrayList<>();
        List<UnitInfo> unmetered = new ArrayList<>();
        for (UnitInfo u : units) {
            if (u.submeter) metered.add(u);
            else unmetered.add(u);
        }

        WaterCalculation r = new WaterCalculation();
        r.consumptionAmount = consumptionAmount;
        r.generalAmount = generalAmount;
        r.mainConsumption = mainConsumption;

        long generalEach = generalAmount / 12L;
        long generalRem = generalAmount % 12L;
        for (int i = 0; i < units.size(); i++) {
            UnitInfo u = units.get(i);
            long g = generalEach + (i < generalRem ? 1L : 0L);
            r.generalShares.put(u.number, g);
        }

        double rate = mainConsumption > 0d ? ((double) consumptionAmount / mainConsumption) : 0d;
        long meteredAllocated = 0L;
        double meteredUse = 0d;

        for (UnitInfo u : metered) {
            if (currentReadings == null || !currentReadings.containsKey(u.number))
                throw new IllegalArgumentException("عدد جدید کنتور واحد " + u.number + " وارد نشده است.");

            double prev = previousReadingForUnit(monthKey, u.number);
            double cur = currentReadings.get(u.number);
            if (cur < 0d) throw new IllegalArgumentException("عدد کنتور نمی‌تواند منفی باشد.");
            if (cur < prev) throw new IllegalArgumentException("عدد جدید کنتور واحد " + u.number + " از عدد قبلی کمتر است.");

            double use = cur - prev;
            meteredUse += use;
            long share = Math.round(use * rate);
            meteredAllocated += share;
            r.previousReadings.put(u.number, prev);
            r.consumptions.put(u.number, use);
            r.consumptionShares.put(u.number, share);
        }

        if (meteredUse - mainConsumption > 0.0001d)
            throw new IllegalArgumentException("مجموع مصرف کنتورهای فرعی از کارکرد کل قبض بیشتر است.");

        long remaining = Math.max(0L, consumptionAmount - meteredAllocated);
        r.meteredConsumption = meteredUse;
        r.remainingConsumptionAmount = remaining;

        if (!unmetered.isEmpty()) {
            if (splitByOccupants) {
                int totalWeight = 0;
                for (UnitInfo u : unmetered) totalWeight += Math.max(0, u.occupants);
                if (totalWeight <= 0) {
                    splitEqual(unmetered, remaining, r.consumptionShares);
                } else {
                    long assigned = 0L;
                    for (int i = 0; i < unmetered.size(); i++) {
                        UnitInfo u = unmetered.get(i);
                        long share;
                        if (i == unmetered.size() - 1) {
                            share = remaining - assigned;
                        } else {
                            share = Math.round(((double) remaining * Math.max(0, u.occupants)) / totalWeight);
                            if (share < 0L) share = 0L;
                            if (assigned + share > remaining) share = remaining - assigned;
                        }
                        assigned += share;
                        r.consumptionShares.put(u.number, share);
                        r.consumptions.put(u.number, 0d);
                    }
                }
            } else {
                splitEqual(unmetered, remaining, r.consumptionShares);
                for (UnitInfo u : unmetered) r.consumptions.put(u.number, 0d);
            }
        } else if (remaining > Math.max(10L, Math.round(consumptionAmount * 0.01d))) {
            throw new IllegalArgumentException("همه واحدها کنتور فرعی دارند اما بخشی از هزینه مصرف تخصیص پیدا نکرده است.");
        }

        for (UnitInfo u : units) {
            long c = value(r.consumptionShares, u.number);
            long g = value(r.generalShares, u.number);
            r.finalShares.put(u.number, c + g);
        }
        return r;
    }

    private static void splitEqual(List<UnitInfo> list, long amount, Map<Integer, Long> out) {
        if (list == null || list.isEmpty()) return;
        long each = amount / list.size();
        long rem = amount % list.size();
        for (int i = 0; i < list.size(); i++) out.put(list.get(i).number, each + (i < rem ? 1L : 0L));
    }

    private static long value(Map<Integer, Long> m, int key) {
        Long x = m.get(key);
        return x == null ? 0L : x;
    }

    public WaterCalculation saveWaterBill(String monthKey, long consumptionAmount, long generalAmount,
                                          double mainConsumption, boolean splitByOccupants,
                                          Map<Integer, Double> currentReadings) {
        WaterCalculation r = calculateWater(monthKey, consumptionAmount, generalAmount, mainConsumption, splitByOccupants, currentReadings);
        MonthData md = getMonth(monthKey);
        md.waterConsumptionAmount = consumptionAmount;
        md.waterGeneralAmount = generalAmount;
        md.mainWaterConsumption = mainConsumption;
        md.waterSplitByOccupants = splitByOccupants;
        md.waterShare.clear(); md.waterShare.putAll(r.finalShares);
        md.waterConsumptionShare.clear(); md.waterConsumptionShare.putAll(r.consumptionShares);
        md.waterGeneralShare.clear(); md.waterGeneralShare.putAll(r.generalShares);
        md.waterConsumption.clear(); md.waterConsumption.putAll(r.consumptions);
        md.waterCurrent.clear();
        if (currentReadings != null) md.waterCurrent.putAll(currentReadings);
        save();
        return r;
    }

    public long waterShare(String monthKey, int unit) {
        return value(getMonth(monthKey).waterShare, unit);
    }

    public long waterConsumptionShare(String monthKey, int unit) {
        return value(getMonth(monthKey).waterConsumptionShare, unit);
    }

    public long waterGeneralShare(String monthKey, int unit) {
        return value(getMonth(monthKey).waterGeneralShare, unit);
    }

    public boolean isWaterPaid(String monthKey, int unit) {
        return Boolean.TRUE.equals(getMonth(monthKey).waterPaid.get(unit));
    }

    public void setWaterPaid(String monthKey, int unit, boolean paid) {
        getMonth(monthKey).waterPaid.put(unit, paid);
        save();
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
        for (int i = expenses.size() - 1; i >= 0; i--) if (expenses.get(i).id == id) expenses.remove(i);
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
        long sum = 0L;
        for (int i = 1; i <= 12; i++) sum += Math.min(chargeDue(monthKey, i), chargePaidAmount(monthKey, i));
        return sum;
    }

    public long monthWaterDue(String monthKey) {
        long sum = 0L;
        for (int i = 1; i <= 12; i++) sum += waterShare(monthKey, i);
        return sum;
    }

    public long monthWaterPaid(String monthKey) {
        long sum = 0L;
        for (int i = 1; i <= 12; i++) if (isWaterPaid(monthKey, i)) sum += waterShare(monthKey, i);
        return sum;
    }

    public long monthDebt(String monthKey) {
        long d = 0L;
        for (int i = 1; i <= 12; i++) d += unitMonthDebt(monthKey, i);
        return d;
    }

    public long unitMonthDebt(String monthKey, int unit) {
        long d = chargeRemaining(monthKey, unit);
        long w = waterShare(monthKey, unit);
        if (w > 0L && !isWaterPaid(monthKey, unit)) d += w;
        return d;
    }

    public long unitTotalDebtThrough(String throughMonth, int unit) {
        int end = PersianDate.monthSerial(throughMonth);
        long sum = 0L;
        for (String key : months.keySet()) {
            if (PersianDate.monthSerial(key) <= end) sum += unitMonthDebt(key, unit);
        }
        return sum;
    }

    public long totalDebtThrough(String throughMonth) {
        long sum = 0L;
        for (int unit = 1; unit <= 12; unit++) sum += unitTotalDebtThrough(throughMonth, unit);
        return sum;
    }

    public long totalChargePaymentIncome() {
        long sum = 0L;
        for (ChargePayment p : chargePayments) sum += p.amount;
        for (MonthData md : months.values()) {
            for (int unit = 1; unit <= 12; unit++) if (Boolean.TRUE.equals(md.legacyChargePaid.get(unit))) sum += md.chargeAmount;
        }
        return sum;
    }

    public long totalWaterIncome() {
        long sum = 0L;
        for (Map.Entry<String, MonthData> e : months.entrySet()) sum += monthWaterPaid(e.getKey());
        return sum;
    }

    public long totalIncome() {
        return totalChargePaymentIncome() + totalWaterIncome();
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
