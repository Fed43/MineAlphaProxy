package com.minealphaproxy.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public final class MinecraftVarintFrameDecoder extends ByteToMessageDecoder {

    private static final int MAX_PACKET_SIZE = 2 * 1024 * 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        int length;
        try {
            length = VarInts.readVarInt(in);
        } catch (IndexOutOfBoundsException e) {
            in.resetReaderIndex();
            return;
        } catch (RuntimeException e) {
            ctx.close();
            return;
        }

        if (length < 0 || length > MAX_PACKET_SIZE) {
            ctx.close();
            return;
        }
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        out.add(in.readRetainedSlice(length));
    }
}