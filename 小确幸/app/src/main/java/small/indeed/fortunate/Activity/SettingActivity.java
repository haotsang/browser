package small.indeed.fortunate.Activity;

import android.os.*;
import android.support.v7.app.*;
import small.indeed.fortunate.Fragment.*;

public class SettingActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
    }
}
