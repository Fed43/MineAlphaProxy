package com.minealphaproxy.api.impl;

import com.minealphaproxy.api.MineAlphaScheduler;
import com.minealphaproxy.util.Logger;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MineAlphaSchedulerImpl implements MineAlphaScheduler {

    private final Logger logger;
    private final ScheduledExecutorService executor;
    private final Map<MineAlphaTask, Future<?>> tasks = new ConcurrentHashMap<>();

    public MineAlphaSchedulerImpl(Logger logger) {
        this.logger = logger;
        this.executor = Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r, "minealpha-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    @Override public MineAlphaTask runAsync(Runnable task) {
        return runAsyncLater(task, 0, TimeUnit.MILLISECONDS);
    }

    @Override public MineAlphaTask runAsyncLater(Runnable task, long delay, TimeUnit unit) {
        TaskImpl t = new TaskImpl();
        Future<?> f = executor.schedule(() -> safeRun(task), delay, unit);
        t.future = f;
        tasks.put(t, f);
        return t;
    }

    @Override public MineAlphaTask runAsyncRepeating(Runnable task, long delay,
                                                      long period, TimeUnit unit) {
        TaskImpl t = new TaskImpl();
        Future<?> f = executor.scheduleAtFixedRate(
                () -> safeRun(task), delay, period, unit);
        t.future = f;
        tasks.put(t, f);
        return t;
    }

    private void safeRun(Runnable r) {
        try { r.run(); }
        catch (Throwable ex) { logger.error("Scheduled task failed", ex); }
    }

    public void shutdown() { executor.shutdownNow(); }

    private final class TaskImpl implements MineAlphaTask {
        Future<?> future;
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        @Override public void cancel() {
            if (cancelled.compareAndSet(false, true) && future != null) future.cancel(false);
        }
        @Override public boolean isCancelled() { return cancelled.get(); }
    }
}