package com.falcon.balav.eatmonster.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public  class FoodCut {

    public static Bitmap EatFood(Bitmap srcBitmap, int State) {
        switch (State) {
            case 0:
                return cutRightTop (srcBitmap);
            case 1:
                return cutLeftTop (srcBitmap);
            case 2:
                return cutLeftBottom (srcBitmap);
            case 3:
                return cutRightBottom (srcBitmap);
        }
        return srcBitmap;
    }

    private static Bitmap cutRightTop(Bitmap srcBitmap) {
        return cutArcImage (srcBitmap, 0F, 270F);
    }

    private static Bitmap cutLeftTop(Bitmap srcBitmap) {
        return cutArcImage (srcBitmap, 0F, 180F);
    }

    private static Bitmap cutLeftBottom(Bitmap srcBitmap) {
        return cutArcImage (srcBitmap, 90F, 90F);
    }

    private static Bitmap cutRightBottom(Bitmap srcBitmap) {
        return cutArcImage (srcBitmap, 0F, 0F);
    }


    private static Bitmap cutArcImage(Bitmap bitmap, float startAngle, float sweepAngle) {
        Bitmap output = Bitmap.createBitmap (bitmap.getWidth (), bitmap.getHeight (), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (output);
        final Paint paint = new Paint ();
        final Rect rect = new Rect (0, 0, bitmap.getWidth (), bitmap.getHeight ());
        Log.v ("getArcBitmap", "Bitmap-W:" + bitmap.getWidth () + ",H:" + bitmap.getHeight ());
        paint.setAntiAlias (true);
        canvas.drawARGB (0, 0, 0, 0);
        int W = bitmap.getWidth () / 2;
        int H = bitmap.getHeight () / 2;
        float radius = 500;
        final RectF oval = new RectF ();
        oval.set (W - radius, H - radius, W + radius, H + radius);
        canvas.drawArc (oval, startAngle, sweepAngle, true, paint);
        paint.setXfermode (new PorterDuffXfermode (PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap (bitmap, rect, rect, paint);
        return output;
    }
}
