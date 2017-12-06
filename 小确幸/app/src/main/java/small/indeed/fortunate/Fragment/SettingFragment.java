package small.indeed.fortunate.Fragment;

import android.content.*;
import android.os.*;
import android.preference.*;
import small.indeed.fortunate.*;

public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_settings);
    }

    @Override
    public void onResume() {
        super.onResume();
		//getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
    }

}
