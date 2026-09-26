package com.ava.hamyarsakhteman;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
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
        for(int f=0;f<4;f++){
            LinearLayout row=new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            for(String u:floors[f]){
                Button b=new Button(this);
                b.setText("واحد "+u+"\nشارژ -\nآب -");
                row.addView(b);
            }
            root.addView(row);
        }
        setContentView(root);
    }
}
