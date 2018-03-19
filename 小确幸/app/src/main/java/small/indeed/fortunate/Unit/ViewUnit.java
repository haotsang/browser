package small.indeed.fortunate.Unit;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.v4.content.*;
import android.support.v4.graphics.*;
import android.support.v7.graphics.*;
import android.util.*;

public class ViewUnit {
	
	public static boolean isColorLight(int color) {
        float[] hsl = new float[3];
        ColorUtils.RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), hsl);
        return hsl[2] > 0.5f;
    }
	
	public static int getColor(Context context, Bitmap bitmap, boolean incognito) {
        Palette palette = Palette.from(bitmap).generate();
        return incognito ? palette.getMutedColor(ContextCompat.getColor(context, 2131296333)) : palette.getVibrantColor(ContextCompat.getColor(context, 2131296330));
    }
	
	public static float dp2px(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(id, null);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

	public static int getWindowHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getWindowWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

}
