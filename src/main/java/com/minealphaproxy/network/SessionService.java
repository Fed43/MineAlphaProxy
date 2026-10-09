package com.minealphaproxy.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SessionService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private SessionService() {}

    public static CompletableFuture<PlayerProfile> hasJoined(String username,
                                                             String serverHash) {
        String url = "https://sessionserver.mojang.com/session/minecraft/hasJoined"
                + "?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&serverId=" + URLEncoder.encode(serverHash, StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();

        return HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() != 200) return null;
                    try {
                        JsonNode root = JSON.readTree(resp.body());
                        String idHex = root.path("id").asText(null);
                        String name = root.path("name").asText(null);
                        if (idHex == null || name == null) return null;

                        UUID uuid = uuidFromUndashed(idHex);

                        List<PlayerProfile.Property> props = new ArrayList<>();
                        JsonNode propsNode = root.path("properties");
                        if (propsNode.isArray()) {
                            for (JsonNode p : propsNode) {
                                String pName = p.path("name").asText(null);
                                String pValue = p.path("value").asText(null);
                                String pSig = p.hasNonNull("signature")
                                        ? p.get("signature").asText() : null;
                                if (pName != null && pValue != null) {
                                    props.add(new PlayerProfile.Property(
                                            pName, pValue, pSig));
                                }
                            }
                        }
                        // Ключ чата в online-mode приходит от клиента, не от Mojang
                        return new PlayerProfile(uuid, name, props, null);
                    } catch (Exception e) {
                        return null;
                    }
                });
    }

    private static UUID uuidFromUndashed(String hex) {
        return new UUID(
                Long.parseUnsignedLong(hex.substring(0, 16), 16),
                Long.parseUnsignedLong(hex.substring(16, 32), 16));
    }
}