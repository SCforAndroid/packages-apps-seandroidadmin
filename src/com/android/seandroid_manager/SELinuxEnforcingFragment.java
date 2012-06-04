  
package com.android.seandroid_manager;

import android.os.Bundle;
import android.os.SELinux;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.CheckBox;

public class SELinuxEnforcingFragment extends PreferenceFragment {

    private static final String KEY_SELINUX_ENFORCING = "selinux_enforcing";

    private CheckBoxPreference mSELinuxToggleEnforce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.selinux_enforcing_fragment);
        
        mSELinuxToggleEnforce =
            (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SELINUX_ENFORCING);

        mSELinuxToggleEnforce.setChecked(SELinux.isSELinuxEnforced());
        
    }

    @Override
	public void onResume() {
        super.onResume();
        
        if (mSELinuxToggleEnforce != null) {
            mSELinuxToggleEnforce.setChecked(SELinux.isSELinuxEnforced());
        }
    }

    @Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        final String key = preference.getKey();

        if (preference == mSELinuxToggleEnforce) {
            SELinux.setSELinuxEnforce(!SELinux.isSELinuxEnforced());
            mSELinuxToggleEnforce.setChecked(SELinux.isSELinuxEnforced());
            saveEnforcing();
        }
        return true;
    }
    
    private void saveEnforcing() {
        String enforcing = SELinux.isSELinuxEnforced() ? "1" : "0";
        Settings.Secure.putString(getActivity().getContentResolver(),
                                  Settings.Secure.SELINUX_ENFORCING,
                                  enforcing);
    }
}
