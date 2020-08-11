package com.leoyuu.utils;

public enum LogLevel {
    Debug(0),
    Trace(1),
    Info(2),
    Warning(3),
    Error(4);

    public final int value;

    LogLevel(int value) {
        this.value = value;
    }
}
