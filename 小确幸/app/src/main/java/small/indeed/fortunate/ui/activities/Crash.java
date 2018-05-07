package small.indeed.fortunate.ui.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import small.indeed.fortunate.*;
import small.indeed.fortunate.utils.*;
import android.support.v7.app.*;

public class Crash extends AppCompatActivity {

    private static final String EXTRA_E = "e";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);

    public static void start(Context context, Throwable e) {
        Intent intent = new Intent(context, Crash.class);
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TOP |
            Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_E, e);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash);
        setTitle("Error");
        TextView message = (TextView) findViewById(R.id.crash_message);

        Throwable e = (Throwable) getIntent().getSerializableExtra(EXTRA_E);

        String env =
            "Runtime Enviorment Inormation\n\n" +
            "CrashTime = " + dateFormat.format(new Date()) + "\n" +
            "Model = " + Build.MODEL + "\n" +
            "Android = " + Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")\n" +
            "Brand = " + Build.BRAND + "\n" +
            "Manufacturer = " + Build.MANUFACTURER + "\n" +
            "Board = " + Build.BOARD + "\n" +
            "Hardware = " + Build.HARDWARE + "\n" +
            "Device = " + Build.DEVICE + "\n" +
            "Version = " + getVersionName() + " (" + getVersionCode() + ")\n" +
            "SupportAbis = " + getSupportAbis() + "\n" +
            "Display = " + Build.DISPLAY + "\n\n\n";
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String stack = "Force Close Crash Log\n\n" + writer.toString();
        final String msg = env + stack;

        message.setText(msg);
        message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
					BrowserUtil.copyURL(Crash.this, msg);
					ToastUtil.show(Crash.this, R.string.app_name);
                    return true;
                }
            });
    }

    private String getVersionName() {
        String packageName = getPackageName();
        String versionName = "";
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            versionName = packageInfo.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private int getVersionCode() {
        String packageName = getPackageName();
        int versionCode = 0;
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            versionCode = packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private String getSupportAbis() {
        String[] abis = Build.SUPPORTED_ABIS;
        StringBuilder abi = new StringBuilder();
        for (int i = 0; i < abis.length; i++) {
            if (i == 0) {
                abi.append(abis[i]);
            } else {
                abi.append(" & ").append(abis[i]);
            }
        }
        return abi.toString();
    }

}

