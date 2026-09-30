package ir.avamedia.hamyarbuilding.a1;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public final class ReportImageUtil {
    public static final class Row {
        public final String label;
        public final String value;
        public final int tone;

        public Row(String label, String value) {
            this(label, value, 0);
        }

        public Row(String label, String value, int tone) {
            this.label = label == null ? "" : label;
            this.value = value == null ? "" : value;
            this.tone = tone;
        }
    }

    private ReportImageUtil() {}

    public static Bitmap create(Context context, String title, String subtitle, List<Row> rows) {
        final int width = 1080;
        final int header = 300;
        final int rowHeight = 94;
        final int footer = 120;
        final int height = Math.max(900, header + rows.size() * rowHeight + footer);

        int cream = Color.rgb(248,242,230);
        int dark = Color.rgb(53,32,22);
        int brown = Color.rgb(112,72,43);
        int gold = Color.rgb(197,153,72);
        int muted = Color.rgb(116,104,94);
        int green = Color.rgb(45,122,72);
        int red = Color.rgb(176,66,58);
        int orange = Color.rgb(194,126,44);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawColor(cream);

        Typeface regular = Typeface.DEFAULT;
        Typeface bold = Typeface.DEFAULT_BOLD;
        try { regular = Typeface.createFromAsset(context.getAssets(), "fonts/shabnam.ttf"); } catch (Throwable ignored) {}
        try { bold = Typeface.createFromAsset(context.getAssets(), "fonts/shabnam_bold.ttf"); } catch (Throwable ignored) {}

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(bold);
        p.setColor(dark);
        c.drawRect(0,0,width,header,p);

        p.setTextAlign(Paint.Align.RIGHT);
        p.setTextSize(54);
        p.setColor(Color.WHITE);
        c.drawText(title, width-64, 105, p);

        p.setTypeface(regular);
        p.setTextSize(30);
        p.setColor(gold);
        c.drawText(subtitle, width-64, 164, p);

        p.setTextSize(26);
        p.setColor(Color.rgb(239,226,205));
        c.drawText("همیار ساختمان | بلوک A1 مجتمع فرهیختگان", width-64, 225, p);

        p.setColor(gold);
        c.drawRoundRect(new RectF(64,250,width-64,258),4,4,p);

        int y = header + 34;
        for (int i=0;i<rows.size();i++) {
            Row row=rows.get(i);
            p.setColor(Color.WHITE);
            c.drawRoundRect(new RectF(48,y,width-48,y+72),22,22,p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);
            p.setColor(Color.rgb(224,207,185));
            c.drawRoundRect(new RectF(48,y,width-48,y+72),22,22,p);
            p.setStyle(Paint.Style.FILL);

            p.setTypeface(regular);
            p.setTextSize(25);
            p.setTextAlign(Paint.Align.RIGHT);
            p.setColor(muted);
            c.drawText(row.label,width-78,y+46,p);

            p.setTypeface(bold);
            p.setTextSize(26);
            p.setTextAlign(Paint.Align.LEFT);
            int valueColor = dark;
            if (row.tone == 1) valueColor = green;
            else if (row.tone == 2) valueColor = red;
            else if (row.tone == 3) valueColor = orange;
            else if (row.tone == 4) valueColor = brown;
            p.setColor(valueColor);
            c.drawText(row.value,78,y+46,p);

            y += rowHeight;
        }

        p.setTypeface(regular);
        p.setTextSize(23);
        p.setColor(muted);
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText("گزارش تولیدشده با اپ همیار ساختمان",width/2f,height-52,p);

        return bitmap;
    }

    public static Uri cacheForShare(Context context, Bitmap bitmap, String fileName) throws Exception {
        File dir = new File(context.getCacheDir(), "reports");
        if (!dir.exists() && !dir.mkdirs()) throw new Exception("پوشه گزارش ساخته نشد");
        File file = new File(dir, safe(fileName) + ".png");
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public static Uri saveToGallery(Context context, Bitmap bitmap, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, safe(fileName) + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HamyarA1");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new Exception("فایل تصویر ساخته نشد");
            OutputStream out = context.getContentResolver().openOutputStream(uri);
            if (out == null) throw new Exception("فایل تصویر باز نشد");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            context.getContentResolver().update(uri, values, null, null);
            return uri;
        }

        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "HamyarA1");
        if (!dir.exists() && !dir.mkdirs()) throw new Exception("پوشه تصویر ساخته نشد");
        File file = new File(dir, safe(fileName) + ".png");
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();
        return Uri.fromFile(file);
    }

    private static String safe(String input) {
        if (input == null || input.trim().isEmpty()) return "report";
        return input.replaceAll("[^A-Za-z0-9_-]+","-");
    }
}
