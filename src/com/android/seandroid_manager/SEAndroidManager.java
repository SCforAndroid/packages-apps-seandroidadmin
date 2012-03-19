package com.android.seandroid_manager;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SELinux;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class SEAndroidManager extends PreferenceActivity {
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.seandroid_manager_headers, target);
    }

    public static class SELinuxEnforcingFragment extends PreferenceFragment {

        private static final String KEY_SELINUX_ENFORCING = "selinux_enforcing";

        private CheckBoxPreference mSELinuxToggleEnforce;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.selinux_enforcing_fragment);
            mSELinuxToggleEnforce = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SELINUX_ENFORCING);
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
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            final String key = preference.getKey();
            if (preference == mSELinuxToggleEnforce) {
                SELinux.setSELinuxEnforce(!SELinux.isSELinuxEnforced());
                mSELinuxToggleEnforce.setChecked(SELinux.isSELinuxEnforced());
            }
            return true;
        }
    }

    public static class SELinuxBooleanFragment extends ListFragment {

        private static final String PREF_FILE = "seandroid_settings";
        private SharedPreferences mPrefs;
        private myBooleanAdapter mAdapter;

        private class myBooleanAdapter extends ArrayAdapter<String> {
            private final LayoutInflater mInflater;
            private String[] mBooleans;

            public myBooleanAdapter(Context context, int textViewResourceId, String[] items) {
                super(context, textViewResourceId, items);
                mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mBooleans = items;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ViewHolder holder;
                if (convertView  == null) {
                    convertView = mInflater.inflate(R.layout.selinux_manage_booleans_item, parent, 
                                                    false);
                    holder = new ViewHolder();
                    holder.tx = (TextView) convertView.findViewById(R.id.text);
                    holder.cb = (CheckBox) convertView.findViewById(R.id.checkbox);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                String name = mBooleans[position];
                holder.tx.setText(name);
                holder.cb.setChecked(SELinux.getBooleanValue(name));
                holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            String name = (String)holder.tx.getText();
                            SELinux.setBooleanValue(name, isChecked);
                            holder.cb.setChecked(SELinux.getBooleanValue(name));
                        }
                    });
                return convertView;
            }

            class ViewHolder {
                TextView tx;
                CheckBox cb;
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAdapter = new myBooleanAdapter(getActivity(), R.layout.selinux_manage_booleans, 
                                            SELinux.getBooleanNames());
            setListAdapter(mAdapter);
        }
    }
}
