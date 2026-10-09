package com.minealphaproxy.compat.velocity;

import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class VelocityChannelRegistrar implements ChannelRegistrar {

    private final Set<ChannelIdentifier> channels = new CopyOnWriteArraySet<>();

    @Override
    public void register(ChannelIdentifier... identifiers) {
        for (ChannelIdentifier id : identifiers) channels.add(id);
    }

    @Override
    public void unregister(ChannelIdentifier... identifiers) {
        for (ChannelIdentifier id : identifiers) channels.remove(id);
    }

    @Override
    public void unregister(ChannelIdentifier identifier) {
        channels.remove(identifier);
    }

    @Override
    public Collection<ChannelIdentifier> getChannels() {
        return Collections.unmodifiableSet(channels);
    }
}