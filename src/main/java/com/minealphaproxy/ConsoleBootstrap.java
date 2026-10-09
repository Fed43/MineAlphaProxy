package com.minealphaproxy;

import com.minealphaproxy.util.WinConsole;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class ConsoleBootstrap {

    private ConsoleBootstrap() {}

    public static void init() {
        // 1. Set Windows console codepage to UTF-8
        WinConsole.enableUtf8();

        // 2. Replace System.out / System.err with UTF-8 streams
        try {
            System.setOut(new PrintStream(
                    new FileOutputStream(FileDescriptor.out),
                    true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(
                    new FileOutputStream(FileDescriptor.err),
                    true, StandardCharsets.UTF_8));
        } catch (Throwable ignored) {
            // fallback: keep default streams
        }
    }
}