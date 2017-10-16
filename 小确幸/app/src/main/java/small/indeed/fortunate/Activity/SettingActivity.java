package small.indeed.fortunate.Activity;

import android.app.*;
import android.os.*;
import small.indeed.fortunate.Fragment.*;

public class SettingActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
    }
	
}


