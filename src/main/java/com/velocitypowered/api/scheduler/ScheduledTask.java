package com.velocitypowered.api.scheduler;

public interface ScheduledTask {
    void cancel();
    Object plugin();
}