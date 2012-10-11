package com.android.seandroid_admin;

import android.app.admin.DevicePolicyManager;
import android.os.Bundle;
import android.os.SELinux;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.seandroid_admin.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

//TODO may want to rename to reflect real functionality
public class SELinuxEnforcingFragment extends SEAndroidAdminFragment
        implements OnPreferenceChangeListener {
    public static final String TAG = "SELinuxFragment";

    private static final String KEY_SELINUX_ENFORCING = "key_selinux_enforcing";

    private static final String KEY_SELINUX_RELOAD = "key_selinux_reload";
    private static final String KEY_SELINUX_RESTORE = "key_selinux_restore";
    private static final String KEY_PROPERTYCONTEXTS_RELOAD = "key_propertycontexts_reload";
    private static final String KEY_PROPERTYCONTEXTS_RESTORE = "key_propertycontexts_restore";
    private static final String KEY_FILECONTEXTS_RELOAD = "key_filecontexts_reload";
    private static final String KEY_FILECONTEXTS_RESTORE = "key_filecontexts_restore";
    private static final String KEY_SEAPPCONTEXTS_RELOAD = "key_seappcontexts_reload";
    private static final String KEY_SEAPPCONTEXTS_RESTORE = "key_seappcontexts_restore";

    private static final String SELINUX_POLICY_FILE = "sepolicy";
    private static final String PROPERTY_CONTEXTS_FILE = "property_contexts";
    private static final String FILE_CONTEXTS_FILE = "file_contexts";
    private static final String SEAPP_CONTEXTS_FILE = "seapp_contexts";

    private CheckBoxPreference mSELinuxEnforceCheckbox;
    private Preference mSELinuxReload, mSELinuxRestore;
    private Preference mPropertyContextsReload, mPropertyContextsRestore;
    private Preference mFileContextsReload, mFileContextsRestore;
    private Preference mSEAppContextsReload, mSEAppContextsRestore;

    private File mSELinuxPolicyFile = null;
    private File mPropertyContextsPolicyFile = null;
    private File mFileContextsPolicyFile = null;
    private File mSEAppContextsPolicyFile = null;

    private String mSELinuxEnforceCheckboxSummaryChecked;
    private String mSELinuxEnforceCheckboxSummaryUnchecked;
    private String mSELinuxEnforceCheckboxSummaryDisabled;

    private TextView mEmptyView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdmin.updateSELinuxState();

        addPreferencesFromResource(R.xml.selinux_enforcing_fragment);

        mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
        getListView().setEmptyView(mEmptyView);

        if (!SELinux.isSELinuxEnabled()) {
            addMessagePreference(R.string.selinuxBooleans_err_selinuxDisabled);

        } else if (!mAdmin.isDeviceAdmin) {
            addMessagePreference(R.string.selinuxBooleans_err_notDeviceAdmin);

        } else if (!mAdmin.isSELinuxAdmin) {
            addMessagePreference(R.string.selinuxBooleans_err_notSELinuxAdmin);

        } else {
            File extFileDir = mActivity.getExternalFilesDir(null);
            if (extFileDir != null) {
                mSELinuxPolicyFile = new File(extFileDir, SELINUX_POLICY_FILE);
                mPropertyContextsPolicyFile = new File(extFileDir, PROPERTY_CONTEXTS_FILE);
                mFileContextsPolicyFile = new File(extFileDir, FILE_CONTEXTS_FILE);
                mSEAppContextsPolicyFile = new File(extFileDir, SEAPP_CONTEXTS_FILE);
            }

            mSELinuxEnforceCheckbox =
                    (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SELINUX_ENFORCING);
            mSELinuxEnforceCheckbox.setOnPreferenceChangeListener(this);
            mSELinuxEnforceCheckboxSummaryChecked =
                    getString(R.string.selinux_enforcing_cb_summaryChecked);
            mSELinuxEnforceCheckboxSummaryUnchecked =
                    getString(R.string.selinux_enforcing_cb_summaryUnchecked);
            mSELinuxEnforceCheckboxSummaryDisabled =
                    getString(R.string.selinux_enforcing_cb_summaryDisabled);

            List<String> boolnames = mAdmin.mDPM.getSELinuxBooleanNames(mAdmin.mDeviceAdmin);
            Log.v(TAG, "SELinux booleans: " + boolnames);
            if (boolnames != null) {
                Collections.sort(boolnames);
                for (final String name : boolnames) {
                    //XXX To do small text, will need to define own xml layout
                    CheckBoxPreference pref = new CheckBoxPreference(mActivity);
                    pref.setTitle(name);
                    pref.setChecked(mAdmin.mDPM.getSELinuxBooleanValue(mAdmin.mDeviceAdmin, name));
                    pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            boolean value = (Boolean) newValue;
                            boolean ret = mAdmin.mDPM.setSELinuxBooleanValue(mAdmin.mDeviceAdmin, name, value);
                            Toast.makeText(mActivity,
                                    ret ? "Success" : "Unable to set boolean " + name,
                                            Toast.LENGTH_SHORT).show();
                            return ret;
                        }
                    });
                    getPreferenceScreen().addPreference(pref);
                }
            }
            //TODO Figure out how to handle failure of getSELinuxBooleanNames
            //TODO Figure out how to add boolean prefs to the Booleans
            //     PreferenceCategory, not just append to end

            /* Warning, lots of duplicated code coming */

            mSELinuxReload =
                    getPreferenceScreen().findPreference(KEY_SELINUX_RELOAD);
            mSELinuxReload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Reload of SELinux policy requested");
                    try {
                        byte[] policy = FileUtils.readFileToByteArray(mSELinuxPolicyFile);
                        if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                                DevicePolicyManager.SELINUX_POLICY_FILE, policy)) {
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

            mSELinuxRestore =
                    getPreferenceScreen().findPreference(KEY_SELINUX_RESTORE);
            mSELinuxRestore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Delete custom SELinux policy requested");
                    if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                            DevicePolicyManager.SELINUX_POLICY_FILE, null)) {
                        Toast.makeText(mActivity, "Unable to remove custom policy", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, "Success", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            mPropertyContextsReload =
                    getPreferenceScreen().findPreference(KEY_PROPERTYCONTEXTS_RELOAD);
            mPropertyContextsReload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Reload of Property Contexts policy requested");
                    try {
                        byte[] policy = FileUtils.readFileToByteArray(mPropertyContextsPolicyFile);
                        if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                                DevicePolicyManager.PROPERTY_CONTEXTS_FILE, policy)) {
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

            mPropertyContextsRestore =
                    getPreferenceScreen().findPreference(KEY_PROPERTYCONTEXTS_RESTORE);
            mPropertyContextsRestore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Delete custom Property Contexts policy requested");
                    if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                            DevicePolicyManager.PROPERTY_CONTEXTS_FILE, null)) {
                        Toast.makeText(mActivity, "Unable to remove custom policy", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, "Success", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            mFileContextsReload =
                    getPreferenceScreen().findPreference(KEY_FILECONTEXTS_RELOAD);
            mFileContextsReload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Reload of File Contexts policy requested");
                    try {
                        byte[] policy = FileUtils.readFileToByteArray(mFileContextsPolicyFile);
                        if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                                DevicePolicyManager.FILE_CONTEXTS_FILE, policy)) {
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

            mFileContextsRestore =
                    getPreferenceScreen().findPreference(KEY_FILECONTEXTS_RESTORE);
            mFileContextsRestore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Delete custom File Contexts policy requested");
                    if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                            DevicePolicyManager.FILE_CONTEXTS_FILE, null)) {
                        Toast.makeText(mActivity, "Unable to remove custom policy", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, "Success", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });

            mSEAppContextsReload =
                    getPreferenceScreen().findPreference(KEY_SEAPPCONTEXTS_RELOAD);
            mSEAppContextsReload.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Reload of SEApp Contexts policy requested");
                    try {
                        byte[] policy = FileUtils.readFileToByteArray(mSEAppContextsPolicyFile);
                        if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                                DevicePolicyManager.SEAPP_CONTEXTS_FILE, policy)) {
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

            mSEAppContextsRestore =
                    getPreferenceScreen().findPreference(KEY_SEAPPCONTEXTS_RESTORE);
            mSEAppContextsRestore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.v(TAG, "Delete custom SEApp Contexts policy requested");
                    if (!mAdmin.mDPM.setCustomPolicyFile(mAdmin.mDeviceAdmin,
                            DevicePolicyManager.SEAPP_CONTEXTS_FILE, null)) {
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

        if (preference == mSELinuxEnforceCheckbox) {
            boolean value = (Boolean) newValue;
            boolean ret = mAdmin.mDPM.setSELinuxEnforcing(mAdmin.mDeviceAdmin, value);
            // TODO show toast on error
            mAdmin.updateSELinuxState();
            updateViews();
            return ret;
        }

        return false;
    }

    private void updateViews() {
        if (TRACE_UPDATE) { Log.v(TAG, "UPDATE updateViews()"); }

        if (mAdmin.isSELinuxAdmin) {
            mSELinuxEnforceCheckbox.setEnabled(true);
            mSELinuxEnforceCheckbox.setChecked(mAdmin.isEnforcingSELinux);
            if (mAdmin.isEnforcingSELinux) {
                mSELinuxEnforceCheckbox.setSummary(mSELinuxEnforceCheckboxSummaryChecked);
            } else {
                mSELinuxEnforceCheckbox.setSummary(mSELinuxEnforceCheckboxSummaryUnchecked);
            }

            mSELinuxReload.setEnabled(true);
            if (mSELinuxPolicyFile != null) {
                mSELinuxReload.setSummary(mSELinuxPolicyFile.getPath());
            } else {
                mSELinuxReload.setSummary(R.string.ext_storage_unavail);
            }
            mSELinuxRestore.setEnabled(true);

            mPropertyContextsReload.setEnabled(true);
            if (mPropertyContextsPolicyFile != null) {
                mPropertyContextsReload.setSummary(mPropertyContextsPolicyFile.getPath());
            } else {
                mPropertyContextsReload.setSummary(R.string.ext_storage_unavail);
            }
            mPropertyContextsRestore.setEnabled(true);

            mFileContextsReload.setEnabled(true);
            if (mFileContextsPolicyFile != null) {
                mFileContextsReload.setSummary(mFileContextsPolicyFile.getPath());
            } else {
                mFileContextsReload.setSummary(R.string.ext_storage_unavail);
            }
            mFileContextsRestore.setEnabled(true);

            mSEAppContextsReload.setEnabled(true);
            if (mSEAppContextsPolicyFile != null) {
                mSEAppContextsReload.setSummary(mSEAppContextsPolicyFile.getPath());
            } else {
                mSEAppContextsReload.setSummary(R.string.ext_storage_unavail);
            }
            mSEAppContextsRestore.setEnabled(true);
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
