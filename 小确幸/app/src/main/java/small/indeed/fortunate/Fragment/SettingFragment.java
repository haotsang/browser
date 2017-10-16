package small.indeed.fortunate.Fragment;


import android.content.*;
import android.os.*;
import android.preference.*;
import small.indeed.fortunate.*;

public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private ListPreference volumeControl;
	private String[] vcEntries;
	
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
        String summary;
		
		vcEntries = getResources().getStringArray(R.array.setting_entries_volume_control);
        summary = vcEntries[Integer.valueOf(sp.getString(getString(R.string.sp_volume), "1"))];
        volumeControl = (ListPreference) findPreference(getString(R.string.sp_volume));
        volumeControl.setSummary(summary);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(getString(R.string.sp_volume))) {
            String summary = vcEntries[Integer.valueOf(sp.getString(key, "1"))];
            volumeControl.setSummary(summary);
        }
    }
	
}


