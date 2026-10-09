package com.minealphaproxy.compat.viaproxy;

import com.minealphaproxy.util.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ViaProxyLauncher {

    private static Process process;
    private static Path workDir;

    public static void start(int bindPort, int targetPort, Logger logger) {
        try {
            workDir = Files.createDirectories(
                    Path.of(System.getProperty("java.io.tmpdir"), "minealpha-viaproxy"));

            Path jarPath = workDir.resolve("ViaProxy.jar");
            if (!Files.exists(jarPath)) {
                try (InputStream in = ViaProxyLauncher.class.getResourceAsStream(
                        "/viaproxy/ViaProxy.bin")) {
                    if (in == null) {
                        logger.warn("Embedded ViaProxy.bin not found вЂ” Via disabled");
                        return;
                    }
                    Files.copy(in, jarPath, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Extracted ViaProxy to {}", jarPath);
                }
            }

            Path configPath = workDir.resolve("viaproxy.yml");
            String yaml =
                    "bind-address: 0.0.0.0:" + bindPort + "\n" +
                    "target-address: 127.0.0.1:" + targetPort + "\n" +
                    "target-version: 26.3\n" +
                    "server-icon: false\n" +
                    "legacy-passthrough: true\n";
            Files.writeString(configPath, yaml, StandardCharsets.UTF_8);

            String javaBin = System.getProperty("java.home")
                    + File.separator + "bin" + File.separator + "java";

            ProcessBuilder pb = new ProcessBuilder(
                    javaBin,
                    "-jar", jarPath.toString(),
                    "config", configPath.toString()
            );
            pb.directory(workDir.toFile());

            // stdout/stderr вЂ” РІ Р»РѕРі-С„Р°Р№Р»
            Path logFile = workDir.resolve("viaproxy.log");
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
            pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile.toFile()));

            // stdin вЂ” РІ NUL-СѓСЃС‚СЂРѕР№СЃС‚РІРѕ, С‡С‚РѕР±С‹ ViaProxy РќР• РјРѕРі С‡РёС‚Р°С‚СЊ РЅР°С€ РєРѕРЅСЃРѕР»СЊРЅС‹Р№ РІРІРѕРґ
            File nul = new File("NUL");
            pb.redirectInput(ProcessBuilder.Redirect.from(nul));

            process = pb.start();
            logger.info("ViaProxy started: bind {} -> target {}", bindPort, targetPort);
            logger.info("ViaProxy log: {}", logFile);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (process != null && process.isAlive()) {
                    process.destroy();
                    logger.info("ViaProxy stopped");
                }
            }));

        } catch (Throwable t) {
            logger.warn("Failed to start ViaProxy: {}: {}",
                    t.getClass().getSimpleName(), t.getMessage());
        }
    }

    public static boolean isRunning() {
        return process != null && process.isAlive();
    }

    private ViaProxyLauncher() {}
}