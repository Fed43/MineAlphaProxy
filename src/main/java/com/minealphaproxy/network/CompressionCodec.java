package com.minealphaproxy.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class CompressionCodec extends MessageToMessageCodec<ByteBuf, ByteBuf> {

    private final int threshold;

    public CompressionCodec(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
            throws Exception {
        int dataLength = VarInts.readVarInt(msg);
        if (dataLength == 0) {
            out.add(msg.retain());
            return;
        }

        byte[] compressed = new byte[msg.readableBytes()];
        msg.readBytes(compressed);

        byte[] decompressed = new byte[dataLength];
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        int written = inflater.inflate(decompressed);
        inflater.end();

        if (written != dataLength) {
            throw new IllegalStateException("Decompression size mismatch: "
                    + written + " != " + dataLength);
        }
        out.add(Unpooled.wrappedBuffer(decompressed));
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
            throws Exception {
        int size = msg.readableBytes();
        ByteBuf result = ctx.alloc().buffer();

        if (size < threshold) {
            VarInts.writeVarInt(result, 0);
            result.writeBytes(msg);
        } else {
            VarInts.writeVarInt(result, size);
            byte[] raw = new byte[size];
            msg.readBytes(raw);

            byte[] buffer = new byte[size];
            Deflater deflater = new Deflater();
            deflater.setInput(raw);
            deflater.finish();
            int compressedSize = deflater.deflate(buffer);
            deflater.end();

            result.writeBytes(buffer, 0, compressedSize);
        }
        out.add(result);
    }
}