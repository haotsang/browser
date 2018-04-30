package small.indeed.fortunate.View;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import small.indeed.fortunate.*;

public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        //重写dialog默认的主题
        this(context, R.style.full_dialog_background);
    }

    public CustomDialog(Context context, int themeResId) {
        super(context, themeResId);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setGravity(Gravity.BOTTOM); //显示在底部

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth(); //设置dialog的宽度为当前手机屏幕的宽度
        getWindow().setAttributes(p);
    }
}
