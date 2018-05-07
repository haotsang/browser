package small.indeed.fortunate;

import android.app.Application;
import android.content.Context;
import small.indeed.fortunate.ui.activities.*;


public class BrowserApp extends Application implements Thread.UncaughtExceptionHandler {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = this;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Crash.start(this, e);
        System.exit(1);
    }

}

