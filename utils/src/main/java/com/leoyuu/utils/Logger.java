package com.leoyuu.utils;

import java.text.SimpleDateFormat;

public class Logger {
    private static LogLevel filterLevel = LogLevel.Debug;
    private final String tag;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public Logger(String tag) {
        this.tag = tag;
    }

    private void log(LogLevel level, String fmt, Object ... args) {
        if (level.value < filterLevel.value) {
            return;
        }
        StringBuilder sb = new StringBuilder(fmt);
        for (Object arg:args) {
            int index = sb.indexOf("{}");
            if (index >= 0) {
                try {
                    sb.replace(index, index+2, String.valueOf(arg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        System.out.printf("%s %s: %s\n", dateFormat.format(System.currentTimeMillis()), tag, sb);
    }

    public void debug(String fmt, Object ... args) {
        log(LogLevel.Debug, fmt, args);
    }

    public void trace(String fmt, Object ... args) {
        log(LogLevel.Trace, fmt, args);
    }
    public void info(String fmt, Object ... args) {
        log(LogLevel.Info, fmt, args);
    }
    public void warn(String fmt, Object ... args) {
        log(LogLevel.Warning, fmt, args);
    }
    public void err(String fmt, Object ... args) {
        log(LogLevel.Error, fmt, args);
    }
}
