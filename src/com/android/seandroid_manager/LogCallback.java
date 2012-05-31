
package com.android.seandroid_manager;

/**
 * Used to handle events from parsing logs looking for messages.
 */
public abstract class LogCallback {

    /**
     * Before parsing begins, this function is called. By default nothing
     * happens, as this function is not required for proper function, but can be
     * overridden by subclass for perhaps starting a progress dialog.
     */
    public void onStart() {
    }

    /**
     * For each message encountered, this callback is called. This
     * must be implemented, each raw message is passed to this
     * callback. From here further processing can be done.
     * 
     * @param message The exact message encountered while parsing.
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
     * 
     * @param e The exception generated
     */
    public void onException(Exception e) {
    }

}
