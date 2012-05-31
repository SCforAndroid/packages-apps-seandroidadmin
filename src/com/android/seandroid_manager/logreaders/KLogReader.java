package com.android.seandroid_manager.logreaders;

import com.android.seandroid_manager.KLogCtl;
import com.android.seandroid_manager.LogCallback;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

public class KLogReader extends Thread {
    
    LogCallback mCallback;
    String mSearch;
    
    public KLogReader(LogCallback handler, String search) {
        super();
        mCallback = handler;
        mSearch = search;
    }
    
    private void parseLogs() {
        int value;
        byte logs[];
        String line;
        
        mCallback.onStart();
        
        try {
            value = KLogCtl.kLogCtl(10, null, 0);
            logs = new byte[value];
            value = KLogCtl.kLogCtl(3, logs, value);

            Scanner stream = new Scanner(new ByteArrayInputStream(logs));

            while (stream.hasNextLine() && !interrupted()) {
                line = stream.nextLine();
                if (line.contains(mSearch)) {
                    mCallback.onEvent(line);
                }
            }
            stream.close();
        } catch (Exception e) {
            mCallback.onException(e);
        } finally {
            mCallback.onFinish();
        }
    }
    
    @Override
    public void run() {
        parseLogs();
    }
    
}
