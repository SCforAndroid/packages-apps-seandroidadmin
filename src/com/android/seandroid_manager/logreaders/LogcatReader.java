package com.android.seandroid_manager.logreaders;

import com.android.seandroid_manager.LogCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogcatReader extends Thread {
    
    static final String DEFAULT_ARGS = " -v raw -d -s ";
    
    final LogCallback mCallback;
    final String mBuffer;
    final String mFilterspec;
    final String mSearch;
    
    /**
     * Retrieve logs using logcat.
     * 
     * See {@link http://developer.android.com/guide/developing/tools/logcat.html}
     * for details.
     * @param handler
     * @param buffer buffer to load, can be "main", "event", "radio", or
     * "system"
     * @param filterspec
     * @param search string to search for
     */
    public LogcatReader(LogCallback handler, String buffer,
            String filterspec, String search) {
        super();
        mCallback = handler;
        mBuffer = buffer;
        mFilterspec = filterspec;
        mSearch = search;
    }
    
    private void parseLogs() {
        
        mCallback.onStart();
        
        Process p = null;
        BufferedReader br = null;
        try {
            //XXX Convert to use ProcessBuilder. See
            // http://developer.android.com/reference/java/lang/Process.html
            p = Runtime.getRuntime().exec("logcat" + DEFAULT_ARGS
                    + " -b " + mBuffer
                    + " " + mFilterspec);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = br.readLine()) != null && !interrupted()) {
                if (line.contains(mSearch)) {
                    mCallback.onEvent(line);
                }
            }
            br.close();
        } catch (IOException e) {
            mCallback.onException(e);
        } finally {
            if (p != null) {
                p.destroy();
            }
            mCallback.onFinish();
        }
    }
    
    @Override
    public void run() {
        parseLogs();
    }

}
