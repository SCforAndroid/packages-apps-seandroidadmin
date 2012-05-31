  
package com.android.seandroid_manager;

import android.os.Bundle;
import android.os.SELinux;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.CheckBox;

public class SELinuxEnforcingFragment extends PreferenceFragment {

    private static final String KEY_SELINUX_ENFORCING = "selinux_enforcing";
    private static final String KEY_MAC_ENFORCING = "mac_enforcing";
    private static final String MAC_SYSTEM_PROPERTY = "persist.mac_enforcing_mode";

    private CheckBoxPreference mSELinuxToggleEnforce;
    private CheckBoxPreference mMACToggleEnforce;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.selinux_enforcing_fragment);
        
        mSELinuxToggleEnforce =
            (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SELINUX_ENFORCING);

        mSELinuxToggleEnforce.setChecked(SELinux.isSELinuxEnforced());

        mMACToggleEnforce =
            (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_MAC_ENFORCING);
        
        mMACToggleEnforce.setChecked(getMacEnforcingMode());
    }

    @Override
	public void onResume() {
        super.onResume();
        
        if (mSELinuxToggleEnforce != null) {
            mSELinuxToggleEnforce.setChecked(SELinux.isSELinuxEnforced());
        }

        if (mMACToggleEnforce != null) {
            mMACToggleEnforce.setChecked(getMacEnforcingMode());
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
        } else if (preference == mMACToggleEnforce) {
            boolean enforce = getMacEnforcingMode();
            mMACToggleEnforce.setChecked(!enforce);
            SystemProperties.set(MAC_SYSTEM_PROPERTY, (enforce ? "0" : "1"));
        }
        return true;
    }

    // return true if in Enforcing mode, false otherwise
    private boolean getMacEnforcingMode() {
        return SystemProperties.getBoolean(MAC_SYSTEM_PROPERTY, false);
    }
    
    private void saveEnforcing() {
        String enforcing = SELinux.isSELinuxEnforced() ? "1" : "0";
        Settings.Secure.putString(getActivity().getContentResolver(),
                                  Settings.Secure.SELINUX_ENFORCING,
                                  enforcing);
    }
}
