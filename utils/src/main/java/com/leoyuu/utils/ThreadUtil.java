package com.leoyuu.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUtil {
    private static final ThreadUtil util = new ThreadUtil();
    private Executor clientExecutor = Executors.newCachedThreadPool(new Factory("client"));
    private Executor gameExecutor = Executors.newCachedThreadPool(new Factory("game"));
    private Executor fixedThreadPool = Executors.newFixedThreadPool(2, new Factory("watch-dog"));

    public void gameThread(Runnable runnable) {
        gameExecutor.execute(runnable);
    }

    public void clientThread(Runnable runnable) {
        clientExecutor.execute(runnable);
    }

    public void fixExecutor(Runnable runnable) {
        fixedThreadPool.execute(runnable);
    }

    private static class Factory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private Factory(String type) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = type + "-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public static ThreadUtil get() {
        return util;
    }
}
