
package com.android.seandroid_manager;

import android.preference.PreferenceFragment;
import android.os.Bundle;

public class SELinuxDisabledFragment extends PreferenceFragment {

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.selinux_not_enabled);
    }
}