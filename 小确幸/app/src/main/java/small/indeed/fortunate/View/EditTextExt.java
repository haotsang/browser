package small.indeed.fortunate.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

public class EditTextExt extends AppCompatEditText {
    private int mPositionX;

    public EditTextExt(Context context) {
        super(context);
    }

    public EditTextExt(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextExt(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private static LinearGradient getGradient(float widthEnd, float fadeStart, float stopStart, float stopEnd, int color) {
        return new LinearGradient(0.0f, 0.0f, widthEnd, 0.0f, new int[]{color, 0, color, color, 0}, new float[]{0.0f, fadeStart, stopStart, stopEnd, 1.0f}, TileMode.CLAMP);
    }

    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        this.mPositionX = x;
        requestLayout();
    }

    public void onDraw(Canvas canvas) {
        float lineWidth = getLayout().getLineWidth(0);
        float width = (float) getMeasuredWidth();
        if (getText() == null || getText().length() == 0 || lineWidth <= width) {
            super.onDraw(canvas);
            getPaint().setShader(null);
            return;
        }
        int textColor = getCurrentTextColor();
        float widthEnd = width + ((float) this.mPositionX);
        float percent = (float) ((int) (((double) width) * 0.2d));
        float fadeStart = ((float) this.mPositionX) / widthEnd;
        float stopStart = this.mPositionX > 0 ? (((float) this.mPositionX) + percent) / widthEnd : 0.0f;
        if (lineWidth <= widthEnd) {
            percent = 0.0f;
        }
        getPaint().setShader(getGradient(widthEnd, fadeStart, stopStart, (widthEnd - percent) / widthEnd, textColor));
        super.onDraw(canvas);
    }

    public void onFocusChanged(boolean gainFocus, int direction, Rect prevFocusedRect) {
        super.onFocusChanged(gainFocus, direction, prevFocusedRect);
        if (!gainFocus) {
            setSelection(0);
        }
    }
}
