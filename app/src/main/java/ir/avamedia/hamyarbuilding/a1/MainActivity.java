package ir.avamedia.hamyarbuilding.a1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int CREAM = Color.rgb(248,242,230);
    private static final int CREAM2 = Color.rgb(239,226,205);
    private static final int BROWN = Color.rgb(112,72,43);
    private static final int DARK = Color.rgb(53,32,22);
    private static final int GOLD = Color.rgb(197,153,72);
    private static final int TEXT = Color.rgb(53,42,34);
    private static final int MUTED = Color.rgb(116,104,94);
    private static final int GREEN = Color.rgb(45,122,72);
    private static final int RED = Color.rgb(176,66,58);
    private static final int ORANGE = Color.rgb(194,126,44);
    private static final int SOFT_GREEN = Color.rgb(228,241,231);
    private static final int SOFT_RED = Color.rgb(249,231,228);
    private static final int SOFT_ORANGE = Color.rgb(249,238,218);

    private static final int REQ_BACKUP_CREATE = 501;
    private static final int REQ_BACKUP_OPEN = 502;

    private DataStore store;
    private Typeface font = Typeface.DEFAULT;
    private Typeface bold = Typeface.DEFAULT_BOLD;
    private LinearLayout root;
    private FrameLayout content;
    private TextView monthLabel;
    private String monthKey;
    private String screen = "dashboard";
    private int detailUnit = 0;
    private final Map<String, LinearLayout> navItems = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        try {
            Window w = getWindow();
            w.setStatusBarColor(DARK);
            w.setNavigationBarColor(DARK);
            w.getDecorView().setSystemUiVisibility(0);
            try { font = Typeface.createFromAsset(getAssets(), "fonts/shabnam.ttf"); } catch (Throwable ignored) {}
            try { bold = Typeface.createFromAsset(getAssets(), "fonts/shabnam_bold.ttf"); } catch (Throwable ignored) {}
            store = new DataStore(this);
            monthKey = PersianDate.currentMonthKey();
            buildShell();
            showDashboard();
        } catch (Throwable e) {
            showFatal(e);
        }
    }

    private void showFatal(Throwable e) {
        LinearLayout p = vbox();
        p.setPadding(dp(24),dp(36),dp(24),dp(24));
        p.setBackgroundColor(CREAM);
        p.addView(tv("همیار ساختمان",24,DARK,true));
        p.addView(tv("خطای راه‌اندازی برنامه\n\n" + e.getClass().getSimpleName() + "\n" +
                (e.getMessage()==null ? "" : e.getMessage()),14,RED,false));
        setContentView(p);
    }

    private void buildShell() {
        root = vbox();
        root.setBackgroundColor(DARK);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(root);

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(0, Math.max(0, insets.getSystemWindowInsetTop()), 0, 0);
            return insets;
        });
        root.requestApplyInsets();

        LinearLayout header = vbox();
        header.setPadding(dp(16),dp(4),dp(16),dp(7));
        header.setBackgroundColor(DARK);
        root.addView(header, lp(-1,-2));

        LinearLayout names = vbox();
        names.setGravity(Gravity.CENTER);
        names.addView(center("همیار ساختمان",22,Color.WHITE,true));
        names.addView(center("بلوک A1 مجتمع فرهیختگان",12,GOLD,false));
        header.addView(names, lp(-1,dp(56)));

        LinearLayout monthRow = hbox();
        monthRow.setGravity(Gravity.CENTER);
        header.addView(monthRow, lp(-1,dp(44)));

        Button next = button("ماه بعد ›",Color.WHITE,Color.TRANSPARENT,false);
        next.setOnClickListener(v -> changeMonth(1));
        monthRow.addView(next,new LinearLayout.LayoutParams(0,-1,1f));

        monthLabel = center(PersianDate.monthLabel(monthKey),14,Color.WHITE,true);
        monthLabel.setBackground(round(Color.rgb(78,49,33),13,1,Color.rgb(128,84,55)));
        monthRow.addView(monthLabel,new LinearLayout.LayoutParams(0,dp(38),1.45f));

        Button prev = button("‹ ماه قبل",Color.WHITE,Color.TRANSPARENT,false);
        prev.setOnClickListener(v -> changeMonth(-1));
        monthRow.addView(prev,new LinearLayout.LayoutParams(0,-1,1f));

        content = new FrameLayout(this);
        content.setBackgroundColor(CREAM);
        root.addView(content, new LinearLayout.LayoutParams(-1,0,1f));

        LinearLayout nav = hbox();
        nav.setPadding(dp(8),dp(6),dp(8),dp(8));
        nav.setBackgroundColor(DARK);
        root.addView(nav, lp(-1,dp(68)));
        addNav(nav,R.drawable.ic_home,"داشبورد","dashboard");
        addNav(nav,R.drawable.ic_apartment,"واحدها","units");
        addNav(nav,R.drawable.ic_wallet,"مالی","finance");
        addNav(nav,R.drawable.ic_bar_chart,"گزارش","reports");
    }

    private void addNav(LinearLayout nav, int iconRes, String label, final String target) {
        LinearLayout box=vbox();
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(5),dp(3),dp(5),dp(3));

        ImageView i=iconView(iconRes,Color.rgb(235,221,201),22);
        TextView t=center(label,10,Color.rgb(235,221,201),false);
        box.addView(i,lp(-1,dp(29)));
        box.addView(t,lp(-1,dp(23)));
        box.setOnClickListener(v -> {
            if ("dashboard".equals(target)) showDashboard();
            else if ("units".equals(target)) showUnits();
            else if ("finance".equals(target)) showFinance();
            else showReports();
        });
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1f);
        p.setMargins(dp(3),0,dp(3),0);
        nav.addView(box,p);
        navItems.put(target,box);
    }

    private void updateNav() {
        String active;
        if ("dashboard".equals(screen) || "unitDetail".equals(screen)) active="dashboard";
        else if ("units".equals(screen)) active="units";
        else if ("reports".equals(screen)) active="reports";
        else active="finance";

        for(Map.Entry<String,LinearLayout> e:navItems.entrySet()) {
            boolean on=e.getKey().equals(active);
            LinearLayout box=e.getValue();
            box.setBackground(on?round(GOLD,14,0,0):round(Color.TRANSPARENT,14,0,0));
            for(int i=0;i<box.getChildCount();i++) {
                View child=box.getChildAt(i);
                if(child instanceof TextView) {
                    ((TextView) child).setTextColor(on?DARK:Color.rgb(235,221,201));
                } else if(child instanceof ImageView) {
                    ((ImageView) child).setColorFilter(on?DARK:Color.rgb(235,221,201));
                }
            }
        }
    }

    private void changeMonth(int delta) {
        monthKey = PersianDate.shiftMonth(monthKey, delta);
        if (monthLabel != null) monthLabel.setText(PersianDate.monthLabel(monthKey));
        render();
    }

    private void render() {
        if ("dashboard".equals(screen)) showDashboard();
        else if ("units".equals(screen)) showUnits();
        else if ("finance".equals(screen)) showFinance();
        else if ("charge".equals(screen)) showCharge();
        else if ("water".equals(screen)) showWater();
        else if ("expenses".equals(screen)) showExpenses();
        else if ("fund".equals(screen)) showFund();
        else if ("sms".equals(screen)) showSmsSettings();
        else if ("backup".equals(screen)) showBackup();
        else if ("unitDetail".equals(screen) && detailUnit > 0) showUnitDetail(detailUnit);
        else showReports();
    }

    private void setPage(String name, View page) {
        screen = name;
        content.removeAllViews();
        content.addView(page,new FrameLayout.LayoutParams(-1,-1));
        updateNav();
    }

    private void showDashboard() {
        LinearLayout body = vbox();
        body.setPadding(dp(8),dp(6),dp(8),dp(8));

        LinearLayout stats = hbox();
        body.addView(stats,lp(-1,-2));
        stat(stats,R.drawable.ic_wallet,"موجودی صندوق",store.fundBalance());
        stat(stats,R.drawable.ic_receipt,"مطالبات تا این ماه",store.totalDebtThrough(monthKey));
        stat(stats,R.drawable.ic_payments,"هزینه ماه",store.monthExpenses(monthKey));

        TextView h = tv("وضعیت واحدها",18,DARK,true);
        h.setPadding(dp(4),dp(7),dp(4),dp(2));
        body.addView(h);

        int[][] rows = {{10,11,12},{7,8,9},{4,5,6},{1,2,3}};
        for (int[] row : rows) {
            LinearLayout r = hbox();
            body.addView(r,lp(-1,-2));
            for (int n : row) {
                final int unit = n;
                DataStore.UnitInfo u = store.getUnit(n);
                int cs = store.chargeStatus(monthKey,n);
                int ws = store.waterStatus(monthKey,n);

                LinearLayout card = vbox();
                card.setGravity(Gravity.CENTER);
                card.setPadding(dp(3),dp(4),dp(3),dp(4));
                card.setBackground(round(Color.WHITE,14,1,Color.rgb(221,204,182)));
                card.addView(center("واحد " + fa(n),13,DARK,true));
                card.addView(center(u==null?"":u.residentName(),8,MUTED,false));
                card.addView(statusChip(chargeStatusText(cs,"شارژ"),statusColor(cs)));
                card.addView(statusChip(chargeStatusText(ws,"آب"),statusColor(ws)));
                card.setOnClickListener(v -> showUnitDetail(unit));

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(80),1f);
                p.setMargins(dp(3),dp(2),dp(3),dp(2));
                r.addView(card,p);
            }
        }
        setPage("dashboard",scroll(body));
    }

    private TextView statusChip(String value,int color) {
        TextView t=center(value,9,color,true);
        int bg;
        if(color==GREEN) bg=SOFT_GREEN;
        else if(color==RED) bg=SOFT_RED;
        else if(color==ORANGE) bg=SOFT_ORANGE;
        else bg=Color.rgb(244,241,237);
        t.setBackground(round(bg,12,0,0));
        LinearLayout.LayoutParams p=lp(dp(82),dp(18));
        p.setMargins(0,dp(1),0,0);
        t.setLayoutParams(p);
        return t;
    }

    private String chargeStatusText(int st,String title) {
        if(st==0) return title+" —";
        if(st==1) return title+" ✕";
        if(st==2) return title+" ناقص";
        return title+" ✓";
    }

    private int statusColor(int st) {
        if(st==1) return RED;
        if(st==2) return ORANGE;
        if(st==3) return GREEN;
        return MUTED;
    }

    private void stat(LinearLayout row,int iconRes,String title,long value) {
        LinearLayout c = vbox();
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(3),dp(4),dp(3),dp(4));
        c.setBackground(round(CREAM2,13,1,Color.rgb(218,196,169)));
        c.addView(iconView(iconRes,BROWN,16),lp(-1,dp(20)));
        c.addView(center(title,8,MUTED,false));
        c.addView(center(money(value),9,DARK,true));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(60),1f);
        p.setMargins(dp(3),0,dp(3),0);
        row.addView(c,p);
    }

    private void showUnitDetail(int number) {
        detailUnit = number;
        DataStore.UnitInfo u = store.getUnit(number);
        if (u == null) return;

        LinearLayout body=vbox();
        body.setPadding(dp(12),dp(12),dp(12),dp(20));
        body.addView(tv("واحد " + fa(number),20,DARK,true));
        body.addView(tv("اطلاعات و خلاصه مالی",10,MUTED,false));

        LinearLayout info = sectionCard("اطلاعات ساکن",R.drawable.ic_apartment);
        info.addView(detailLine("مالک", emptyDash(u.owner)));
        info.addView(detailLine("موبایل مالک", emptyDash(u.ownerPhone)));
        if (DataStore.OCC_TENANT.equals(u.occupancy)) {
            info.addView(detailLine("مستأجر", emptyDash(u.tenant)));
            info.addView(detailLine("موبایل مستأجر", emptyDash(u.tenantPhone)));
        } else if (DataStore.OCC_VACANT.equals(u.occupancy)) {
            info.addView(detailLine("وضعیت سکونت","واحد خالی"));
        } else {
            info.addView(detailLine("وضعیت سکونت","مالک ساکن است"));
        }
        info.addView(detailLine("تعداد نفرات",fa(u.occupants)));
        body.addView(info,rowParams());

        long chargeDue=store.chargeDue(monthKey,number);
        long chargePaid=store.chargePaidAmount(monthKey,number);
        long water=store.waterShare(monthKey,number);
        long waterPaid=store.waterPaidAmount(monthKey,number);
        long monthDebt=store.unitMonthDebt(monthKey,number);
        long totalDebt=store.unitTotalDebtThrough(monthKey,number);

        LinearLayout fin=sectionCard("وضعیت مالی",R.drawable.ic_wallet);
        fin.addView(detailLine("شارژ این ماه",chargeDue<=0?"ثبت نشده":money(chargeDue)));
        fin.addView(detailLine("پرداخت شارژ",money(Math.min(chargePaid,chargeDue))));
        fin.addView(detailLine("مانده شارژ",money(Math.max(0,chargeDue-chargePaid))));
        if(store.chargeCredit(number)>0) fin.addView(detailLine("بستانکاری شارژ",money(store.chargeCredit(number))));
        fin.addView(detailLine("سهم آب",water<=0?"ثبت نشده":money(water)));
        fin.addView(detailLine("پرداخت آب",money(Math.min(waterPaid,water))));
        fin.addView(detailLine("مانده آب",money(store.waterRemaining(monthKey,number))));
        fin.addView(detailLine("بدهی " + PersianDate.monthLabel(monthKey),money(monthDebt)));
        fin.addView(detailLine("کل بدهی تا این ماه",money(totalDebt)));
        body.addView(fin,rowParams());

        LinearLayout actions=hbox();
        if (totalDebt > 0L) {
            Button sms=button("✉ پیامک یادآوری",Color.WHITE,BROWN,true);
            sms.setOnClickListener(v->sendUnitReminder(number));
            actions.addView(sms,pairButtonParams(1f));
        }
        Button image=button("اشتراک گزارش",BROWN,CREAM2,true);
        image.setOnClickListener(v->showUnitReportImage(number));
        actions.addView(image,pairButtonParams(1f));
        body.addView(actions,rowParams());

        List<DataStore.ChargePayment> payments=store.paymentsForUnit(number);
        List<DataStore.WaterPayment> wp=store.waterPaymentsForUnit(number);
        if(!payments.isEmpty() || !wp.isEmpty()) {
            body.addView(tv("سوابق پرداخت",15,DARK,true));
        }

        int shown=0;
        for(final DataStore.ChargePayment p:payments) {
            if(p.externalFunding) continue;
            LinearLayout row=historyRow("شارژ | "+p.date,money(p.amount),allocationSummary(p.allocations));
            Button del=smallDelete(v->new AlertDialog.Builder(this)
                    .setTitle("حذف پرداخت شارژ")
                    .setMessage("این پرداخت حذف شود؟")
                    .setNegativeButton("انصراف",null)
                    .setPositiveButton("حذف",(d,w)->{store.deleteChargePayment(p.id);showUnitDetail(number);})
                    .show());
            row.addView(del,lp(dp(58),dp(34)));
            body.addView(row,rowParams());
            shown++; if(shown>=4) break;
        }

        shown=0;
        for(final DataStore.WaterPayment p:wp) {
            String sub="سهم آب "+money(p.appliedWater);
            if(p.transferredToCharge>0) sub+=" | انتقال به شارژ "+money(p.transferredToCharge);
            LinearLayout row=historyRow("آب | "+p.date,money(p.amount),sub);
            Button del=smallDelete(v->new AlertDialog.Builder(this)
                    .setTitle("حذف پرداخت آب")
                    .setMessage("پرداخت آب و انتقال مرتبط به شارژ حذف شود؟")
                    .setNegativeButton("انصراف",null)
                    .setPositiveButton("حذف",(d,w)->{store.deleteWaterPayment(p.id);showUnitDetail(number);})
                    .show());
            row.addView(del,lp(dp(58),dp(34)));
            body.addView(row,rowParams());
            shown++; if(shown>=4) break;
        }

        setPage("unitDetail",scroll(body));
    }

    private LinearLayout historyRow(String title,String value,String subtitle) {
        LinearLayout row=hbox();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10),dp(8),dp(10),dp(8));
        row.setBackground(round(Color.WHITE,11,1,Color.rgb(224,207,185)));
        LinearLayout text=vbox();
        text.addView(tv(title+" — "+value,10,TEXT,true));
        text.addView(tv(subtitle,9,MUTED,false));
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1f));
        return row;
    }

    private Button smallDelete(View.OnClickListener listener) {
        Button b=button("حذف",RED,SOFT_RED,false);
        b.setOnClickListener(listener);
        return b;
    }

    private String allocationSummary(Map<String,Long> allocations) {
        if(allocations==null || allocations.isEmpty()) return "بستانکاری / بدون تخصیص ماهانه";
        StringBuilder sb=new StringBuilder();
        int i=0;
        for(Map.Entry<String,Long> e:allocations.entrySet()) {
            if(i>0) sb.append("، ");
            sb.append(PersianDate.monthLabel(e.getKey())).append(": ").append(money(e.getValue()));
            i++;
            if(i>=3 && allocations.size()>3){sb.append(" ...");break;}
        }
        return sb.toString();
    }

    private View detailLine(String label,String value) {
        LinearLayout r=hbox();
        r.setPadding(0,dp(4),0,dp(4));
        r.addView(tv(label,10,MUTED,false),new LinearLayout.LayoutParams(0,-2,1f));
        r.addView(tv(value,11,TEXT,true),new LinearLayout.LayoutParams(0,-2,1.5f));
        return r;
    }

    private LinearLayout sectionCard(String title,int iconRes) {
        LinearLayout c=vbox();
        c.setPadding(dp(12),dp(10),dp(12),dp(10));
        c.setBackground(round(Color.WHITE,13,1,Color.rgb(224,207,185)));
        LinearLayout head=hbox();
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(iconView(iconRes,BROWN,19),lp(dp(34),dp(30)));
        head.addView(tv(title,13,DARK,true),new LinearLayout.LayoutParams(0,dp(30),1f));
        c.addView(head);
        return c;
    }

    private void showUnits() {
        LinearLayout body = vbox();
        body.setPadding(dp(10),dp(12),dp(10),dp(18));
        body.addView(tv("پروفایل واحدها",19,DARK,true));
        body.addView(tv("اطلاعات مالک، مستأجر، تعداد نفرات و کنتور فرعی",10,MUTED,false));

        for (DataStore.UnitInfo u : store.getUnits()) {
            final int n = u.number;
            LinearLayout row = hbox();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12),dp(10),dp(12),dp(10));
            row.setBackground(round(Color.WHITE,12,1,Color.rgb(224,207,185)));

            TextView badge=center(fa(u.number),15,Color.WHITE,true);
            badge.setBackground(round(BROWN,12,0,0));
            row.addView(badge,lp(dp(44),dp(44)));

            LinearLayout info = vbox();
            info.setPadding(dp(8),0,dp(8),0);
            info.addView(tv("واحد " + fa(u.number) + " — طبقه " + fa(u.floor),13,DARK,true));
            info.addView(tv(emptyDash(u.residentName()),10,MUTED,false));
            String meter=u.submeter?"کنتور فرعی آب فعال":"بدون کنتور فرعی";
            info.addView(tv(meter,9,u.submeter?BROWN:MUTED,false));
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1f));

            Button e = button("ویرایش",BROWN,CREAM2,true);
            e.setOnClickListener(v -> editUnit(n));
            row.addView(e,lp(dp(72),dp(38)));
            body.addView(row,rowParams());
        }
        setPage("units",scroll(body));
    }

    private void editUnit(final int number) {
        final DataStore.UnitInfo u = store.getUnit(number);
        if (u == null) return;

        LinearLayout form = vbox();
        form.setPadding(dp(14),dp(4),dp(14),dp(8));

        final EditText owner = labeledField(form,"نام مالک",u.owner,InputType.TYPE_CLASS_TEXT);
        final EditText ownerPhone = labeledField(form,"شماره موبایل مالک",u.ownerPhone,InputType.TYPE_CLASS_PHONE);

        form.addView(tv("وضعیت سکونت",11,DARK,true));
        final RadioGroup occGroup = new RadioGroup(this);
        occGroup.setOrientation(RadioGroup.VERTICAL);
        occGroup.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        final RadioButton ownerOcc=radio("مالک ساکن است");
        final RadioButton tenantOcc=radio("واحد در اختیار مستأجر است");
        final RadioButton vacantOcc=radio("واحد خالی است");
        occGroup.addView(ownerOcc); occGroup.addView(tenantOcc); occGroup.addView(vacantOcc);
        form.addView(occGroup);

        final LinearLayout tenantBox=vbox();
        final EditText tenant = labeledField(tenantBox,"نام مستأجر",u.tenant,InputType.TYPE_CLASS_TEXT);
        final EditText tenantPhone = labeledField(tenantBox,"شماره موبایل مستأجر",u.tenantPhone,InputType.TYPE_CLASS_PHONE);
        form.addView(tenantBox);

        final EditText persons = labeledField(form,"تعداد نفرات ساکن",String.valueOf(u.occupants),InputType.TYPE_CLASS_NUMBER);
        final CheckBox meter = check("این واحد کنتور فرعی آب دارد",u.submeter);
        form.addView(meter);

        final LinearLayout meterBox=vbox();
        final EditText last = labeledField(meterBox,"عدد مبنای اولیه کنتور فرعی",trim(u.lastWaterReading),
                InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        form.addView(meterBox);

        final EditText notes = labeledField(form,"توضیحات",u.notes,InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notes.setMinLines(2);

        if(DataStore.OCC_TENANT.equals(u.occupancy)) tenantOcc.setChecked(true);
        else if(DataStore.OCC_VACANT.equals(u.occupancy)) vacantOcc.setChecked(true);
        else ownerOcc.setChecked(true);

        Runnable refresh=()->{
            boolean tenantMode=tenantOcc.isChecked();
            boolean vacantMode=vacantOcc.isChecked();
            tenantBox.setVisibility(tenantMode?View.VISIBLE:View.GONE);
            persons.setEnabled(!vacantMode);
            if(vacantMode) persons.setText("0");
            meterBox.setVisibility(meter.isChecked()?View.VISIBLE:View.GONE);
        };
        occGroup.setOnCheckedChangeListener((g,id)->refresh.run());
        meter.setOnCheckedChangeListener((b,c)->refresh.run());
        refresh.run();

        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle("ویرایش واحد " + fa(number))
                .setView(scroll(form))
                .setNegativeButton("انصراف",null)
                .setPositiveButton("ذخیره",null)
                .create();

        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            u.owner=s(owner);
            u.ownerPhone=s(ownerPhone);
            u.tenant=s(tenant);
            u.tenantPhone=s(tenantPhone);
            if(tenantOcc.isChecked()) u.occupancy=DataStore.OCC_TENANT;
            else if(vacantOcc.isChecked()) u.occupancy=DataStore.OCC_VACANT;
            else u.occupancy=DataStore.OCC_OWNER;
            u.occupants=DataStore.OCC_VACANT.equals(u.occupancy)?0:Math.max(0,safeInt(s(persons),1));
            u.submeter=meter.isChecked();
            u.lastWaterReading=Math.max(0d,safeDouble(s(last),0d));
            u.notes=s(notes);
            store.updateUnit(u);
            dialog.dismiss();
            showUnits();
            toast("اطلاعات واحد ذخیره شد");
        }));
        dialog.show();
    }

    private RadioButton radio(String label) {
        RadioButton r=new RadioButton(this);
        r.setText(label);
        r.setTextColor(TEXT);
        r.setTextSize(12);
        r.setTypeface(font);
        return r;
    }

    private CheckBox check(String label, boolean checked) {
        CheckBox c=new CheckBox(this);
        c.setText(label);
        c.setTextColor(TEXT);
        c.setTextSize(12);
        c.setTypeface(font);
        c.setChecked(checked);
        return c;
    }

    private EditText labeledField(LinearLayout parent,String label,String value,int type) {
        TextView l=tv(label,10,MUTED,false);
        l.setPadding(dp(2),dp(5),dp(2),dp(2));
        parent.addView(l);
        EditText e=edit("",value,type);
        parent.addView(e,rowParams());
        return e;
    }

    private void showFinance() {
        LinearLayout body = vbox();
        body.setPadding(dp(10),dp(12),dp(10),dp(18));
        body.addView(tv("مدیریت مالی",19,DARK,true));
        body.addView(tv("ثبت و کنترل دریافت‌ها، هزینه‌ها و صندوق",10,MUTED,false));
        body.addView(menuButton(R.drawable.ic_payments,"شارژ ماهانه","مبلغ، تاریخ و پرداخت چندماهه",v->showCharge()));
        body.addView(menuButton(R.drawable.ic_water_drop,"قبض آب","مصرف، هزینه عمومی و پرداخت واحدها",v->showWater()));
        body.addView(menuButton(R.drawable.ic_receipt,"هزینه‌های ساختمان","برق عمومی، نگهبان، نظافت، تعمیرات و ...",v->showExpenses()));
        body.addView(menuButton(R.drawable.ic_wallet,"صندوق بلوک","موجودی، دریافتی‌ها، هزینه‌ها و اشتراک گزارش",v->showFund()));
        body.addView(menuButton(R.drawable.ic_sms,"تنظیم متن پیامک","متن یادآوری بدهی مالک یا مستأجر",v->showSmsSettings()));
        body.addView(menuButton(R.drawable.ic_backup,"پشتیبان‌گیری","خروجی و بازیابی اطلاعات برنامه",v->showBackup()));
        setPage("finance",scroll(body));
    }

    private View menuButton(int iconRes,String title,String desc,View.OnClickListener listener) {
        LinearLayout r=hbox();
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(12),dp(10),dp(12),dp(10));
        r.setBackground(round(Color.WHITE,13,1,Color.rgb(224,207,185)));

        FrameLayout badge=new FrameLayout(this);
        badge.setBackground(round(CREAM2,12,0,0));
        ImageView icon=iconView(iconRes,BROWN,21);
        FrameLayout.LayoutParams iconLp=new FrameLayout.LayoutParams(dp(26),dp(26),Gravity.CENTER);
        badge.addView(icon,iconLp);
        r.addView(badge,lp(dp(46),dp(46)));

        LinearLayout info=vbox();
        info.setPadding(dp(8),0,dp(8),0);
        info.addView(tv(title,14,DARK,true));
        info.addView(tv(desc,10,MUTED,false));
        r.addView(info,new LinearLayout.LayoutParams(0,-2,1f));
        TextView arrow=center("‹",27,BROWN,true);
        r.addView(arrow,lp(dp(34),dp(38)));
        r.setOnClickListener(listener);
        r.setLayoutParams(rowParams());
        return r;
    }

    private void showCharge() {
        DataStore.MonthData md=store.getMonth(monthKey);
        LinearLayout body=vbox();
        body.setPadding(dp(10),dp(12),dp(10),dp(18));
        body.addView(pageTitle("شارژ " + PersianDate.monthLabel(monthKey)));
        body.addView(tv("پرداخت‌ها با مبلغ و تاریخ ثبت می‌شوند و می‌توانند چند ماه را یکجا پوشش دهند.",10,MUTED,false));

        final EditText amount=moneyEdit("مبلغ شارژ هر واحد (تومان)",md.chargeAmount>0?String.valueOf(md.chargeAmount):"");
        body.addView(amount,rowParams());

        LinearLayout buttons=hbox();
        Button save=button("ذخیره مبلغ شارژ",Color.WHITE,BROWN,true);
        save.setOnClickListener(v->{
            long x=safeLong(s(amount),-1);
            if(x<0){toast("مبلغ شارژ را صحیح وارد کنید");return;}
            store.setChargeAmount(monthKey,x);
            showCharge();
            toast("مبلغ شارژ ذخیره شد");
        });
        buttons.addView(save,pairButtonParams(1.3f));

        Button image=button("اشتراک گزارش",BROWN,CREAM2,true);
        image.setOnClickListener(v->showChargeReportImage());
        buttons.addView(image,pairButtonParams(1f));
        body.addView(buttons,rowParams());

        body.addView(tv("وضعیت پرداخت واحدها",14,DARK,true));

        for(int n=1;n<=12;n++){
            final int unit=n;
            long due=store.chargeDue(monthKey,n);
            long paid=store.chargePaidAmount(monthKey,n);
            long remain=Math.max(0,due-paid);
            int st=store.chargeStatus(monthKey,n);

            LinearLayout r=financialRow("واحد "+fa(n),statusText(st)+" | مانده "+money(remain),st);
            Button pay=button("ثبت پرداخت",Color.WHITE,BROWN,true);
            pay.setEnabled(due>0);
            pay.setAlpha(due>0?1f:.45f);
            pay.setOnClickListener(v->showChargePaymentDialog(unit));
            r.addView(pay,lp(dp(92),dp(38)));
            body.addView(r,rowParams());
        }

        setPage("charge",scroll(body));
    }

    private String statusText(int st) {
        if(st==0) return "ثبت نشده";
        if(st==1) return "پرداخت نشده";
        if(st==2) return "پرداخت ناقص";
        return "تسویه";
    }

    private LinearLayout financialRow(String title,String desc,int status) {
        LinearLayout r=hbox();
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(10),dp(8),dp(10),dp(8));
        r.setBackground(round(Color.WHITE,11,1,Color.rgb(224,207,185)));
        TextView dot=center("●",13,statusColor(status),true);
        r.addView(dot,lp(dp(28),dp(34)));
        LinearLayout info=vbox();
        info.addView(tv(title,12,TEXT,true));
        info.addView(tv(desc,9,MUTED,false));
        r.addView(info,new LinearLayout.LayoutParams(0,-2,1f));
        return r;
    }

    private void showChargePaymentDialog(final int unit) {
        if(store.chargeDue(monthKey,unit)<=0){toast("ابتدا مبلغ شارژ این ماه را تعیین کنید");return;}

        LinearLayout form=vbox();
        form.setPadding(dp(14),dp(4),dp(14),dp(8));
        final EditText date=labeledField(form,"تاریخ پرداخت",PersianDate.todayKey(),InputType.TYPE_CLASS_TEXT);
        final EditText amount=moneyEdit("مبلغ پرداختی (تومان)","");
        form.addView(tv("مبلغ پرداختی",10,MUTED,false));
        form.addView(amount,rowParams());

        form.addView(tv("شروع تخصیص پرداخت",10,MUTED,false));
        LinearLayout monthPicker=hbox();
        final String[] start={monthKey};
        Button next=button("›",BROWN,CREAM2,true);
        TextView label=center(PersianDate.monthLabel(start[0]),12,DARK,true);
        Button prev=button("‹",BROWN,CREAM2,true);
        next.setOnClickListener(v->{start[0]=PersianDate.shiftMonth(start[0],1);label.setText(PersianDate.monthLabel(start[0]));});
        prev.setOnClickListener(v->{start[0]=PersianDate.shiftMonth(start[0],-1);label.setText(PersianDate.monthLabel(start[0]));});
        monthPicker.addView(next,lp(dp(48),dp(40)));
        monthPicker.addView(label,new LinearLayout.LayoutParams(0,dp(40),1f));
        monthPicker.addView(prev,lp(dp(48),dp(40)));
        form.addView(monthPicker,rowParams());

        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle("پرداخت شارژ واحد "+fa(unit))
                .setView(form)
                .setNegativeButton("انصراف",null)
                .setPositiveButton("پیش‌نمایش",null)
                .create();

        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            long value=safeLong(s(amount),-1);
            if(value<=0){toast("مبلغ پرداختی را وارد کنید");return;}
            try{
                DataStore.ChargePreview p=store.previewChargePayment(unit,start[0],value);
                StringBuilder msg=new StringBuilder();
                for(Map.Entry<String,Long> e:p.allocations.entrySet()){
                    long due=store.getMonth(e.getKey()).chargeAmount;
                    if(due<=0) due=store.getMonth(start[0]).chargeAmount;
                    msg.append("• ").append(PersianDate.monthLabel(e.getKey())).append(": ")
                            .append(money(e.getValue()));
                    if(e.getValue()<due) msg.append(" (ناقص)");
                    msg.append("\n");
                }
                if(p.credit>0) msg.append("\nبستانکاری باقی‌مانده: ").append(money(p.credit));

                new AlertDialog.Builder(this)
                        .setTitle("پیش‌نمایش تخصیص")
                        .setMessage(msg.length()==0?"مبلغی برای تخصیص پیدا نشد.":msg.toString())
                        .setNegativeButton("برگشت",null)
                        .setPositiveButton("ثبت نهایی",(dd,ww)->{
                            store.recordChargePayment(unit,s(date),start[0],value);
                            dialog.dismiss();
                            showCharge();
                            toast("پرداخت ثبت شد");
                        }).show();
            }catch(Exception ex){alert("خطا",ex.getMessage()==null?"اطلاعات را بررسی کنید":ex.getMessage());}
        }));
        dialog.show();
    }

    private void showWater() {
        DataStore.MonthData md=store.getMonth(monthKey);
        LinearLayout body=vbox();
        body.setPadding(dp(10),dp(12),dp(10),dp(18));
        body.addView(pageTitle("قبض آب " + PersianDate.monthLabel(monthKey)));
        body.addView(tv("هزینه عمومی بین همه واحدها تقسیم می‌شود. هزینه مصرف بر اساس کارکرد کل قبض و کنتورهای فرعی محاسبه می‌شود.",10,MUTED,false));

        final EditText consumptionAmount=moneyEdit("هزینه مصرف آب (تومان)",md.waterConsumptionAmount>0?String.valueOf(md.waterConsumptionAmount):"");
        final EditText generalAmount=moneyEdit("هزینه عمومی: فاضلاب، مالیات، آبونمان و ...",md.waterGeneralAmount>0?String.valueOf(md.waterGeneralAmount):"");
        final EditText main=edit("کارکرد کل درج‌شده روی قبض",md.mainWaterConsumption>0?trim(md.mainWaterConsumption):"",
                InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        body.addView(consumptionAmount,rowParams());
        body.addView(generalAmount,rowParams());
        body.addView(main,rowParams());

        final CheckBox byPeople=check("باقی‌مانده مصرف واحدهای بدون کنتور بر اساس تعداد نفرات تقسیم شود",md.waterSplitByOccupants);
        body.addView(byPeople);

        final Map<Integer,EditText> currents=new HashMap<>();
        boolean anyMeter=false;
        for(DataStore.UnitInfo u:store.getUnits()){
            if(!u.submeter) continue;
            anyMeter=true;
            LinearLayout meterCard=sectionCard("کنتور فرعی واحد "+fa(u.number),R.drawable.ic_water_drop);
            double prev=store.previousReadingForUnit(monthKey,u.number);
            meterCard.addView(tv("عدد قبلی: "+PersianDate.toFaDigits(trim(prev)),10,MUTED,false));
            EditText c=edit("عدد جدید کنتور",trim(store.savedCurrentReading(monthKey,u.number)),
                    InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            meterCard.addView(c,rowParams());
            body.addView(meterCard,rowParams());
            currents.put(u.number,c);
        }
        if(!anyMeter) body.addView(tv("هیچ واحدی کنتور فرعی ندارد؛ هزینه مصرف با روش انتخاب‌شده تقسیم می‌شود.",10,BROWN,false));

        LinearLayout actions=hbox();
        Button calc=button("محاسبه و پیش‌نمایش",Color.WHITE,BROWN,true);
        actions.addView(calc,pairButtonParams(1.3f));
        Button image=button("اشتراک گزارش",BROWN,CREAM2,true);
        image.setOnClickListener(v->showWaterReportImage());
        actions.addView(image,pairButtonParams(1f));
        body.addView(actions,rowParams());

        calc.setOnClickListener(v->{
            long consAmount=safeLong(s(consumptionAmount),-1);
            long genAmount=safeLong(s(generalAmount),-1);
            double totalUse=safeDouble(s(main),-1);
            Map<Integer,Double> readings=new HashMap<>();
            for(DataStore.UnitInfo u:store.getUnits()) if(u.submeter) {
                readings.put(u.number,safeDouble(s(currents.get(u.number)),-1));
            }
            try{
                DataStore.WaterCalculation r=store.calculateWater(monthKey,consAmount,genAmount,totalUse,byPeople.isChecked(),readings);
                StringBuilder msg=new StringBuilder();
                for(int n=1;n<=12;n++){
                    DataStore.UnitInfo u=store.getUnit(n);
                    msg.append("واحد ").append(fa(n)).append(": ");
                    if(u!=null && u.submeter) {
                        Double use=r.consumptions.get(n);
                        msg.append("مصرف ").append(PersianDate.toFaDigits(trim(use==null?0d:use))).append(" | ");
                    }
                    msg.append("جمع ").append(money(mapLong(r.finalShares,n))).append("\n");
                }

                new AlertDialog.Builder(this)
                        .setTitle("پیش‌نمایش تقسیم قبض")
                        .setMessage(msg.toString())
                        .setNegativeButton("برگشت",null)
                        .setPositiveButton("ذخیره قبض",(d,w)->{
                            store.saveWaterBill(monthKey,consAmount,genAmount,totalUse,byPeople.isChecked(),readings);
                            showWater();
                            toast("قبض آب ذخیره شد");
                        }).show();
            }catch(Exception ex){alert("خطا در محاسبه",ex.getMessage()==null?"اطلاعات واردشده را بررسی کنید":ex.getMessage());}
        });

        if(md.waterConsumptionAmount>0 || md.waterGeneralAmount>0){
            body.addView(tv("سهم و پرداخت واحدها",14,DARK,true));
            for(int n=1;n<=12;n++){
                final int unit=n;
                long share=store.waterShare(monthKey,n);
                long paid=store.waterPaidAmount(monthKey,n);
                long remain=store.waterRemaining(monthKey,n);
                int st=store.waterStatus(monthKey,n);

                String desc="سهم "+money(share)+" | پرداخت "+money(Math.min(paid,share))+" | مانده "+money(remain);
                LinearLayout r=financialRow("واحد "+fa(n),desc,st);
                Button pay=button(st==3?"تسویه":"ثبت پرداخت",st==3?GREEN:Color.WHITE,st==3?SOFT_GREEN:BROWN,true);
                pay.setEnabled(st!=3 && share>0);
                pay.setOnClickListener(v->showWaterPaymentDialog(unit));
                r.addView(pay,lp(dp(92),dp(38)));
                body.addView(r,rowParams());
            }
        }

        setPage("water",scroll(body));
    }

    private void showWaterPaymentDialog(final int unit) {
        long due=store.waterRemaining(monthKey,unit);
        if(due<=0){toast("قبض آب این واحد تسویه شده است");return;}

        LinearLayout form=vbox();
        form.setPadding(dp(14),dp(4),dp(14),dp(8));
        form.addView(tv("مانده قبض آب: "+money(due),11,DARK,true));
        final EditText date=labeledField(form,"تاریخ پرداخت",PersianDate.todayKey(),InputType.TYPE_CLASS_TEXT);
        final EditText amount=moneyEdit("مبلغ پرداختی (تومان)",String.valueOf(due));
        form.addView(tv("مبلغ پرداختی",10,MUTED,false));
        form.addView(amount,rowParams());

        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle("پرداخت آب واحد "+fa(unit))
                .setView(form)
                .setNegativeButton("انصراف",null)
                .setPositiveButton("پیش‌نمایش",null)
                .create();

        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            long value=safeLong(s(amount),-1);
            if(value<=0){toast("مبلغ پرداختی را وارد کنید");return;}
            try{
                DataStore.WaterPaymentPreview p=store.previewWaterPayment(unit,monthKey,value);
                StringBuilder msg=new StringBuilder();
                msg.append("تسویه آب: ").append(money(p.appliedWater));
                if(p.waterRemainingAfter>0) msg.append("\nمانده آب: ").append(money(p.waterRemainingAfter));
                if(p.transferredToCharge>0) {
                    msg.append("\n\nمازاد پرداخت: ").append(money(p.transferredToCharge));
                    msg.append("\nاین مبلغ به شارژ/بستانکاری واحد منتقل می‌شود.");
                    if(p.chargePreview!=null && !p.chargePreview.allocations.isEmpty()) {
                        msg.append("\n");
                        for(Map.Entry<String,Long> e:p.chargePreview.allocations.entrySet()) {
                            msg.append("\n• ").append(PersianDate.monthLabel(e.getKey())).append(": ").append(money(e.getValue()));
                        }
                    }
                }
                new AlertDialog.Builder(this)
                        .setTitle("پیش‌نمایش پرداخت")
                        .setMessage(msg.toString())
                        .setNegativeButton("برگشت",null)
                        .setPositiveButton("ثبت نهایی",(dd,ww)->{
                            store.recordWaterPayment(unit,monthKey,s(date),value);
                            dialog.dismiss();
                            showWater();
                            toast("پرداخت آب ثبت شد");
                        }).show();
            }catch(Exception ex){alert("خطا",ex.getMessage()==null?"اطلاعات را بررسی کنید":ex.getMessage());}
        }));
        dialog.show();
    }

    private long mapLong(Map<Integer,Long> m,int key){
        Long x=m.get(key);
        return x==null?0L:x;
    }

    private void showExpenses() {
        LinearLayout body=vbox();
        body.setPadding(dp(10),dp(12),dp(10),dp(18));
        body.addView(pageTitle("هزینه‌های " + PersianDate.monthLabel(monthKey)));

        final EditText title=edit("عنوان هزینه؛ مثال: برق عمومی","",InputType.TYPE_CLASS_TEXT);
        final EditText amount=moneyEdit("مبلغ (تومان)","");
        final EditText date=edit("تاریخ",PersianDate.todayKey(),InputType.TYPE_CLASS_TEXT);
        body.addView(title,rowParams());
        body.addView(amount,rowParams());
        body.addView(date,rowParams());

        Button add=button("＋ ثبت هزینه",Color.WHITE,BROWN,true);
        add.setOnClickListener(v->{
            String t=s(title);
            long a=safeLong(s(amount),-1);
            if(TextUtils.isEmpty(t)||a<=0){toast("عنوان و مبلغ هزینه را وارد کنید");return;}
            store.addExpense(monthKey,t,a,s(date));
            showExpenses();
            toast("هزینه ثبت شد");
        });
        body.addView(add,buttonParams());

        LinearLayout sum=sectionCard("جمع هزینه ماه",R.drawable.ic_payments);
        sum.addView(detailLine("مبلغ",money(store.monthExpenses(monthKey))));
        body.addView(sum,rowParams());

        for(final DataStore.Expense e:store.expensesForMonth(monthKey)){
            LinearLayout r=historyRow(e.title,money(e.amount),e.date);
            Button del=smallDelete(v->new AlertDialog.Builder(this)
                    .setTitle("حذف هزینه")
                    .setMessage("این هزینه حذف شود؟")
                    .setNegativeButton("خیر",null)
                    .setPositiveButton("بله",(d,w)->{store.deleteExpense(e.id);showExpenses();})
                    .show());
            r.addView(del,lp(dp(58),dp(34)));
            body.addView(r,rowParams());
        }
        setPage("expenses",scroll(body));
    }

    private void showFund() {
        LinearLayout body=vbox();
        body.setPadding(dp(10),dp(12),dp(10),dp(18));
        body.addView(pageTitle("صندوق بلوک"));

        LinearLayout big=vbox();
        big.setGravity(Gravity.CENTER);
        big.setPadding(dp(10),dp(18),dp(10),dp(18));
        big.setBackground(round(DARK,16,0,0));
        big.addView(center("◉  موجودی فعلی صندوق",12,Color.WHITE,false));
        big.addView(center(money(store.fundBalance()),22,GOLD,true));
        body.addView(big,rowParams());

        LinearLayout stats=hbox();
        miniStat(stats,"شارژ",store.totalChargeCashIncome(),GREEN);
        miniStat(stats,"آب",store.totalWaterCashIncome(),GREEN);
        miniStat(stats,"هزینه",store.totalExpenses(),RED);
        body.addView(stats,rowParams());

        final EditText initial=moneyEdit("موجودی اولیه صندوق",String.valueOf(store.getInitialFund()));
        body.addView(initial,rowParams());

        LinearLayout actions=hbox();
        Button save=button("ذخیره موجودی اولیه",Color.WHITE,BROWN,true);
        save.setOnClickListener(v->{
            long x=safeLong(s(initial),-1);
            if(x<0){toast("عدد صحیح وارد کنید");return;}
            store.setInitialFund(x);
            showFund();
        });
        actions.addView(save,pairButtonParams(1.3f));

        Button image=button("اشتراک گزارش",BROWN,CREAM2,true);
        image.setOnClickListener(v->showFundReportImage());
        actions.addView(image,pairButtonParams(1f));
        body.addView(actions,rowParams());

        body.addView(tv("ریز هزینه‌های "+PersianDate.monthLabel(monthKey),14,DARK,true));
        List<DataStore.Expense> expenses=store.expensesForMonth(monthKey);
        if(expenses.isEmpty()) body.addView(tv("برای این ماه هزینه‌ای ثبت نشده است.",10,MUTED,false));
        for(DataStore.Expense e:expenses) body.addView(historyRow(e.title,money(e.amount),e.date),rowParams());

        setPage("fund",scroll(body));
    }

    private void miniStat(LinearLayout row,String title,long value,int color) {
        LinearLayout c=vbox();
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(3),dp(8),dp(3),dp(8));
        c.setBackground(round(Color.WHITE,11,1,Color.rgb(224,207,185)));
        c.addView(center(title,9,MUTED,false));
        c.addView(center(money(value),9,color,true));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(62),1f);
        p.setMargins(dp(3),0,dp(3),0);
        row.addView(c,p);
    }

    private void showSmsSettings() {
        LinearLayout body=vbox();
        body.setPadding(dp(10),dp(12),dp(10),dp(18));
        body.addView(pageTitle("تنظیم متن پیامک"));
        body.addView(tv("متغیرها: {نام}  {واحد}  {ماه}  {مبلغ}",10,MUTED,false));

        final EditText msg=edit("متن پیامک",store.getSmsTemplate(),InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        msg.setMinLines(5);
        body.addView(msg,rowParams());

        Button save=button("ذخیره متن پیامک",Color.WHITE,BROWN,true);
        save.setOnClickListener(v->{
            if(TextUtils.isEmpty(s(msg))){toast("متن پیامک خالی است");return;}
            store.setSmsTemplate(s(msg));
            toast("متن پیامک ذخیره شد");
        });
        body.addView(save,buttonParams());
        setPage("sms",scroll(body));
    }

    private void showBackup() {
        LinearLayout body=vbox();
        body.setPadding(dp(12),dp(12),dp(12),dp(20));
        body.addView(pageTitle("پشتیبان‌گیری اطلاعات"));
        body.addView(tv("برای حفظ اطلاعات واقعی ساختمان، هر چند وقت یکبار فایل پشتیبان بگیرید.",10,MUTED,false));

        Button export=button("↓ ذخیره فایل پشتیبان",Color.WHITE,BROWN,true);
        export.setOnClickListener(v->{
            Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/json");
            i.putExtra(Intent.EXTRA_TITLE,"Hamyar-A1-Backup-"+PersianDate.todayKey().replace("/","-")+".json");
            startActivityForResult(i,REQ_BACKUP_CREATE);
        });
        body.addView(export,buttonParams());

        Button restore=button("↑ بازیابی از فایل پشتیبان",BROWN,CREAM2,true);
        restore.setOnClickListener(v->new AlertDialog.Builder(this)
                .setTitle("بازیابی اطلاعات")
                .setMessage("اطلاعات فعلی با محتوای فایل پشتیبان جایگزین می‌شود. ادامه می‌دهید؟")
                .setNegativeButton("انصراف",null)
                .setPositiveButton("انتخاب فایل",(d,w)->{
                    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("application/json");
                    startActivityForResult(i,REQ_BACKUP_OPEN);
                }).show());
        body.addView(restore,buttonParams());
        setPage("backup",scroll(body));
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK || data==null || data.getData()==null) return;
        Uri uri=data.getData();
        try{
            if(requestCode==REQ_BACKUP_CREATE){
                OutputStream out=getContentResolver().openOutputStream(uri);
                if(out==null) throw new Exception("فایل باز نشد");
                out.write(store.exportJson().getBytes("UTF-8"));
                out.flush();
                out.close();
                toast("فایل پشتیبان ذخیره شد");
            } else if(requestCode==REQ_BACKUP_OPEN){
                InputStream in=getContentResolver().openInputStream(uri);
                if(in==null) throw new Exception("فایل باز نشد");
                BufferedReader br=new BufferedReader(new InputStreamReader(in,"UTF-8"));
                StringBuilder sb=new StringBuilder();
                String line;
                while((line=br.readLine())!=null) sb.append(line).append('\n');
                br.close();
                store.importJson(sb.toString());
                monthKey=PersianDate.currentMonthKey();
                if(monthLabel!=null) monthLabel.setText(PersianDate.monthLabel(monthKey));
                showDashboard();
                toast("اطلاعات پشتیبان بازیابی شد");
            }
        }catch(Exception e){alert("خطای پشتیبان‌گیری",e.getMessage()==null?"عملیات انجام نشد":e.getMessage());}
    }

    private void showReports() {
        LinearLayout body=vbox();
        body.setPadding(dp(10),dp(12),dp(10),dp(18));
        body.addView(tv("گزارش مالی",19,DARK,true));
        body.addView(tv(PersianDate.monthLabel(monthKey),11,BROWN,true));

        LinearLayout summary=sectionCard("خلاصه ماه",R.drawable.ic_bar_chart);
        summary.addView(detailLine("شارژ تعیین‌شده",money(store.monthChargeDue(monthKey))));
        summary.addView(detailLine("شارژ وصول‌شده",money(store.monthChargePaid(monthKey))));
        summary.addView(detailLine("آب تعیین‌شده",money(store.monthWaterDue(monthKey))));
        summary.addView(detailLine("آب وصول‌شده",money(store.monthWaterPaid(monthKey))));
        summary.addView(detailLine("هزینه‌های ماه",money(store.monthExpenses(monthKey))));
        summary.addView(detailLine("کل مطالبات تا این ماه",money(store.totalDebtThrough(monthKey))));
        summary.addView(detailLine("موجودی صندوق",money(store.fundBalance())));
        body.addView(summary,rowParams());

        Button image=button("اشتراک گزارشی گزارش ماه",Color.WHITE,BROWN,true);
        image.setOnClickListener(v->showMonthlyReportImage());
        body.addView(image,buttonParams());

        body.addView(tv("واحدهای بدهکار",14,DARK,true));
        boolean any=false;
        for(int n=1;n<=12;n++){
            long debt=store.unitTotalDebtThrough(monthKey,n);
            if(debt<=0) continue;
            any=true;
            final int unit=n;
            LinearLayout r=financialRow("واحد "+fa(n),money(debt),1);
            Button sms=button("پیامک",Color.WHITE,BROWN,true);
            sms.setOnClickListener(v->sendUnitReminder(unit));
            r.addView(sms,lp(dp(66),dp(36)));
            body.addView(r,rowParams());
        }
        if(!any) body.addView(tv("تا این ماه بدهی ثبت‌شده‌ای وجود ندارد.",11,GREEN,false));
        setPage("reports",scroll(body));
    }

    private void sendUnitReminder(int number) {
        DataStore.UnitInfo u=store.getUnit(number);
        if(u==null) return;
        long debt=store.unitTotalDebtThrough(monthKey,number);
        if(debt<=0){toast("برای این واحد بدهی ثبت نشده است");return;}

        final List<String> phones=new ArrayList<>();
        final List<String> names=new ArrayList<>();
        final List<String> labels=new ArrayList<>();

        if(!TextUtils.isEmpty(u.ownerPhone)){
            phones.add(u.ownerPhone);
            names.add(TextUtils.isEmpty(u.owner)?"مالک محترم":u.owner);
            labels.add("مالک: "+(TextUtils.isEmpty(u.owner)?u.ownerPhone:u.owner));
        }
        if(DataStore.OCC_TENANT.equals(u.occupancy) && !TextUtils.isEmpty(u.tenantPhone)){
            phones.add(u.tenantPhone);
            names.add(TextUtils.isEmpty(u.tenant)?"مستأجر محترم":u.tenant);
            labels.add("مستأجر: "+(TextUtils.isEmpty(u.tenant)?u.tenantPhone:u.tenant));
        }

        if(phones.isEmpty()){toast("شماره موبایل مالک یا مستأجر ثبت نشده است");return;}
        if(phones.size()==1){previewSms(phones.get(0),reminder(names.get(0),number,debt));return;}

        new AlertDialog.Builder(this)
                .setTitle("گیرنده پیامک")
                .setItems(labels.toArray(new String[0]),(d,w)->previewSms(phones.get(w),reminder(names.get(w),number,debt)))
                .show();
    }

    private String reminder(String name,int unit,long debt) {
        return store.getSmsTemplate()
                .replace("{نام}",name)
                .replace("{واحد}",fa(unit))
                .replace("{ماه}",PersianDate.monthLabel(monthKey))
                .replace("{مبلغ}",PersianDate.toFaDigits(format(debt)));
    }

    private void previewSms(String phone,String body) {
        new AlertDialog.Builder(this)
                .setTitle("پیش‌نمایش پیامک")
                .setMessage(body)
                .setNegativeButton("انصراف",null)
                .setPositiveButton("ارسال پیامک",(d,w)->openSms(phone,body))
                .show();
    }

    private void openSms(String phone,String body) {
        String p=PersianDate.normalizeDigits(phone).replace(" ","").replace("-","");
        try{
            Intent i=new Intent(Intent.ACTION_SENDTO);
            i.setData(Uri.parse("smsto:"+p));
            i.putExtra("sms_body",body);
            startActivity(i);
        }catch(ActivityNotFoundException e){toast("برنامه پیامک روی گوشی پیدا نشد");}
    }

    private void showChargeReportImage() {
        List<ReportImageUtil.Row> rows=new ArrayList<>();
        rows.add(new ReportImageUtil.Row("مبلغ شارژ هر واحد",money(store.getMonth(monthKey).chargeAmount),4));
        rows.add(new ReportImageUtil.Row("جمع شارژ ماه",money(store.monthChargeDue(monthKey))));
        rows.add(new ReportImageUtil.Row("وصول‌شده",money(store.monthChargePaid(monthKey)),1));
        rows.add(new ReportImageUtil.Row("مانده مطالبات شارژ",money(Math.max(0,store.monthChargeDue(monthKey)-store.monthChargePaid(monthKey))),2));
        for(int n=1;n<=12;n++) {
            int st=store.chargeStatus(monthKey,n);
            String value=statusText(st);
            long rem=store.chargeRemaining(monthKey,n);
            if(rem>0) value+=" | "+money(rem);
            rows.add(new ReportImageUtil.Row("واحد "+fa(n),value,st==3?1:(st==2?3:(st==1?2:0))));
        }
        showReportActions("گزارش شارژ","ماه "+PersianDate.monthLabel(monthKey),rows,"charge-"+monthKey.replace("/","-"));
    }

    private void showWaterReportImage() {
        DataStore.MonthData md=store.getMonth(monthKey);
        if(md.waterConsumptionAmount<=0 && md.waterGeneralAmount<=0){toast("ابتدا قبض آب را ثبت کنید");return;}
        List<ReportImageUtil.Row> rows=new ArrayList<>();
        rows.add(new ReportImageUtil.Row("هزینه مصرف آب",money(md.waterConsumptionAmount),4));
        rows.add(new ReportImageUtil.Row("هزینه عمومی",money(md.waterGeneralAmount),4));
        rows.add(new ReportImageUtil.Row("کارکرد کل قبض",PersianDate.toFaDigits(trim(md.mainWaterConsumption))));
        rows.add(new ReportImageUtil.Row("روش تقسیم بدون کنتور",md.waterSplitByOccupants?"بر اساس تعداد نفرات":"تقسیم مساوی"));
        for(int n=1;n<=12;n++) {
            int st=store.waterStatus(monthKey,n);
            String value=money(store.waterShare(monthKey,n))+" | "+statusText(st);
            rows.add(new ReportImageUtil.Row("واحد "+fa(n),value,st==3?1:(st==2?3:(st==1?2:0))));
        }
        showReportActions("گزارش قبض آب","ماه "+PersianDate.monthLabel(monthKey),rows,"water-"+monthKey.replace("/","-"));
    }

    private void showFundReportImage() {
        List<ReportImageUtil.Row> rows=new ArrayList<>();
        rows.add(new ReportImageUtil.Row("موجودی اولیه",money(store.getInitialFund())));
        rows.add(new ReportImageUtil.Row("دریافتی نقدی شارژ",money(store.totalChargeCashIncome()),1));
        rows.add(new ReportImageUtil.Row("دریافتی نقدی آب",money(store.totalWaterCashIncome()),1));
        rows.add(new ReportImageUtil.Row("کل هزینه‌ها",money(store.totalExpenses()),2));
        rows.add(new ReportImageUtil.Row("موجودی فعلی صندوق",money(store.fundBalance()),4));
        rows.add(new ReportImageUtil.Row("هزینه‌های "+PersianDate.monthLabel(monthKey),money(store.monthExpenses(monthKey)),2));
        for(DataStore.Expense e:store.expensesForMonth(monthKey)) {
            rows.add(new ReportImageUtil.Row(e.title,money(e.amount)));
        }
        showReportActions("گزارش صندوق","تا "+PersianDate.monthLabel(monthKey),rows,"fund-"+monthKey.replace("/","-"));
    }

    private void showMonthlyReportImage() {
        List<ReportImageUtil.Row> rows=new ArrayList<>();
        rows.add(new ReportImageUtil.Row("شارژ تعیین‌شده",money(store.monthChargeDue(monthKey))));
        rows.add(new ReportImageUtil.Row("شارژ وصول‌شده",money(store.monthChargePaid(monthKey)),1));
        rows.add(new ReportImageUtil.Row("آب تعیین‌شده",money(store.monthWaterDue(monthKey))));
        rows.add(new ReportImageUtil.Row("آب وصول‌شده",money(store.monthWaterPaid(monthKey)),1));
        rows.add(new ReportImageUtil.Row("هزینه ماه",money(store.monthExpenses(monthKey)),2));
        rows.add(new ReportImageUtil.Row("کل مطالبات تا این ماه",money(store.totalDebtThrough(monthKey)),2));
        rows.add(new ReportImageUtil.Row("موجودی صندوق",money(store.fundBalance()),4));
        for(int n=1;n<=12;n++) {
            long debt=store.unitTotalDebtThrough(monthKey,n);
            rows.add(new ReportImageUtil.Row("واحد "+fa(n),debt>0?("بدهی "+money(debt)):"تسویه",debt>0?2:1));
        }
        showReportActions("گزارش مالی ساختمان","ماه "+PersianDate.monthLabel(monthKey),rows,"monthly-"+monthKey.replace("/","-"));
    }

    private void showUnitReportImage(int unit) {
        DataStore.UnitInfo u=store.getUnit(unit);
        if(u==null) return;
        List<ReportImageUtil.Row> rows=new ArrayList<>();
        rows.add(new ReportImageUtil.Row("نام ساکن",emptyDash(u.residentName())));
        rows.add(new ReportImageUtil.Row("شارژ این ماه",money(store.chargeDue(monthKey,unit))));
        rows.add(new ReportImageUtil.Row("پرداخت شارژ",money(store.chargePaidAmount(monthKey,unit)),1));
        rows.add(new ReportImageUtil.Row("مانده شارژ",money(store.chargeRemaining(monthKey,unit)),store.chargeRemaining(monthKey,unit)>0?2:1));
        rows.add(new ReportImageUtil.Row("سهم آب",money(store.waterShare(monthKey,unit))));
        rows.add(new ReportImageUtil.Row("پرداخت آب",money(store.waterPaidAmount(monthKey,unit)),1));
        rows.add(new ReportImageUtil.Row("مانده آب",money(store.waterRemaining(monthKey,unit)),store.waterRemaining(monthKey,unit)>0?2:1));
        if(store.chargeCredit(unit)>0) rows.add(new ReportImageUtil.Row("بستانکاری شارژ",money(store.chargeCredit(unit)),1));
        rows.add(new ReportImageUtil.Row("کل بدهی تا این ماه",money(store.unitTotalDebtThrough(monthKey,unit)),store.unitTotalDebtThrough(monthKey,unit)>0?2:1));
        showReportActions("صورتحساب واحد "+fa(unit),"تا "+PersianDate.monthLabel(monthKey),rows,"unit-"+unit+"-"+monthKey.replace("/","-"));
    }

    private void showReportActions(String title,String subtitle,List<ReportImageUtil.Row> rows,String filename) {
        try {
            Bitmap bitmap=ReportImageUtil.create(this,title,subtitle,rows);
            shareReport(bitmap,filename);
        } catch(Exception e) {
            alert("خطا در ساخت تصویر",e.getMessage()==null?"تصویر ساخته نشد":e.getMessage());
        }
    }

    private void shareReport(Bitmap bitmap,String filename) {
        try {
            Uri uri=ReportImageUtil.cacheForShare(this,bitmap,filename);
            Intent i=new Intent(Intent.ACTION_SEND);
            i.setType("image/png");
            i.putExtra(Intent.EXTRA_STREAM,uri);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i,"اشتراک‌گذاری گزارش"));
        } catch(Exception e) {
            alert("خطا",e.getMessage()==null?"اشتراک‌گذاری انجام نشد":e.getMessage());
        }
    }

    private void saveReport(Bitmap bitmap,String filename) {
        try {
            ReportImageUtil.saveToGallery(this,bitmap,filename);
            toast("تصویر گزارش ذخیره شد");
        } catch(Exception e) {
            alert("خطا",e.getMessage()==null?"ذخیره تصویر انجام نشد":e.getMessage());
        }
    }

    private TextView pageTitle(String title) {
        TextView t=tv(title,19,DARK,true);
        t.setPadding(0,0,0,dp(4));
        return t;
    }

    private ImageView iconView(int resId,int tint,int sizeDp) {
        ImageView image=new ImageView(this);
        image.setImageResource(resId);
        image.setColorFilter(tint);
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        image.setPadding(dp(2),dp(2),dp(2),dp(2));
        return image;
    }

    private LinearLayout.LayoutParams pairButtonParams(float weight) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),weight);
        p.setMargins(dp(4),0,dp(4),0);
        return p;
    }

    private TextView tv(String value,float size,int color,boolean isBold) {
        TextView t=new TextView(this);
        t.setText(value==null?"":value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(isBold?bold:font);
        t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        return t;
    }

    private TextView center(String value,float size,int color,boolean isBold) {
        TextView t=tv(value,size,color,isBold);
        t.setGravity(Gravity.CENTER);
        t.setMaxLines(1);
        t.setEllipsize(TextUtils.TruncateAt.END);
        return t;
    }

    private EditText edit(String hint,String value,int type) {
        EditText e=new EditText(this);
        e.setHint(hint);
        e.setText(value==null?"":value);
        e.setTextSize(12);
        e.setTextColor(TEXT);
        e.setHintTextColor(Color.rgb(145,130,116));
        e.setTypeface(font);
        e.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        e.setTextDirection(View.TEXT_DIRECTION_RTL);
        e.setInputType(type);
        e.setPadding(dp(12),dp(9),dp(12),dp(9));
        e.setBackground(round(Color.WHITE,11,1,Color.rgb(216,198,176)));
        return e;
    }

    private EditText moneyEdit(String hint,String value) {
        final EditText e=edit(hint,"",InputType.TYPE_CLASS_NUMBER);
        long initial=safeLong(value,0);
        if(initial>0) e.setText(format(initial));
        e.addTextChangedListener(new TextWatcher() {
            boolean editing=false;
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){}
            @Override public void afterTextChanged(Editable editable){
                if(editing) return;
                String raw=PersianDate.normalizeDigits(editable.toString()).replace(",","").replace("٬","").replace(" ","");
                if(raw.isEmpty()) return;
                try{
                    long x=Long.parseLong(raw);
                    String formatted=format(x);
                    if(!formatted.equals(editable.toString())){
                        editing=true;
                        e.setText(formatted);
                        e.setSelection(e.getText().length());
                        editing=false;
                    }
                }catch(Exception ignored){}
            }
        });
        return e;
    }

    private Button button(String label,int color,int bg,boolean isBold) {
        Button b=new Button(this);
        b.setText(label);
        b.setTextColor(color);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setTypeface(isBold?bold:font);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(6),0,dp(6),0);
        if(bg==Color.TRANSPARENT) b.setBackgroundColor(Color.TRANSPARENT);
        else b.setBackground(round(bg,11,0,0));
        return b;
    }

    private LinearLayout vbox() {
        LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }

    private LinearLayout hbox() {
        LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }

    private ScrollView scroll(View v) {
        ScrollView s=new ScrollView(this);
        s.setFillViewport(true);
        s.setBackgroundColor(CREAM);
        s.addView(v,new ScrollView.LayoutParams(-1,-2));
        return s;
    }

    private GradientDrawable round(int color,int radiusDp,int strokeDp,int strokeColor) {
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        if(strokeDp>0) g.setStroke(dp(strokeDp),strokeColor);
        return g;
    }

    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private LinearLayout.LayoutParams rowParams(){LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,dp(4),0,dp(4));return p;}
    private LinearLayout.LayoutParams buttonParams(){LinearLayout.LayoutParams p=lp(-1,dp(46));p.setMargins(0,dp(5),0,dp(8));return p;}

    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private String s(EditText e){return e==null?"":e.getText().toString().trim();}
    private int safeInt(String x,int fallback){try{return Integer.parseInt(PersianDate.normalizeDigits(x).replace(",","").trim());}catch(Exception e){return fallback;}}
    private long safeLong(String x,long fallback){try{return Long.parseLong(PersianDate.normalizeDigits(x).replace(",","").replace("٬","").replace(" ","").trim());}catch(Exception e){return fallback;}}
    private double safeDouble(String x,double fallback){try{return Double.parseDouble(PersianDate.normalizeDigits(x).replace("٫",".").replace(",",".").replace(" ","").trim());}catch(Exception e){return fallback;}}
    private String trim(double x){if(Math.abs(x-Math.rint(x))<0.000001)return String.valueOf((long)Math.rint(x));return String.format(Locale.US,"%.2f",x).replaceAll("0+$","").replaceAll("\\.$","");}
    private String format(long x){return new DecimalFormat("#,###").format(x);}
    private String money(long x){return PersianDate.toFaDigits(format(x))+" تومان";}
    private String fa(int x){return PersianDate.toFaDigits(String.valueOf(x));}
    private String emptyDash(String x){return x==null||x.trim().isEmpty()?"—":x;}
    private void toast(String x){Toast.makeText(this,x,Toast.LENGTH_SHORT).show();}
    private void alert(String title,String msg){new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("باشه",null).show();}
}
