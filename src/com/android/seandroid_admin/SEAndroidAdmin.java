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

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Keeps track of the state of the Device Admin system.
 */
public class SEAndroidAdmin {
    public static final String TAG = "SEAdmin";
    public static final boolean TRACE_UPDATE = false; // traces some of the update method call chains

    public static final int REQUEST_CODE_ENABLE_ADMIN = 1;

    // XXX Maybe this should be split out to do GUI-like stuff for callbacks.
    public static class myDeviceAdminReceiver extends DeviceAdminReceiver { }

    final SEAndroidAdminActivity mActivity;
    final DevicePolicyManager mDPM;
    final ComponentName mDeviceAdmin;

    /** True if we are an active device administrator */
    boolean isDeviceAdmin;

    /** True if we are a SELinux admin */
    boolean isSELinuxAdmin;
    /** True if we are enforcing SELinux policy */
    boolean isEnforcingSELinux;

    /** True if we are a MMAC admin */
    boolean isMMACadmin;
    /** True if we are enforcing MMAC policy */
    boolean isEnforcingMMAC;

    SEAndroidAdmin(SEAndroidAdminActivity activity) {
        mActivity = activity;
        mDPM = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(activity, myDeviceAdminReceiver.class);
        updateState();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(TAG+'@'+Integer.toHexString(super.hashCode())+"={");
        sb.append("isDeviceAdmin : "+isDeviceAdmin+", ");
        sb.append("isSELinuxAdmin : "+isSELinuxAdmin+", ");
        sb.append("isEnforcingSELinux : "+isEnforcingSELinux+", ");
        sb.append("isMMACadmin : "+isMMACadmin+", ");
        sb.append("isEnforcingMMAC : "+isEnforcingMMAC+", ");
        sb.delete(sb.length()-2, sb.length()); //delete comma
        sb.append('}');
        return sb.toString();
    }

    /**
     * Be very careful about using this. This only exists because removeActiveAdmin()
     * defers removal of this admin from the admins list until later and returns
     * almost immediately. So this code could execute before the list removal is executed,
     * resulting in isActiveAdmin() returning true. To mitigate this, we set mAdminActive
     * to the value we want and proceed with updating state as normal.
     */
    void setAdminState(boolean value) {
        if (TRACE_UPDATE) { Log.v(TAG, "UPDATE setAdminState(value="+value+")"); }
        boolean old = isDeviceAdmin;
        isDeviceAdmin = value;
        if (old != isDeviceAdmin) { Log.v(TAG, "mAdminActive: " + old + " -> " + isDeviceAdmin); }
        updateSELinuxState();
        updateMMACstate();
    }

    void updateState() {
        updateDeviceAdminState();
        updateSELinuxState();
        updateMMACstate();
    }

    /** Updates Device Admin state with current values */
    void updateDeviceAdminState() {
        if (TRACE_UPDATE) { Log.v(TAG, "UPDATE updateDeviceAdminState()"); }
        boolean old = isDeviceAdmin;
        isDeviceAdmin = mDPM.isAdminActive(mDeviceAdmin);
        if (old != isDeviceAdmin) { Log.v(TAG, "mAdminActive: " + old + " -> " + isDeviceAdmin); }
    }

    /** Updates SELinux state with current device values */
    void updateSELinuxState() {
        if (TRACE_UPDATE) { Log.v(TAG, "UPDATE updateSELinuxState()"); }

        boolean old;

        // Device Admin necessary for SELinux Admin
        old = isSELinuxAdmin;
        isSELinuxAdmin = isDeviceAdmin && mDPM.isSELinuxAdmin(mDeviceAdmin);
        if (old != isSELinuxAdmin) { Log.v(TAG, "mSELinuxAdmin: " + old + " -> " + isSELinuxAdmin); }

        // SELinux Admin necessary for Enforcing SELinux Policy
        old = isEnforcingSELinux;
        isEnforcingSELinux = isSELinuxAdmin && mDPM.getSELinuxEnforcing(mDeviceAdmin);
        if (old != isEnforcingSELinux) { Log.v(TAG, "mEnforcingSELinux: " + old + " -> " + isEnforcingSELinux); }        
    }

    /** Updates MMAC state with current device values */
    void updateMMACstate() {
        if (TRACE_UPDATE) { Log.v(TAG, "UPDATE updateMMACState()"); }

        boolean old;

        // Device Admin necessary for MMAC Admin
        old = isMMACadmin;
        isMMACadmin = isDeviceAdmin && mDPM.isMMACadmin(mDeviceAdmin);
        if (old != isMMACadmin) { Log.v(TAG, "mMMACadmin: " + old + " -> " + isMMACadmin); }

        // MMAC Admin necessary for Enforcing MMAC Policy
        old = isEnforcingMMAC;
        isEnforcingMMAC = isMMACadmin && mDPM.getMMACenforcing(mDeviceAdmin);
        if (old != isEnforcingMMAC) { Log.v(TAG, "mEnforceMMAC: " + old + " -> " + isEnforcingMMAC); }
    }

    /** Asks user to enable SEAdmin as a Device Admin */
    void enableAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        mActivity.startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }

    void removeAdmin() {
        mDPM.removeActiveAdmin(mDeviceAdmin);
        setAdminState(false);   // removeActiveAdmin returns immediately but the
        updateSELinuxState();   // removal is delayed, so set state manually
        updateMMACstate();
    }
}
