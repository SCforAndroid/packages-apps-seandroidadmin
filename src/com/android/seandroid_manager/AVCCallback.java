
package com.android.seandroid_manager;

/**
 * Used to handle events from parsing the kernel logs looking for AVC messages.
 */
public abstract class AVCCallback {

    /**
     * Before parsing begins, this function is called. By default nothing
     * happens, as this function is not required for proper function, but can be
     * overridden by subclass for perhaps starting a progress dialog.
     */
    public void onStart() {
    }

    /**
     * For each AVC denied message encountered, this callback is called. This
     * must be implemented, each raw kernel AVC message is passed to this
     * callback. From here further processing can be done.
     * 
     * @param message The exact AVC denied message encountered while parsing.
     */
    public abstract void onEvent(String logMessage);

    /**
     * This is called at the end of parsing, when no more messages are found. By
     * default nothing happens, as this function is not required for proper
     * function, but can be overridden by a subclass for perhaps starting a
     * progress dialog.
     */
    public void onFinish() {
    }

    /**
     * Called when an exception occurs.
     * The exception generated while trying to get the kernel ring buffer.
     * 
     * @param e The exception generated
     */
    public void onException(Exception e) {
    }

}
