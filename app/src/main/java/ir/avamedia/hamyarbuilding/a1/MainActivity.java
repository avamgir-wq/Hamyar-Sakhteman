package ir.avamedia.hamyarbuilding.a1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

    private DataStore store;
    private Typeface font = Typeface.DEFAULT;
    private Typeface bold = Typeface.DEFAULT_BOLD;
    private LinearLayout root;
    private FrameLayout content;
    private Spinner monthSpinner;
    private String monthKey;
    private String screen = "dashboard";
    private boolean changingMonth;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        try {
            Window w = getWindow();
            w.setStatusBarColor(DARK);
            w.setNavigationBarColor(DARK);
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
        LinearLayout p = vbox(16);
        p.setPadding(dp(24),dp(24),dp(24),dp(24));
        p.setBackgroundColor(CREAM);
        p.addView(tv("همیار ساختمان",24,DARK,true));
        p.addView(tv("خطای راه‌اندازی برنامه\n\n" + e.getClass().getSimpleName() + "\n" +
                (e.getMessage()==null ? "" : e.getMessage()),14,RED,false));
        setContentView(p);
    }

    private void buildShell() {
        root = vbox(0);
        root.setBackgroundColor(CREAM);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(root);

        LinearLayout header = vbox(6);
        header.setPadding(dp(12),dp(10),dp(12),dp(8));
        header.setBackgroundColor(DARK);
        root.addView(header, lp(-1,-2));

        LinearLayout top = hbox();
        top.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(top, lp(-1,-2));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.app_icon);
        icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        icon.setBackground(round(Color.WHITE,12,0,0));
        top.addView(icon, lp(dp(54),dp(54)));

        LinearLayout names = vbox(0);
        names.setPadding(dp(10),0,dp(10),0);
        top.addView(names, new LinearLayout.LayoutParams(0,-2,1f));
        names.addView(tv("همیار ساختمان",20,Color.WHITE,true));
        names.addView(tv("بلوک A1 مجتمع فرهیختگان",12,GOLD,false));

        TextView badge = tv("A1",16,DARK,true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(GOLD,10,0,0));
        top.addView(badge, lp(dp(46),dp(38)));

        LinearLayout monthRow = hbox();
        monthRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(monthRow, lp(-1,dp(44)));
        monthRow.addView(tv("ماه مالی",12,Color.WHITE,false), lp(dp(70),-1));

        final List<String> keys = PersianDate.nearbyMonthKeys();
        List<String> labels = new ArrayList<>();
        for (String k : keys) labels.add(PersianDate.monthLabel(k));
        monthSpinner = new Spinner(this);
        ArrayAdapter<String> a = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, labels) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView t = tv(getItem(position),12,Color.WHITE,true);
                t.setGravity(Gravity.CENTER);
                t.setPadding(dp(8),0,dp(8),0);
                t.setBackground(round(Color.rgb(78,49,33),9,1,Color.rgb(115,77,50)));
                return t;
            }
            @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView t = tv(getItem(position),13,TEXT,false);
                t.setPadding(dp(12),dp(12),dp(12),dp(12));
                t.setBackgroundColor(Color.WHITE);
                return t;
            }
        };
        monthSpinner.setAdapter(a);
        int ix = keys.indexOf(monthKey);
        if (ix < 0) ix = keys.size()/2;
        changingMonth = true;
        monthSpinner.setSelection(ix,false);
        changingMonth = false;
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
                if (changingMonth || position < 0 || position >= keys.size()) return;
                String k = keys.get(position);
                if (!k.equals(monthKey)) {
                    monthKey = k;
                    render();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        monthRow.addView(monthSpinner, new LinearLayout.LayoutParams(0,dp(40),1f));

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(-1,0,1f));

        LinearLayout nav = hbox();
        nav.setPadding(dp(4),dp(4),dp(4),dp(4));
        nav.setBackgroundColor(DARK);
        root.addView(nav, lp(-1,dp(62)));
        addNav(nav,"داشبورد","dashboard");
        addNav(nav,"واحدها","units");
        addNav(nav,"مالی","finance");
        addNav(nav,"گزارش","reports");
    }

    private void addNav(LinearLayout nav, String label, final String target) {
        Button b = button(label,Color.WHITE,Color.TRANSPARENT,false);
        b.setTextSize(11);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if ("dashboard".equals(target)) showDashboard();
                else if ("units".equals(target)) showUnits();
                else if ("finance".equals(target)) showFinance();
                else showReports();
            }
        });
        nav.addView(b,new LinearLayout.LayoutParams(0,-1,1f));
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
        else showReports();
    }

    private void setPage(String name, View page) {
        screen = name;
        content.removeAllViews();
        content.addView(page,new FrameLayout.LayoutParams(-1,-1));
    }

    private void showDashboard() {
        LinearLayout body = vbox(8);
        body.setPadding(dp(10),dp(10),dp(10),dp(16));

        LinearLayout stats = hbox();
        body.addView(stats,lp(-1,-2));
        stat(stats,"موجودی صندوق",store.fundBalance());
        stat(stats,"مطالبات ماه",store.monthDebt(monthKey));
        stat(stats,"هزینه ماه",store.monthExpenses(monthKey));

        TextView h = tv("وضعیت واحدها",17,DARK,true);
        h.setPadding(dp(3),dp(8),dp(3),0);
        body.addView(h);
        body.addView(tv("۴ طبقه × ۳ واحد — وضعیت شارژ و آب در " + PersianDate.monthLabel(monthKey),10,MUTED,false));

        int[][] rows = {{10,11,12},{7,8,9},{4,5,6},{1,2,3}};
        for (int[] row : rows) {
            LinearLayout r = hbox();
            body.addView(r,lp(-1,-2));
            for (int n : row) {
                final int unit = n;
                DataStore.UnitInfo u = store.getUnit(n);
                DataStore.MonthData md = store.getMonth(monthKey);
                boolean cp = store.isChargePaid(monthKey,n);
                boolean wp = store.isWaterPaid(monthKey,n);
                String cs = md.chargeAmount<=0 ? "شارژ —" : (cp ? "شارژ ✓" : "شارژ ✕");
                long ws = store.waterShare(monthKey,n);
                String water = ws<=0 ? "آب —" : (wp ? "آب ✓" : "آب ✕");
                String resident = "";
                if (u != null) {
                    if (!TextUtils.isEmpty(u.tenant)) resident=u.tenant;
                    else if (!TextUtils.isEmpty(u.owner)) resident=u.owner;
                }
                LinearLayout card = vbox(2);
                card.setGravity(Gravity.CENTER);
                card.setPadding(dp(4),dp(7),dp(4),dp(7));
                card.setBackground(round(Color.WHITE,12,1,Color.rgb(221,204,182)));
                card.addView(center("واحد " + fa(n),15,DARK,true));
                card.addView(center(resident,9,MUTED,false));
                card.addView(center(cs,10,cp?GREEN:(md.chargeAmount>0?RED:MUTED),false));
                card.addView(center(water,10,wp?GREEN:(ws>0?RED:MUTED),false));
                card.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { editUnit(unit); }
                });
                card.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override public boolean onLongClick(View v) { sendUnitReminder(unit); return true; }
                });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(112),1f);
                p.setMargins(dp(3),dp(4),dp(3),dp(4));
                r.addView(card,p);
            }
        }
        setPage("dashboard",scroll(body));
    }

    private void stat(LinearLayout row, String title, long value) {
        LinearLayout c = vbox(2);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(4),dp(8),dp(4),dp(8));
        c.setBackground(round(CREAM2,10,1,Color.rgb(218,196,169)));
        c.addView(center(title,9,MUTED,false));
        c.addView(center(money(value),11,DARK,true));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(70),1f);
        p.setMargins(dp(3),0,dp(3),0);
        row.addView(c,p);
    }

    private void showUnits() {
        LinearLayout body = vbox(7);
        body.setPadding(dp(10),dp(10),dp(10),dp(16));
        body.addView(tv("پروفایل ۱۲ واحد",18,DARK,true));
        for (DataStore.UnitInfo u : store.getUnits()) {
            final int n = u.number;
            LinearLayout row = hbox();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10),dp(8),dp(10),dp(8));
            row.setBackground(round(Color.WHITE,10,1,Color.rgb(224,207,185)));
            LinearLayout info = vbox(1);
            info.addView(tv("واحد " + fa(u.number) + " — طبقه " + fa(u.floor),13,DARK,true));
            String who = TextUtils.isEmpty(u.tenant) ? u.owner : u.tenant;
            info.addView(tv(TextUtils.isEmpty(who) ? "اطلاعات ساکن ثبت نشده" : who,10,MUTED,false));
            info.addView(tv(u.submeter ? "دارای کنتور فرعی آب" : "بدون کنتور فرعی آب",9,u.submeter?BROWN:MUTED,false));
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1f));
            Button e = button("ویرایش",BROWN,CREAM2,true);
            e.setOnClickListener(new View.OnClickListener(){ @Override public void onClick(View v){ editUnit(n); }});
            row.addView(e,lp(dp(70),dp(38)));
            body.addView(row,rowParams());
        }
        setPage("units",scroll(body));
    }

    private void editUnit(final int number) {
        final DataStore.UnitInfo u = store.getUnit(number);
        if (u == null) return;
        LinearLayout form = vbox(4);
        form.setPadding(dp(14),dp(4),dp(14),dp(4));
        final EditText owner = edit("نام مالک",u.owner,InputType.TYPE_CLASS_TEXT);
        final EditText ownerPhone = edit("شماره موبایل مالک",u.ownerPhone,InputType.TYPE_CLASS_PHONE);
        final EditText tenant = edit("نام مستأجر",u.tenant,InputType.TYPE_CLASS_TEXT);
        final EditText tenantPhone = edit("شماره موبایل مستأجر",u.tenantPhone,InputType.TYPE_CLASS_PHONE);
        final EditText persons = edit("تعداد نفرات",String.valueOf(u.occupants),InputType.TYPE_CLASS_NUMBER);
        final CheckBox meter = new CheckBox(this);
        meter.setText("این واحد کنتور فرعی آب دارد");
        meter.setChecked(u.submeter);
        meter.setTypeface(font);
        meter.setTextColor(TEXT);
        final EditText last = edit("آخرین عدد کنتور فرعی",trim(u.lastWaterReading),InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final EditText notes = edit("توضیحات",u.notes,InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        form.addView(owner); form.addView(ownerPhone); form.addView(tenant); form.addView(tenantPhone);
        form.addView(persons); form.addView(meter); form.addView(last); form.addView(notes);

        new AlertDialog.Builder(this)
                .setTitle("پروفایل واحد " + fa(number))
                .setView(scroll(form))
                .setNegativeButton("انصراف",null)
                .setPositiveButton("ذخیره",new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d,int which) {
                        u.owner=s(owner); u.ownerPhone=s(ownerPhone); u.tenant=s(tenant); u.tenantPhone=s(tenantPhone);
                        u.occupants=Math.max(0,safeInt(s(persons),1));
                        u.submeter=meter.isChecked();
                        u.lastWaterReading=Math.max(0d,safeDouble(s(last),0d));
                        u.notes=s(notes);
                        store.updateUnit(u);
                        render();
                        toast("اطلاعات واحد ذخیره شد");
                    }
                }).show();
    }

    private void showFinance() {
        LinearLayout body = vbox(8);
        body.setPadding(dp(10),dp(12),dp(10),dp(16));
        body.addView(tv("مدیریت مالی",18,DARK,true));
        body.addView(menuButton("شارژ ماهانه","مبلغ شارژ و وضعیت پرداخت ۱۲ واحد",new View.OnClickListener(){@Override public void onClick(View v){showCharge();}}));
        body.addView(menuButton("قبض آب","محاسبه کنتورهای فرعی و تقسیم باقیمانده",new View.OnClickListener(){@Override public void onClick(View v){showWater();}}));
        body.addView(menuButton("هزینه‌های ساختمان","برق عمومی، نگهبان، نظافت، تعمیرات و ...",new View.OnClickListener(){@Override public void onClick(View v){showExpenses();}}));
        body.addView(menuButton("صندوق بلوک","موجودی، دریافتی‌ها و هزینه‌ها",new View.OnClickListener(){@Override public void onClick(View v){showFund();}}));
        body.addView(menuButton("تنظیم متن پیامک","متن یادآوری بدهی مالک یا مستأجر",new View.OnClickListener(){@Override public void onClick(View v){showSmsSettings();}}));
        setPage("finance",scroll(body));
    }

    private View menuButton(String title,String desc,View.OnClickListener listener) {
        LinearLayout r=hbox(); r.setGravity(Gravity.CENTER_VERTICAL); r.setPadding(dp(12),dp(10),dp(12),dp(10));
        r.setBackground(round(Color.WHITE,11,1,Color.rgb(224,207,185)));
        LinearLayout info=vbox(1); info.addView(tv(title,14,DARK,true)); info.addView(tv(desc,10,MUTED,false));
        r.addView(info,new LinearLayout.LayoutParams(0,-2,1f));
        TextView arrow=center("‹",28,BROWN,true); r.addView(arrow,lp(dp(34),dp(38)));
        r.setOnClickListener(listener);
        LinearLayout.LayoutParams p=rowParams(); r.setLayoutParams(p);
        return r;
    }

    private void showCharge() {
        final DataStore.MonthData md=store.getMonth(monthKey);
        LinearLayout body=vbox(6); body.setPadding(dp(10),dp(10),dp(10),dp(16));
        body.addView(pageTitle("شارژ " + PersianDate.monthLabel(monthKey)));
        final EditText amount=edit("مبلغ شارژ هر واحد (تومان)",md.chargeAmount>0?String.valueOf(md.chargeAmount):"",InputType.TYPE_CLASS_NUMBER);
        body.addView(amount);
        Button save=button("ذخیره مبلغ شارژ",Color.WHITE,BROWN,true);
        save.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            long x=safeLong(s(amount),-1); if(x<0){toast("مبلغ شارژ را صحیح وارد کنید");return;}
            store.setChargeAmount(monthKey,x); showCharge(); toast("مبلغ شارژ ذخیره شد");
        }});
        body.addView(save,buttonParams());
        body.addView(tv("وضعیت پرداخت واحدها",14,DARK,true));
        for(int n=1;n<=12;n++){
            final int unit=n;
            LinearLayout r=payRow("واحد "+fa(n),md.chargeAmount>0?money(md.chargeAmount):"ابتدا مبلغ شارژ را ثبت کنید");
            Switch sw=new Switch(this); sw.setChecked(store.isChargePaid(monthKey,n)); sw.setEnabled(md.chargeAmount>0); sw.setTypeface(font);
            sw.setOnCheckedChangeListener((b,checked)->{store.setChargePaid(monthKey,unit,checked);});
            r.addView(sw); body.addView(r,rowParams());
        }
        setPage("charge",scroll(body));
    }

    private void showWater() {
        final DataStore.MonthData md=store.getMonth(monthKey);
        final LinearLayout body=vbox(6); body.setPadding(dp(10),dp(10),dp(10),dp(16));
        body.addView(pageTitle("قبض آب " + PersianDate.monthLabel(monthKey)));
        body.addView(tv("مبلغ کل قبض و کارکرد کل کنتور اصلی را وارد کنید. سهم واحدهای دارای کنتور فرعی از روی مصرفشان حساب می‌شود و باقی قبض بین واحدهای بدون کنتور تقسیم می‌شود.",10,MUTED,false));
        final EditText bill=edit("مبلغ کل قبض آب (تومان)",md.waterBillAmount>0?String.valueOf(md.waterBillAmount):"",InputType.TYPE_CLASS_NUMBER);
        final EditText main=edit("کارکرد کل کنتور اصلی",md.mainWaterConsumption>0?trim(md.mainWaterConsumption):"",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        body.addView(bill); body.addView(main);

        final Map<Integer,EditText> prev=new HashMap<>();
        final Map<Integer,EditText> cur=new HashMap<>();
        boolean anyMeter=false;
        for(DataStore.UnitInfo u:store.getUnits()){
            if(!u.submeter) continue;
            anyMeter=true;
            TextView uh=tv("کنتور فرعی واحد "+fa(u.number),12,DARK,true); uh.setPadding(0,dp(6),0,0); body.addView(uh);
            LinearLayout rr=hbox();
            EditText p=edit("عدد قبلی",trim(store.savedPreviousReading(monthKey,u.number,u.lastWaterReading)),InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            EditText c=edit("عدد جدید",trim(store.savedCurrentReading(monthKey,u.number,u.lastWaterReading)),InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            rr.addView(p,fieldParams()); rr.addView(c,fieldParams()); body.addView(rr);
            prev.put(u.number,p); cur.put(u.number,c);
        }
        if(!anyMeter) body.addView(tv("در حال حاضر هیچ واحدی کنتور فرعی ندارد؛ کل قبض بین ۱۲ واحد تقسیم می‌شود.",10,BROWN,false));

        Button calc=button("محاسبه و ذخیره قبض",Color.WHITE,BROWN,true);
        calc.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            long total=safeLong(s(bill),-1); double cons=safeDouble(s(main),-1);
            Map<Integer,DataStore.Reading> readings=new HashMap<>();
            for(DataStore.UnitInfo u:store.getUnits()){
                if(!u.submeter) continue;
                double p=safeDouble(s(prev.get(u.number)),-1); double c=safeDouble(s(cur.get(u.number)),-1);
                readings.put(u.number,new DataStore.Reading(p,c));
            }
            try{
                store.saveWaterBill(monthKey,total,cons,readings);
                showWater(); toast("قبض آب محاسبه و ذخیره شد");
            }catch(Exception ex){ alert("خطا در محاسبه",ex.getMessage()==null?"اطلاعات واردشده را بررسی کنید":ex.getMessage()); }
        }});
        body.addView(calc,buttonParams());

        if(md.waterBillAmount>0){
            body.addView(tv("سهم و وضعیت پرداخت واحدها",14,DARK,true));
            for(int n=1;n<=12;n++){
                final int unit=n; long share=store.waterShare(monthKey,n);
                LinearLayout r=payRow("واحد "+fa(n),money(share));
                Switch sw=new Switch(this); sw.setChecked(store.isWaterPaid(monthKey,n)); sw.setTypeface(font);
                sw.setOnCheckedChangeListener((b,checked)->store.setWaterPaid(monthKey,unit,checked));
                r.addView(sw); body.addView(r,rowParams());
            }
        }
        setPage("water",scroll(body));
    }

    private void showExpenses() {
        LinearLayout body=vbox(6); body.setPadding(dp(10),dp(10),dp(10),dp(16));
        body.addView(pageTitle("هزینه‌های " + PersianDate.monthLabel(monthKey)));
        final EditText title=edit("عنوان هزینه؛ مثال: برق عمومی", "",InputType.TYPE_CLASS_TEXT);
        final EditText amount=edit("مبلغ (تومان)","",InputType.TYPE_CLASS_NUMBER);
        final EditText date=edit("تاریخ",PersianDate.todayKey(),InputType.TYPE_CLASS_TEXT);
        body.addView(title); body.addView(amount); body.addView(date);
        Button add=button("ثبت هزینه",Color.WHITE,BROWN,true);
        add.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            String t=s(title); long a=safeLong(s(amount),-1);
            if(TextUtils.isEmpty(t)||a<=0){toast("عنوان و مبلغ هزینه را وارد کنید");return;}
            store.addExpense(monthKey,t,a,s(date)); showExpenses(); toast("هزینه ثبت شد");
        }});
        body.addView(add,buttonParams());
        body.addView(tv("جمع هزینه ماه: "+money(store.monthExpenses(monthKey)),13,DARK,true));
        for(final DataStore.Expense e:store.expensesForMonth(monthKey)){
            LinearLayout r=hbox(); r.setGravity(Gravity.CENTER_VERTICAL); r.setPadding(dp(10),dp(8),dp(10),dp(8)); r.setBackground(round(Color.WHITE,10,1,Color.rgb(224,207,185)));
            LinearLayout info=vbox(1); info.addView(tv(e.title,12,TEXT,true)); info.addView(tv(e.date+" — "+money(e.amount),10,MUTED,false));
            r.addView(info,new LinearLayout.LayoutParams(0,-2,1f));
            Button del=button("حذف",RED,CREAM2,false); del.setOnClickListener(v->{
                new AlertDialog.Builder(this).setTitle("حذف هزینه").setMessage("این هزینه حذف شود؟").setNegativeButton("خیر",null).setPositiveButton("بله",(d,w)->{store.deleteExpense(e.id);showExpenses();}).show();
            });
            r.addView(del,lp(dp(60),dp(36))); body.addView(r,rowParams());
        }
        setPage("expenses",scroll(body));
    }

    private void showFund() {
        LinearLayout body=vbox(7); body.setPadding(dp(10),dp(10),dp(10),dp(16));
        body.addView(pageTitle("صندوق بلوک"));
        LinearLayout big=vbox(3); big.setGravity(Gravity.CENTER); big.setPadding(dp(10),dp(18),dp(10),dp(18)); big.setBackground(round(DARK,14,0,0));
        big.addView(center("موجودی فعلی صندوق",12,Color.WHITE,false));
        big.addView(center(money(store.fundBalance()),22,GOLD,true));
        body.addView(big,rowParams());
        body.addView(tv("کل دریافتی ثبت‌شده: "+money(store.totalIncome()),12,GREEN,false));
        body.addView(tv("کل هزینه ثبت‌شده: "+money(store.totalExpenses()),12,RED,false));
        final EditText initial=edit("موجودی اولیه صندوق",String.valueOf(store.getInitialFund()),InputType.TYPE_CLASS_NUMBER);
        body.addView(initial);
        Button save=button("ذخیره موجودی اولیه",Color.WHITE,BROWN,true);
        save.setOnClickListener(v->{long x=safeLong(s(initial),-1);if(x<0){toast("عدد صحیح وارد کنید");return;}store.setInitialFund(x);showFund();});
        body.addView(save,buttonParams());
        setPage("fund",scroll(body));
    }

    private void showSmsSettings() {
        LinearLayout body=vbox(7); body.setPadding(dp(10),dp(10),dp(10),dp(16));
        body.addView(pageTitle("تنظیم متن پیامک"));
        body.addView(tv("متغیرهای قابل استفاده: {نام}  {واحد}  {ماه}  {مبلغ}",10,MUTED,false));
        final EditText msg=edit("متن پیامک",store.getSmsTemplate(),InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        msg.setMinLines(5); body.addView(msg);
        Button save=button("ذخیره متن پیامک",Color.WHITE,BROWN,true);
        save.setOnClickListener(v->{if(TextUtils.isEmpty(s(msg))){toast("متن پیامک خالی است");return;}store.setSmsTemplate(s(msg));toast("متن پیامک ذخیره شد");});
        body.addView(save,buttonParams());
        setPage("sms",scroll(body));
    }

    private void showReports() {
        LinearLayout body=vbox(7); body.setPadding(dp(10),dp(10),dp(10),dp(16));
        body.addView(tv("گزارش مالی " + PersianDate.monthLabel(monthKey),18,DARK,true));
        report(body,"شارژ تعیین‌شده",store.monthChargeDue(monthKey));
        report(body,"شارژ وصول‌شده",store.monthChargePaid(monthKey));
        report(body,"آب تعیین‌شده",store.monthWaterDue(monthKey));
        report(body,"آب وصول‌شده",store.monthWaterPaid(monthKey));
        report(body,"هزینه‌های ماه",store.monthExpenses(monthKey));
        report(body,"مطالبات باقی‌مانده",store.monthDebt(monthKey));
        report(body,"موجودی صندوق",store.fundBalance());
        body.addView(tv("واحدهای بدهکار",14,DARK,true));
        boolean any=false;
        for(int n=1;n<=12;n++){
            long debt=store.unitDebt(monthKey,n);
            if(debt<=0) continue;
            any=true; final int unit=n;
            LinearLayout r=payRow("واحد "+fa(n),money(debt));
            Button sms=button("پیامک",Color.WHITE,BROWN,true); sms.setOnClickListener(v->sendUnitReminder(unit));
            r.addView(sms,lp(dp(66),dp(36))); body.addView(r,rowParams());
        }
        if(!any) body.addView(tv("برای این ماه بدهی ثبت‌شده‌ای وجود ندارد.",11,GREEN,false));
        setPage("reports",scroll(body));
    }

    private void report(LinearLayout body,String label,long value) {
        LinearLayout r=hbox(); r.setGravity(Gravity.CENTER_VERTICAL); r.setPadding(dp(10),dp(9),dp(10),dp(9)); r.setBackground(round(Color.WHITE,9,1,Color.rgb(225,208,187)));
        r.addView(tv(label,11,TEXT,false),new LinearLayout.LayoutParams(0,-2,1f));
        r.addView(tv(money(value),12,DARK,true)); body.addView(r,rowParams());
    }

    private void sendUnitReminder(int number) {
        DataStore.UnitInfo u=store.getUnit(number); if(u==null)return;
        long debt=store.unitDebt(monthKey,number); if(debt<=0){toast("برای این واحد بدهی ثبت نشده است");return;}
        final List<String> phones=new ArrayList<>(); final List<String> names=new ArrayList<>(); final List<String> labels=new ArrayList<>();
        if(!TextUtils.isEmpty(u.ownerPhone)){phones.add(u.ownerPhone);names.add(TextUtils.isEmpty(u.owner)?"مالک محترم":u.owner);labels.add("مالک: "+(TextUtils.isEmpty(u.owner)?u.ownerPhone:u.owner));}
        if(!TextUtils.isEmpty(u.tenantPhone)){phones.add(u.tenantPhone);names.add(TextUtils.isEmpty(u.tenant)?"ساکن محترم":u.tenant);labels.add("مستأجر: "+(TextUtils.isEmpty(u.tenant)?u.tenantPhone:u.tenant));}
        if(phones.isEmpty()){toast("شماره موبایل مالک یا مستأجر ثبت نشده است");return;}
        if(phones.size()==1){openSms(phones.get(0),reminder(names.get(0),number,debt));return;}
        new AlertDialog.Builder(this).setTitle("ارسال پیامک به").setItems(labels.toArray(new String[0]),(d,w)->openSms(phones.get(w),reminder(names.get(w),number,debt))).show();
    }

    private String reminder(String name,int unit,long debt) {
        return store.getSmsTemplate().replace("{نام}",name)
                .replace("{واحد}",fa(unit)).replace("{ماه}",PersianDate.monthLabel(monthKey))
                .replace("{مبلغ}",PersianDate.toFaDigits(format(debt)));
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

    private TextView pageTitle(String title) {
        TextView t=tv(title,18,DARK,true); t.setPadding(0,0,0,dp(4)); return t;
    }

    private LinearLayout payRow(String title,String desc) {
        LinearLayout r=hbox(); r.setGravity(Gravity.CENTER_VERTICAL); r.setPadding(dp(10),dp(8),dp(10),dp(8)); r.setBackground(round(Color.WHITE,10,1,Color.rgb(224,207,185)));
        LinearLayout info=vbox(1); info.addView(tv(title,12,TEXT,true)); info.addView(tv(desc,10,MUTED,false));
        r.addView(info,new LinearLayout.LayoutParams(0,-2,1f)); return r;
    }

    private TextView tv(String value,float size,int color,boolean isBold) {
        TextView t=new TextView(this); t.setText(value==null?"":value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(isBold?bold:font); t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); t.setTextDirection(View.TEXT_DIRECTION_RTL); return t;
    }

    private TextView center(String value,float size,int color,boolean isBold) {
        TextView t=tv(value,size,color,isBold); t.setGravity(Gravity.CENTER); t.setMaxLines(1); t.setEllipsize(TextUtils.TruncateAt.END); return t;
    }

    private EditText edit(String hint,String value,int type) {
        EditText e=new EditText(this); e.setHint(hint); e.setText(value==null?"":value); e.setTextSize(12); e.setTextColor(TEXT); e.setHintTextColor(Color.rgb(145,130,116));
        e.setTypeface(font); e.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); e.setTextDirection(View.TEXT_DIRECTION_RTL); e.setInputType(type);
        e.setPadding(dp(10),dp(8),dp(10),dp(8)); e.setBackground(round(Color.WHITE,9,1,Color.rgb(216,198,176)));
        e.setLayoutParams(rowParams()); return e;
    }

    private Button button(String label,int color,int bg,boolean isBold) {
        Button b=new Button(this); b.setText(label); b.setTextColor(color); b.setTextSize(12); b.setAllCaps(false); b.setTypeface(isBold?bold:font); b.setGravity(Gravity.CENTER);
        if(bg==Color.TRANSPARENT)b.setBackgroundColor(Color.TRANSPARENT);else b.setBackground(round(bg,9,0,0)); return b;
    }

    private LinearLayout vbox(int ignored) {
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l;
    }

    private LinearLayout hbox() {
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l;
    }

    private ScrollView scroll(View v) {
        ScrollView s=new ScrollView(this); s.setFillViewport(true); s.setBackgroundColor(CREAM); s.addView(v,new ScrollView.LayoutParams(-1,-2)); return s;
    }

    private GradientDrawable round(int color,int radiusDp,int strokeDp,int strokeColor) {
        GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radiusDp)); if(strokeDp>0)g.setStroke(dp(strokeDp),strokeColor); return g;
    }

    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private LinearLayout.LayoutParams rowParams(){LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,dp(4),0,dp(4));return p;}
    private LinearLayout.LayoutParams buttonParams(){LinearLayout.LayoutParams p=lp(-1,dp(46));p.setMargins(0,dp(5),0,dp(8));return p;}
    private LinearLayout.LayoutParams fieldParams(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1f);p.setMargins(dp(3),0,dp(3),0);return p;}

    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private String s(EditText e){return e==null?"":e.getText().toString().trim();}
    private int safeInt(String x,int fallback){try{return Integer.parseInt(PersianDate.normalizeDigits(x).replace(",","").trim());}catch(Exception e){return fallback;}}
    private long safeLong(String x,long fallback){try{return Long.parseLong(PersianDate.normalizeDigits(x).replace(",","").replace("٬","").replace(" ","").trim());}catch(Exception e){return fallback;}}
    private double safeDouble(String x,double fallback){try{return Double.parseDouble(PersianDate.normalizeDigits(x).replace("٫",".").replace(",",".").replace(" ","").trim());}catch(Exception e){return fallback;}}
    private String trim(double x){if(Math.abs(x-Math.rint(x))<0.000001)return String.valueOf((long)Math.rint(x));return String.format(Locale.US,"%.2f",x).replaceAll("0+$","").replaceAll("\\.$","");}
    private String format(long x){return new DecimalFormat("#,###").format(x);}
    private String money(long x){return PersianDate.toFaDigits(format(x))+" تومان";}
    private String fa(int x){return PersianDate.toFaDigits(String.valueOf(x));}
    private void toast(String x){Toast.makeText(this,x,Toast.LENGTH_SHORT).show();}
    private void alert(String title,String msg){new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("باشه",null).show();}
}