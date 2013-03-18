/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.seandroid_admin;

import android.app.admin.DevicePolicyManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class MMACFragment extends SEAndroidAdminFragment
        implements OnPreferenceChangeListener {
    public static final String TAG = "MMACfragment";

    private static final String KEY_MMAC_ENFORCING = "key_mmac_enforcing";
    private static final String KEY_MMAC_RELOAD = "key_mmac_reload";
    private static final String KEY_MMAC_RESTORE = "key_mmac_restore";

    private static final String MMAC_ENFORCE_PROPERTY = "persist.mmac.enforce";
    private static final String MMAC_POLICY_FILE = "mac_permissions.xml";

    private CheckBoxPreference mMMACenforceCheckbox;
    private Preference mMMACreload;
    private Preference mMMACrestore;

    private File mMMACpolicyFile = null;

    private String mMMACenforceCheckboxSummaryChecked;
    private String mMMACenforceCheckboxSummaryUnchecked;
    private String mMMACenforceCheckboxSummaryDisabled;

    private TextView mEmptyView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdmin.updateMMACstate();

        addPreferencesFromResource(R.xml.mmac_fragment);

        mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
        getListView().setEmptyView(mEmptyView);

        if (!mAdmin.isDeviceAdmin) {
            addMessagePreference("not device admin");

        } else if (!mAdmin.isMMACadmin) {
            addMessagePreference("not mmac admin");

        } else {
            File extFileDir = mActivity.getExternalFilesDir(null);
            if (extFileDir != null) {
                mMMACpolicyFile = new File(extFileDir, MMAC_POLICY_FILE);
            }

            mMMACenforceCheckbox =
                    (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_MMAC_ENFORCING);
            mMMACenforceCheckbox.setOnPreferenceChangeListener(this);
            mMMACenforceCheckboxSummaryChecked =
                    getString(R.string.mmac_enforcing_cb_summaryChecked);
            mMMACenforceCheckboxSummaryUnchecked =
                    getString(R.string.mmac_enforcing_cb_summaryUnchecked);
            mMMACenforceCheckboxSummaryDisabled =
                    getString(R.string.mmac_enforcing_cb_summaryDisabled);

            mMMACreload =
                    getPreferenceScreen().findPreference(KEY_MMAC_RELOAD);
            mMMACreload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Reload of MMAC policy requested");
                    try {
                        byte[] policy = Files.toByteArray(mMMACpolicyFile);
                        if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                                DevicePolicyManager.MMAC_POLICY_FILE, policy)) {
                            Toast.makeText(mActivity, "Unable to set policy", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mActivity, "Success", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException ioex) {
                        Log.e(TAG, "Exception ocurred", ioex);
                        Toast.makeText(mActivity, ioex.toString(), Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            mMMACrestore =
                    getPreferenceScreen().findPreference(KEY_MMAC_RESTORE);
            mMMACrestore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Delete custom MMAC policy requested");
                    if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                            DevicePolicyManager.MMAC_POLICY_FILE, null)) {
                        Toast.makeText(mActivity, "Unable to remove custom policy", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, "Success", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onResume() {
        // XXX Unfortunately, both super.onResume and updateViews will update
        // the Admin state to the same thing.
        super.onResume();
        updateViews();
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (super.onPreferenceChange(preference, newValue)) {
            return true;
        }
        
        if (preference == mMMACenforceCheckbox) {
            boolean value = (Boolean) newValue;
            boolean ret = mAdmin.mDPM.setMMACenforcing(mAdmin.mDeviceAdmin, value);
            mAdmin.updateMMACstate();
            updateViews();
            return ret;
        }
        
        return false;
    }
    
    private void updateViews() {
        if (TRACE_UPDATE) { Log.v(TAG, "UPDATE updateViews()"); }
        
        if (mAdmin.isMMACadmin) {
            mMMACenforceCheckbox.setEnabled(true);
            boolean systemState = SystemProperties.getBoolean(MMAC_ENFORCE_PROPERTY, false);
            mMMACenforceCheckbox.setChecked(systemState);
            if (systemState) {
                mMMACenforceCheckbox.setSummary(mMMACenforceCheckboxSummaryChecked);
            } else {
                mMMACenforceCheckbox.setSummary(mMMACenforceCheckboxSummaryUnchecked);
            }

            mMMACreload.setEnabled(true);
            if (mMMACpolicyFile != null) {
                mMMACreload.setSummary(mMMACpolicyFile.getPath());
            } else {
                mMMACreload.setSummary(R.string.ext_storage_unavail);
            }

            mMMACrestore.setEnabled(true);
        }
    }

    private void addMessagePreference(int messageId) {
        if (mEmptyView != null) mEmptyView.setText(messageId);
        getPreferenceScreen().removeAll();
    }

    private void addMessagePreference(String message) {
        if (mEmptyView != null) mEmptyView.setText(message);
        getPreferenceScreen().removeAll();
    }
}
