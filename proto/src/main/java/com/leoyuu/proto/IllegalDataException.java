package com.leoyuu.proto;

import java.io.IOException;

public class IllegalDataException extends IOException {
    public IllegalDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalDataException(String message) {
        super(message);
    }

    public IllegalDataException() {
    }
}
