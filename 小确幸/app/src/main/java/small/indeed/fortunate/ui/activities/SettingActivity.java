package small.indeed.fortunate.ui.activities;

import android.os.*;
import android.support.v7.app.*;
import small.indeed.fortunate.ui.preferences.*;


public class SettingActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
    }
}
