  
package com.android.seandroid_manager;

import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SELinux;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import android.util.Log;

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
