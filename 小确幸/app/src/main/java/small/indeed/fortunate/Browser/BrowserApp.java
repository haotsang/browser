package small.indeed.fortunate.Browser;

import android.app.*;
import android.content.*;

public class BrowserApp extends Application {
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

	public static Context getAppContext() {
		return context;
	}
}

