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
        root.setPadding(24,24,24,24);
        root.setBackgroundColor(Color.rgb(10,30,60));

        TextView title = new TextView(this);
        title.setText("همیار ساختمان\nبلوک A1 مجتمع فرهیختگان");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        String[] units = {"۱","۲","۳","۴","۵","۶","۷","۸","۹","۱۰","۱۱","۱۲"};
        for(String unit: units){
            Button button = new Button(this);
            button.setText("واحد " + unit);
            root.addView(button);
        }

        setContentView(root);
    }
}
