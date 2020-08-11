package com.leoyuu.utils;

import org.junit.Test;

public class TestLogger {
    @Test
    public void testLogger() {
        Logger logger = new Logger("Test");
        logger.info("error parse {}", 123);
        logger.info("the seq {} callback {}", 123, new TestLogger());
        logger.info("error args {}, {}", 123, 123, 123);
    }
}
