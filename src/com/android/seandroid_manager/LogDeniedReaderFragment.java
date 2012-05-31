   
package com.android.seandroid_manager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.seandroid_manager.logreaders.KLogReader;
import com.android.seandroid_manager.logreaders.LogcatReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class LogDeniedReaderFragment extends Fragment {

    private TextView mMessageLog;
    private TextView mDefaultMessage;
    private ScrollView mScrollView;
    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    private Activity mActivity;
    private ActionBar mActionBar;
    
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

    private LogCallback handleMessage = new LogCallback() {

        @Override
        public void onStart() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.show();
                }
            });
        }

        @Override
        public void onEvent(final String logMessage) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mMessageLog.append(logMessage + "\n\n");
                }
            });
        }
            
        @Override
        public void onFinish() {
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    mProgressDialog.cancel();
                }
           });
        }

        @Override
        public void onException(Exception e) {
            showDialog(e.getMessage(), DIALOG_ERROR);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                   Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.log_denied_reader, container, false);

        mMessageLog = (TextView) v.findViewById(R.id.avcLogTextView);
        mDefaultMessage = (TextView) v.findViewById(R.id.avcLogTextViewI);
        
        mScrollView = (ScrollView) v.findViewById(R.id.avcLogScrollView);
        
        mHandler = new Handler();
        
        mActivity = getActivity();
        
        mActionBar = mActivity.getActionBar();
        
        defaultText = getString(R.string.avc_denied_log_reload_msg);
        replacement = getString(R.string.avc_denied_timestamp_replacement_char);
        fileExtension = getString(R.string.avc_denied_log_file_extension);
        regExp = getString(R.string.avc_denied_timestamp_format_regex);
        fileOnSave = getString(R.string.avc_denied_log_onsave_dialog_message);
        dialogInfo = getString(R.string.avc_dialog_title_info);
        dialogError = getString(R.string.avc_dialog_title_error);
        emptyLogMessage = getString(R.string.avc_denied_log_empty_log_msg);
        okButtonText = getString(R.string.avc_ok_button_text);
        
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.avc_progress_bar_message));
        
        if (savedInstanceState != null) {
            CharSequence oldLogs = savedInstanceState.getCharSequence(logsKey);
            String title = savedInstanceState.getString("title");
            
            if (title != null) {
                mActionBar.setTitle(title);
            }
            
            if (oldLogs != null && !oldLogs.toString().equals("")) {
                mDefaultMessage.setVisibility(View.GONE);
                mMessageLog.setText(oldLogs);
            } else {
                mDefaultMessage.setVisibility(View.VISIBLE);
            }
        }
        
        setHasOptionsMenu(true);
        return v;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putCharSequence(logsKey, mMessageLog.getText());
        savedInstanceState.putString("title", mActionBar.getTitle().toString());
        super.onSaveInstanceState(savedInstanceState);
    }
    
    /**
     * Displays a pop up ERROR dialog. Safe to call from non gui thread.
     *
     * @param message
     * @param type The type of message to be displayed, see DIALOG_ERROR and
     *            friends.
     */
    private void showDialog(final String message, final int type) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                String title = (type == DIALOG_INFO) ? dialogInfo : dialogError;

                AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
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
     * Saves the message logs to disk. This function runs on a
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
                    CharSequence c = mMessageLog.getText();
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

    /**
     * Create the action bar items
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Handle when one of the action bar items is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.saveButton:
                // save the log to the sdcard
                int length = mMessageLog.getText().length();
                if (length != 0) {
                    
                    String fileName = logPrefix + replacement +
                        DateFormat.getDateTimeInstance().format(new Date()).toLowerCase() +
                        fileExtension;

                    // See res/strings.xml for details on regExp formatting
                    fileName = fileName.replaceAll(regExp, replacement);
                    saveLogs(fileName);
                } else if (length == 0) {
                    showDialog(emptyLogMessage, DIALOG_INFO);
                }
                else {
                    showDialog(defaultText, DIALOG_ERROR);
                }
                return true;

            case R.id.AVC:
                // display all the avc denials
                mActionBar.setTitle(R.string.avc_title);
                mDefaultMessage.setVisibility(View.GONE);
                logPrefix = getString(R.string.avc_denied_log_filename);
                mMessageLog.setText(null);
                KLogReader logReader = new KLogReader(handleMessage, "avc");
                logReader.start();
                return true;
                
            case R.id.clear:
                // just clear the screen
                mActionBar.setTitle(R.string.avc_denied_log_fragment_title);
                mDefaultMessage.setVisibility(View.VISIBLE);
                mMessageLog.setText(null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
