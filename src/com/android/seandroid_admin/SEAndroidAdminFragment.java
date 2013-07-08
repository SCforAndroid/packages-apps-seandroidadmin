package com.android.seandroid_admin;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

/**
 * Common fragment code for DevicePolicyManager access. Provides two shared elements:
 *   1. Provides instance variables to access activity/context, DevicePolicyManager, etc.
 *   2. You can also place data shared by multiple Fragments in SEAdmin here.
 */
public class SEAndroidAdminFragment extends PreferenceFragment implements
        OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String TAG = "SEAdminFragment";
    protected static final boolean TRACE_LIFECYCLE = SEAndroidAdminActivity.TRACE_LIFECYCLE;
    protected static final boolean TRACE_UPDATE = SEAndroidAdminActivity.TRACE_UPDATE;

    protected SEAndroidAdminActivity mActivity;
    protected SEAndroidAdmin mAdmin;

    @Override
    public void onAttach(Activity activity) {
        if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE FRAGMENT onAttach()"); }
        super.onAttach(activity);
        mActivity = (SEAndroidAdminActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE FRAGMENT onCreate()"); }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE FRAGMENT onActivityCreated()"); }
        super.onActivityCreated(savedInstanceState);

        mAdmin = mActivity.mAdmin;
    }

    @Override
    public void onStart() {
        if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE FRAGMENT onStart()"); }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (TRACE_LIFECYCLE) { Log.v(TAG, "LIFECYCLE FRAGMENT onResume()"); }
        super.onResume();

        mAdmin.updateState();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

}
