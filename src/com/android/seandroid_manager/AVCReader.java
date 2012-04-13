
package com.android.seandroid_manager;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

/**
 * Reads the kernel message log and sends AVCDenied messages to the registered
 * handler.
 */
public class AVCReader extends Thread {

    private String avcSearchTag = "avc";
    private AVCCallback callback = null;

    public AVCReader(AVCCallback avcHandler) {
        super();
        callback = avcHandler;
    }

    /**
     * Reads from kernel logs, parses them and calls the callbacks set by
     * AVCReader(AVCCallback avcHandler) for each avc denied message
     * encountered.
     */
    private void parseLogs() {

        int value;
        byte logs[];
        String line;

        callback.onStart();

        try {
            value = KLogCtl.kLogCtl(10, null, 0);
            logs = new byte[value];
            value = KLogCtl.kLogCtl(3, logs, value);

            Scanner stream = new Scanner(new ByteArrayInputStream(logs));

            while (stream.hasNextLine() && !interrupted()) {
                line = stream.nextLine();
                if (line.contains(avcSearchTag)) {
                    callback.onEvent(line);
                }
            }
        } catch (Exception e) {
            callback.onException(e);
        } finally {
            callback.onFinish();
        }
        return;
    }

    @Override
    public void run() {
        parseLogs();
    }

}
