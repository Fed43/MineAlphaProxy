package com.minealphaproxy.util;

import com.sun.jna.Platform;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public final class Logger {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final PrintStream STREAM;

    static {
        WinConsole.enableUtf8();
        WinConsole.enableAnsi();

        PrintStream stream;
        try {
            stream = new PrintStream(
                    new FileOutputStream(FileDescriptor.out),
                    true,
                    StandardCharsets.UTF_8);
        } catch (Throwable t) {
            stream = System.out;
        }
        STREAM = stream;
    }

    private final String shortName;

    public Logger(String name) {
        this.shortName = shorten(name);
    }

    public static Logger get(Class<?> clazz) {
        return new Logger(clazz.getName());
    }

    public void info(String msg, Object... args) { log("INFO", msg, args); }
    public void warn(String msg, Object... args) { log("WARN", msg, args); }
    public void error(String msg, Object... args) { log("ERROR", msg, args); }
    public void debug(String msg, Object... args) { /* disabled */ }

    private void log(String level, String msg, Object... args) {
        Throwable t = null;
        if (args != null && args.length > 0
                && args[args.length - 1] instanceof Throwable) {
            t = (Throwable) args[args.length - 1];
            args = Arrays.copyOf(args, args.length - 1);
        }

        String formatted = format(msg, args);
        String time = LocalTime.now().format(TIME_FMT);
        String line = "[" + time + "] " + pad(level) + " " + shortName
                + " - " + formatted;

        STREAM.println(colorize(level, line));
        if (t != null) {
            t.printStackTrace(STREAM);
        }
    }

    private static String pad(String level) {
        return switch (level) {
            case "INFO" -> "INFO ";
            case "WARN" -> "WARN ";
            case "ERROR" -> "ERROR";
            default -> level;
        };
    }

    private static String colorize(String level, String line) {
        String color = switch (level) {
            case "INFO" -> "\u001B[32m";
            case "WARN" -> "\u001B[33m";
            case "ERROR" -> "\u001B[31m";
            default -> "";
        };
        return color + line + "\u001B[0m";
    }

    private static String format(String msg, Object... args) {
        if (args == null || args.length == 0) return msg;
        StringBuilder sb = new StringBuilder(msg.length() + 32);
        int argIndex = 0;
        int i = 0;
        while (i < msg.length()) {
            int idx = msg.indexOf("{}", i);
            if (idx == -1 || argIndex >= args.length) {
                sb.append(msg, i, msg.length());
                break;
            }
            sb.append(msg, i, idx);
            sb.append(args[argIndex] == null ? "null" : args[argIndex].toString());
            i = idx + 2;
            argIndex++;
        }
        return sb.toString();
    }

    private static String shorten(String name) {
        String[] parts = name.split("\\.");
        if (parts.length <= 2) return name;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0)).append('.');
        }
        sb.append(parts[parts.length - 1]);
        return sb.toString();
    }
}