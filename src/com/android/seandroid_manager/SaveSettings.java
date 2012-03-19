package com.android.seandroid_manager;

import android.content.ContentResolver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SELinux;
import android.provider.Settings;
import android.util.Log;

public class SaveSettings extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SELinux.isSELinuxEnabled()) {
            Log.d("SEAndroidManager", "Saving the SELinux settings");

            ContentResolver mContentResolver = context.getContentResolver();

            // Save SELinux enforcing mode
            String enforcing = SELinux.isSELinuxEnforced() ? "1" : "0";
            Settings.Secure.putString(mContentResolver, Settings.Secure.SELINUX_ENFORCING, 
                                      enforcing);

            // Save SELinux boolean settings
            String[] mNames = SELinux.getBooleanNames();
            StringBuilder newBooleanList = new StringBuilder();
            boolean first = true;
            for (String n : mNames) {
                if (first) {
                    first = false;
                } else {
                    newBooleanList.append(",");
                } 
                newBooleanList.append(n);
                newBooleanList.append(":");
                newBooleanList.append(SELinux.getBooleanValue(n) ? 1 : 0);
            }
            Settings.Secure.putString(mContentResolver, Settings.Secure.SELINUX_BOOLEANS, 
                                      newBooleanList.toString());
        }
    }
}
