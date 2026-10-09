package com.minealphaproxy.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public final class BackendConnector {

    @FunctionalInterface
    public interface OnSuccess {
        void accept(Channel backendChannel);
    }

    @FunctionalInterface
    public interface OnFailure {
        void accept(String host, int port, Throwable cause);
    }

    public static void connect(EventLoopGroup group, String host, int port,
                               int timeoutMs, OnSuccess onSuccess,
                               OnFailure onFailure) {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast("frame-decoder",
                                        new MinecraftVarintFrameDecoder())
                                .addLast("frame-encoder",
                                        new MinecraftFrameEncoder());
                    }
                });

        ChannelFuture future = b.connect(host, port);
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                onSuccess.accept(f.channel());
            } else {
                onFailure.accept(host, port, f.cause());
            }
        });
    }

    private BackendConnector() {}
}