package com.velocitypowered.api.scheduler;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

    TaskBuilder buildTask(Object plugin, Runnable runnable);

    ScheduledTask schedule(Object plugin, Runnable task, long delay, TimeUnit unit);

    interface TaskBuilder {
        TaskBuilder delay(long delay, TimeUnit unit);
        TaskBuilder repeat(long delay, long interval, TimeUnit unit);
        ScheduledTask schedule();
    }
}