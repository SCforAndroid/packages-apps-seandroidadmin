
package com.android.seandroid_manager;

public class KLogCtl {

    /**
     * Reads from the kernel logs. See man klogctl.
     * 
     * @param type Determines the action to be taken by this function. Quoting
     *            from kernel/printk.c: Commands to sys_syslog: 0 -- Close the
     *            log. Currently a NOP. 1 -- Open the log. Currently a NOP. 2 --
     *            Read from the log. 3 -- Read all messages remaining in the
     *            ring buffer. 4 -- Read and clear all messages remaining in the
     *            ring buffer 5 -- Clear ring buffer. 6 -- Disable printk to
     *            console 7 -- Enable printk to console 8 -- Set level of
     *            messages printed to console 9 -- Return number of unread
     *            characters in the log buffer 10 -- Return size of the log
     *            buffer
     * @throws An exception for each std error the syscall can fail with ENOSYS
     *             --> MissingResourceException, EPERM -->
     *             AccessControlException, ERESTARTSYS --> InterruptedException,
     *             EINVAL --> IllegalArgumentException, and an unknown error not
     *             listed above will throw --> Exception
     * @param buf Buffer to read the kernel log into.
     * @param length The number of bytes to read.
     * @return bytes read on commands 2, 3, 4 and 6, else 0 on success.
     */
    public static native int kLogCtl(int type, byte buf[], int length) throws Exception;

    static {
        System.loadLibrary("jni_klogctl");
    }
}
