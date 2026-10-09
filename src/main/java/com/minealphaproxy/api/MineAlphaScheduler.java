package com.minealphaproxy.api;

import java.util.concurrent.TimeUnit;

public interface MineAlphaScheduler {

    MineAlphaTask runAsync(Runnable task);
    MineAlphaTask runAsyncLater(Runnable task, long delay, TimeUnit unit);
    MineAlphaTask runAsyncRepeating(Runnable task, long delay, long period, TimeUnit unit);

    interface MineAlphaTask {
        void cancel();
        boolean isCancelled();
    }
}