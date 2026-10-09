package com.velocitypowered.api.proxy.messages;

public interface ChannelRegistrar {
    void register(ChannelIdentifier... identifiers);
    void unregister(ChannelIdentifier... identifiers);
    void unregister(ChannelIdentifier identifier);
    java.util.Collection<ChannelIdentifier> getChannels();
}