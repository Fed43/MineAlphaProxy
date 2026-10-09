package com.minealphaproxy.command;

public interface CommandSource {
    void sendMessage(String message);
    boolean hasPermission(String permission);
    String name();
}