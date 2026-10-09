package com.velocitypowered.api.event.connection;

import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelMessageSink;
import com.velocitypowered.api.proxy.messages.ChannelMessageSource;

public final class PluginMessageEvent {

    private final ChannelMessageSource source;
    private final ChannelMessageSink target;
    private final ChannelIdentifier identifier;
    private final byte[] data;
    private boolean result = true;

    public PluginMessageEvent(ChannelMessageSource source, ChannelMessageSink target,
                              ChannelIdentifier identifier, byte[] data) {
        this.source = source;
        this.target = target;
        this.identifier = identifier;
        this.data = data;
    }

    public ChannelMessageSource getSource() { return source; }
    public ChannelMessageSink getTarget() { return target; }
    public ChannelIdentifier getIdentifier() { return identifier; }
    public byte[] getData() { return data; }
    public boolean getResult() { return result; }
    public void setResult(boolean result) { this.result = result; }
}