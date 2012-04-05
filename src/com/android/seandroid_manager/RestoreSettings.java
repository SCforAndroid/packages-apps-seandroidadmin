package com.android.seandroid_manager;

import android.content.ContentResolver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SELinux;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;

public class RestoreSettings extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SELinux.isSELinuxEnabled()) {
            Log.d("SEAndroidManager", "Restoring the SELinux settings");

            ContentResolver mContentResolver = context.getContentResolver();

            // Restore SELinux enforcing mode
            final String enforcing = Settings.Secure.getString(mContentResolver,
                                                               Settings.Secure.SELINUX_ENFORCING);
            if (enforcing != null) {
                SELinux.setSELinuxEnforce(enforcing.contentEquals("1") ? true : false);
            }

            // Restore SELinux boolean settings
            final String booleanList = Settings.Secure.getString(mContentResolver,
                                                                 Settings.Secure.SELINUX_BOOLEANS);
            if (booleanList != null) {
                HashMap<String, Boolean>  mBooleanValueMap = new HashMap<String, Boolean>();
                String[] booleanValues = booleanList.split(",");
                for (String value : booleanValues) {
                    final int delimiter = value.indexOf(':');
                    if (delimiter > 0) {
                        String n = value.substring(0, delimiter);
                        Boolean v = value.substring(delimiter + 1).contentEquals("1") ? true : false;
                        mBooleanValueMap.put(n, v);
                    }
                }
                String[] mNames = SELinux.getBooleanNames();
                for (String n : mNames) {
                    if (mBooleanValueMap.containsKey(n)) {
                        SELinux.setBooleanValue(n, mBooleanValueMap.get(n));
                    }
                }
            }
        }
    }
}
