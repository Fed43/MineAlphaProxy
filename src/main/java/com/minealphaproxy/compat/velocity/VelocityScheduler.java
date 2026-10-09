package com.minealphaproxy.compat.velocity;

import com.minealphaproxy.util.Logger;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VelocityScheduler implements Scheduler {

    private final ScheduledExecutorService executor;
    private final Logger logger;
    private final Map<ScheduledTask, Future<?>> tasks = new ConcurrentHashMap<>();

    public VelocityScheduler(Logger logger) {
        this.logger = logger;
        this.executor = Executors.newScheduledThreadPool(4, r -> {
            Thread t = new Thread(r, "velocity-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public TaskBuilder buildTask(Object plugin, Runnable runnable) {
        return new TaskBuilderImpl(plugin, runnable);
    }

    @Override
    public ScheduledTask schedule(Object plugin, Runnable task, long delay, TimeUnit unit) {
        return buildTask(plugin, task).delay(delay, unit).schedule();
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private final class TaskBuilderImpl implements TaskBuilder {
        private final Object plugin;
        private final Runnable runnable;
        private long delay = 0;
        private long interval = -1;
        private TimeUnit unit = TimeUnit.SECONDS;

        TaskBuilderImpl(Object plugin, Runnable runnable) {
            this.plugin = plugin;
            this.runnable = runnable;
        }

        @Override
        public TaskBuilder delay(long d, TimeUnit u) {
            this.delay = d;
            this.unit = u;
            return this;
        }

        @Override
        public TaskBuilder repeat(long d, long i, TimeUnit u) {
            this.delay = d;
            this.interval = i;
            this.unit = u;
            return this;
        }

        @Override
        public ScheduledTask schedule() {
            ScheduledTask task = new TaskImpl(plugin);
            Future<?> future;
            if (interval > 0) {
                future = executor.scheduleAtFixedRate(() -> safeRun(task), delay, interval, unit);
            } else {
                future = executor.schedule(() -> safeRun(task), delay, unit);
            }
            tasks.put(task, future);
            return task;
        }

        private void safeRun(ScheduledTask task) {
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.error("Scheduled task threw exception", t);
            }
        }
    }

    private final class TaskImpl implements ScheduledTask {
        private final Object plugin;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        TaskImpl(Object plugin) { this.plugin = plugin; }

        @Override
        public void cancel() {
            if (cancelled.compareAndSet(false, true)) {
                Future<?> f = tasks.remove(this);
                if (f != null) f.cancel(false);
            }
        }

        @Override
        public Object plugin() { return plugin; }
    }
}