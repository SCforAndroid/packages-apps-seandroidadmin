
package com.android.seandroid_manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SELinux;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.provider.Settings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class SEAndroidManager extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (SELinux.isSELinuxEnabled()) {
            loadHeadersFromResource(R.xml.enabled_headers, target);
        } else {
            loadHeadersFromResource(R.xml.disabled_headers, target);
        }
    }

    public static class SELinuxDisabledFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.selinux_not_enabled);
        }
    }

    public static class AVCDeniedReaderFragment extends Fragment {

        private TextView logs;
        private ScrollView scrollView;
        private ProgressDialog progressDialog;
        private Handler handler;
        private Activity activity;

        private String defaultText;
        private String replacement;
        private String regExp;
        private String fileExtension;
        private String fileOnSave;
        private String logPrefix;
        private String emptyLogMessage;
        private String dialogInfo;
        private String dialogError;
        private String logsKey;
        private String okButtonText;

        private final static int DIALOG_ERROR = 0;
        private final static int DIALOG_INFO = 1;

        private AVCCallback handleMessage = new AVCCallback() {

            @Override
            public void onStart() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.show();
                    }
                });
            }

            @Override
            public void onEvent(final String logMessage) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        logs.append(logMessage + "\n\n");
                    }
                });
            }

            @Override
            public void onFinish() {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        progressDialog.cancel();
                    }
                });
            }

            @Override
            public void onException(Exception e) {
                showDialog(e.getMessage(), DIALOG_ERROR);
            }

        };

        private OnClickListener onAVCRefreshClick = new OnClickListener() {

            @Override
            public void onClick(View v) {
                logs.setText(null);
                AVCReader logReader = new AVCReader(handleMessage);
                logReader.start();
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            View v = inflater.inflate(R.layout.avc_denied_reader, container, false);

            Button b = (Button) v.findViewById(R.id.refreshButton);
            b.setOnClickListener(onAVCRefreshClick);

            logs = (TextView) v.findViewById(R.id.avcLogTextView);

            scrollView = (ScrollView) v.findViewById(R.id.avcLogScrollView);

            handler = new Handler();

            activity = getActivity();

            defaultText = getString(R.string.avc_denied_log_reload_msg);
            replacement = getString(R.string.avc_denied_timestamp_replacement_char);
            fileExtension = getString(R.string.avc_denied_log_file_extension);
            regExp = getString(R.string.avc_denied_timestamp_format_regex);
            fileOnSave = getString(R.string.avc_denied_log_onsave_dialog_message);
            logPrefix = getString(R.string.avc_denied_log_filename);
            dialogInfo = getString(R.string.avc_dialog_title_info);
            dialogError = getString(R.string.avc_dialog_title_error);
            logsKey = getString(R.string.avc_logs_key);
            emptyLogMessage = getString(R.string.avc_denied_log_empty_log_msg);
            okButtonText = getString(R.string.avc_ok_button_text);

            progressDialog = new ProgressDialog(activity);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.avc_progress_bar_message));

            if (savedInstanceState != null) {
                CharSequence oldLogs = savedInstanceState.getCharSequence(logsKey);
                if (oldLogs != null) {
                    logs.setText(oldLogs);
                }
            }

            setHasOptionsMenu(true);
            return v;
        }

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            savedInstanceState.putCharSequence(logsKey, logs.getText());
            super.onSaveInstanceState(savedInstanceState);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.layout.avc_denied_options, menu);
            return;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            int id = item.getItemId();

            switch (id) {

                case R.id.fileSave:

                    int length = logs.getText().length();
                    if (length != defaultText.length() &&
                            !defaultText.equals(logs.getText())) {

                        String fileName = logPrefix + replacement +
                                DateFormat.getDateTimeInstance().format(new Date()).toLowerCase() +
                                fileExtension;

                        // See res/strings.xml for details on regExp formatting
                        fileName = fileName.replaceAll(regExp, replacement);
                        saveLogs(fileName);
                    }
                    else if (length == 0) {
                        showDialog(emptyLogMessage, DIALOG_INFO);
                    }
                    else {
                        showDialog(defaultText, DIALOG_ERROR);
                    }
                    break;
                default:
                    return super.onOptionsItemSelected(item);
            }
            return true;
        }

        /**
         * Displays a pop up ERROR dialog. Safe to call from non gui thread.
         *
         * @param message
         * @param type The type of message to be displayed, see DIALOG_ERROR and
         *            friends.
         */
        private void showDialog(final String message, final int type) {

            handler.post(new Runnable() {

                @Override
                public void run() {

                    String title = (type == DIALOG_INFO) ? dialogInfo : dialogError;

                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle(title);
                    alertDialog.setMessage(message);

                    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, okButtonText,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // intentionally left empty
                                }
                            });

                    // alertDialog
                    alertDialog.show();
                    return;
                }
            });
        }

        /**
         * Saves the AVC denied message logs to disk. This function runs on a
         * separate thread.
         *
         * @param filename The filename to save the logs as
         */
        private void saveLogs(final String filename) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        final File file =
                                new File(Environment.getExternalStorageDirectory(), filename);

                        FileOutputStream fos = new FileOutputStream(file);
                        CharSequence c = logs.getText();
                        fos.write(c.toString().getBytes());
                        fos.flush();
                        fos.close();
                        showDialog(fileOnSave + filename, DIALOG_INFO);

                    }
                    catch (FileNotFoundException e) {

                        showDialog(e.toString(), DIALOG_ERROR);
                    }
                    catch (IOException e) {

                        showDialog(e.toString(), DIALOG_ERROR);
                    }
                }
            }).run();
        }
    }

    public static class SELinuxEnforcingFragment extends PreferenceFragment {

        private static final String KEY_SELINUX_ENFORCING = "selinux_enforcing";
        private CheckBoxPreference mSELinuxToggleEnforce;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.selinux_enforcing_fragment);

            mSELinuxToggleEnforce = (CheckBoxPreference) getPreferenceScreen().findPreference(
                    KEY_SELINUX_ENFORCING);
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

        private void saveEnforcing()
        {
            String enforcing = SELinux.isSELinuxEnforced() ? "1" : "0";
            Settings.Secure.putString(getActivity().getContentResolver(),
                                      Settings.Secure.SELINUX_ENFORCING,
                                      enforcing);
        }
    }

    public static class SELinuxBooleanFragment extends ListFragment {

        private SharedPreferences mPrefs;
        private myBooleanAdapter mAdapter;

        private class myBooleanAdapter extends ArrayAdapter<String> {
            private final LayoutInflater mInflater;
            private String[] mBooleans;

            public myBooleanAdapter(Context context, int textViewResourceId, String[] items) {
                super(context, textViewResourceId, items);
                mInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                            saveBooleans();
                        }
                    });
                return convertView;
            }

            private class ViewHolder {
                TextView tx;
                CheckBox cb;
            }

            private void saveBooleans()
            {
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
                Settings.Secure.putString(getActivity().getContentResolver(),
                                          Settings.Secure.SELINUX_BOOLEANS,
                                          newBooleanList.toString());
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setEmptyText(getActivity().getText(R.string.selinux_no_booleans));
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
