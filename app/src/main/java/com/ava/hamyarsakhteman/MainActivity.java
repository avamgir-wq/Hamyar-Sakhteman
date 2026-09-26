package com.ava.hamyarsakhteman;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    LinearLayout root;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDashboard();
    }

    void showDashboard(){
        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20,20,20,20);
        root.setBackgroundColor(Color.rgb(10,30,60));

        TextView title=new TextView(this);
        title.setText("🏢 همیار ساختمان\nبلوک A1 مجتمع فرهیختگان");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        String[][] floors={{"۱۰","۱۱","۱۲"},{"۷","۸","۹"},{"۴","۵","۶"},{"۱","۲","۳"}};
        for(String[] floor:floors){
            LinearLayout row=new LinearLayout(this);
            for(String u:floor){
                Button b=new Button(this);
                b.setText("واحد "+u+"\nشارژ -\nآب -");
                b.setOnClickListener(v->openUnit(u));
                row.addView(b);
            }
            root.addView(row);
        }
        setContentView(root);
    }

    void openUnit(String unit){
        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(25,25,25,25);
        page.setBackgroundColor(Color.rgb(10,30,60));
        TextView t=new TextView(this);
        t.setText("پروفایل واحد "+unit);
        t.setTextColor(Color.WHITE);
        t.setTextSize(22);
        page.addView(t);
        page.addView(field("نام مالک"));
        page.addView(field("شماره تماس"));
        page.addView(field("شماره پارکینگ"));
        page.addView(field("شماره انباری"));
        page.addView(field("کنتور فرعی آب"));
        Button save=new Button(this);
        save.setText("ذخیره اطلاعات");
        page.addView(save);
        setContentView(page);
    }

    EditText field(String hint){
        EditText e=new EditText(this);
        e.setHint(hint);
        return e;
    }
}
