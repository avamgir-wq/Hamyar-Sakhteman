package com.ava.hamyarsakhteman;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20,20,20,20);
        root.setBackgroundColor(Color.rgb(10,30,60));

        TextView title = new TextView(this);
        title.setText("🏢 همیار ساختمان\nبلوک A1 مجتمع فرهیختگان");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView month = new TextView(this);
        month.setText("ماه انتخابی: مهر");
        month.setTextColor(Color.rgb(220,180,70));
        month.setGravity(Gravity.CENTER);
        root.addView(month);

        String[][] floors={{"۱۰","۱۱","۱۲"},{"۷","۸","۹"},{"۴","۵","۶"},{"۱","۲","۳"}};
        for(String[] floor:floors){
            LinearLayout row=new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            for(String u:floor){
                Button b=new Button(this);
                b.setText("واحد "+u+"\nشارژ -\nآب -");
                b.setOnClickListener(v -> openUnit(u));
                row.addView(b);
            }
            root.addView(row);
        }
        setContentView(root);
    }

    private void openUnit(String unit){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(20,20,20,20);
        TextView info=new TextView(this);
        info.setText("پروفایل واحد "+unit+"\n\nمالک:\nتلفن:\nپارکینگ:\nانباری:\nکنتور فرعی آب:");
        info.setTextColor(Color.WHITE);
        info.setTextSize(18);
        box.addView(info);
        setContentView(box);
    }
}
