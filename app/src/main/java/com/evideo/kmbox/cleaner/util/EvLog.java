package com.evideo.kmbox.cleaner.util;

import android.util.Log;

public class EvLog {

    private static String m_stag = "KmBox_Logger";

    private static boolean debug = true;

    /*
     * private static EvLog instance = new EvLog();
     * 
     * 
     * private EvLog() { }
     * 
     * public static EvLog getLogger() { return instance; }
     */

    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return null;
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getFileName().equals("EvLog.java")) {
                continue;
            }

            return "[" + Thread.currentThread().getName() + "("
                    + Thread.currentThread().getId() + "): " + st.getFileName()
                    + ":" + st.getLineNumber() + "]";
        }

        return null;
    }

    private static String createMessage(String msg) {
        String functionName = getFunctionName();
        String message = (functionName == null ? msg
                : (functionName + " - " + msg));
        return message;
    }

    public static void i(String tag, String msg) {
        if (debug) {
            Log.i(tag, createMessage(msg));
        }
    }

    /**
     * log.i
     */
    public static void i(String msg) {
        i(m_stag, msg);
    }

    public static void v(String tag, String msg) {
        if (debug) {
            Log.v(tag, createMessage(msg));
        }
    }

    /**
     * log.v
     */
    public static void v(String msg) {
        v(m_stag, msg);
    }

    public static void d(String tag, String msg) {
        if (debug) {
            Log.d(tag, createMessage(msg));
        }
    }

    /**
     * log.d
     */
    public static void d(String msg) {
        d(m_stag, msg);
    }

    public static void e(String tag, String msg) {
        if (debug) {
            Log.e(tag, createMessage(msg));
        }
    }

    /**
     * log.e
     */
    public static void e(String msg) {
        e(m_stag, msg);
    }

    public static void w(String tag, String msg) {
        if (debug) {
            Log.w(tag, createMessage(msg));
        }
    }

    /**
     * log.w
     */
    public static void w(String msg) {
        w(m_stag, msg);
    }

    /**
     * log.error
     */
    public static void error(Exception e) {
        if (debug) {
            StringBuffer sb = new StringBuffer();
            String name = getFunctionName();
            StackTraceElement[] sts = e.getStackTrace();

            if (name != null) {
                sb.append(name + " - " + e + "\r\n");
            } else {
                sb.append(e + "\r\n");
            }
            if (sts != null && sts.length > 0) {
                for (StackTraceElement st : sts) {
                    if (st != null) {
                        sb.append("[ " + st.getFileName() + ":"
                                + st.getLineNumber() + " ]\r\n");
                    }
                }
            }
            Log.e(m_stag, sb.toString());
        }
    }

    public static void setTag(String tag) {
        m_stag = tag;
    }

    /**
     * set debug
     */
    public static void setDebug(boolean d) {
        debug = d;
    }

    public static boolean isDebug() {
        return debug;
    }
}