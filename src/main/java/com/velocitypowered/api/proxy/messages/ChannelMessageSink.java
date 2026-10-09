package com.velocitypowered.api.proxy.messages;

public interface ChannelMessageSink {
    boolean sendPluginMessage(ChannelIdentifier identifier, byte[] data);
}