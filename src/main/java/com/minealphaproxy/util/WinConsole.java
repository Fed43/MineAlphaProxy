package com.minealphaproxy.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public final class WinConsole {

    private WinConsole() {}

    private interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        boolean SetConsoleOutputCP(int codePage);
        boolean SetConsoleCP(int codePage);
        int GetConsoleOutputCP();

        long GetStdHandle(int nStdHandle);
        boolean GetConsoleMode(long handle, int[] mode);
        boolean SetConsoleMode(long handle, int mode);
    }

    private static final int STD_OUTPUT_HANDLE = -11;
    private static final int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004;

    public static void enableUtf8() {
        if (!Platform.isWindows()) return;
        try {
            Kernel32.INSTANCE.SetConsoleOutputCP(65001);
            Kernel32.INSTANCE.SetConsoleCP(65001);
        } catch (Throwable ignored) {}
    }

    public static void enableAnsi() {
        if (!Platform.isWindows()) return;
        try {
            long handle = Kernel32.INSTANCE.GetStdHandle(STD_OUTPUT_HANDLE);
            int[] mode = new int[1];
            if (Kernel32.INSTANCE.GetConsoleMode(handle, mode)) {
                Kernel32.INSTANCE.SetConsoleMode(handle,
                        mode[0] | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
            }
        } catch (Throwable ignored) {}
    }
}