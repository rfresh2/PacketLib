package com.github.steveice10.packetlib.tcp;

import com.velocitypowered.natives.encryption.VelocityCipher;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.util.List;

public class TcpPacketVelocityEncryptor extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    private final VelocityCipher velocityCipherDecrypt;
    private final VelocityCipher velocityCipherEncrypt;

    public TcpPacketVelocityEncryptor(SecretKey key) {
        try {
            this.velocityCipherDecrypt = Natives.cipher.get().forDecryption(key);
            this.velocityCipherEncrypt = Natives.cipher.get().forEncryption(key);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.velocityCipherEncrypt.close();
        this.velocityCipherDecrypt.close();
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        ByteBuf compatible = MoreByteBufUtils.ensureCompatible(ctx.alloc(), velocityCipherEncrypt, in);
        try {
            velocityCipherEncrypt.process(compatible);
            out.add(compatible);
        } catch (Exception e) {
            compatible.release();
            throw e;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBuf compatible = MoreByteBufUtils.ensureCompatible(ctx.alloc(), velocityCipherDecrypt, in);
        try {
            velocityCipherDecrypt.process(compatible);
            out.add(compatible);
        } catch (Exception e) {
            compatible.release(); // compatible will never be used if we throw an exception
            throw e;
        }
    }
}
