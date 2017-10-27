package small.indeed.fortunate.Unit;

import android.content.*;

public class IntentUnit
{
	public static final int REQUEST_FILE_16 = 0x101;
    public static final int REQUEST_FILE_21 = 0x102;
	public static final String INTENT_TYPE_TEXT_PLAIN = "text/plain";
	
	public static void share(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(INTENT_TYPE_TEXT_PLAIN);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(intent, url));
    }
}
