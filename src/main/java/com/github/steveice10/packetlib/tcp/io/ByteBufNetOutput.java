package com.github.steveice10.packetlib.tcp.io;

import com.github.steveice10.packetlib.io.NetOutput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A NetOutput implementation using a ByteBuf as a backend.
 */
public class ByteBufNetOutput implements NetOutput {
    private ByteBuf buf;

    public ByteBufNetOutput(ByteBuf buf) {
        this.buf = buf;
    }

    public static ByteBufNetOutput createWrappedOutput(byte[] data) {
        ByteBufNetOutput output = new ByteBufNetOutput(Unpooled.wrappedBuffer(data));
        output.getBuffer().resetWriterIndex(); // writer index defaults to the length of the buffer.
        return output;
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        this.buf.writeBoolean(b);
    }

    @Override
    public void writeByte(int b) throws IOException {
        this.buf.writeByte(b);
    }

    @Override
    public void writeShort(int s) throws IOException {
        this.buf.writeShort(s);
    }

    @Override
    public void writeChar(int c) throws IOException {
        this.buf.writeChar(c);
    }

    @Override
    public void writeInt(int i) throws IOException {
        this.buf.writeInt(i);
    }

    @Override
    public void writeVarInt(int value) throws IOException {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            this.buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            this.buf.writeShort(w);
        } else {
            writeVarIntFull(this.buf, value);
        }
    }

    private static void writeVarIntFull(ByteBuf buf, int value) {
        // See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
            buf.writeMedium(w);
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            int w = (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21);
            buf.writeInt(w);
        } else {
            int w = (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80);
            buf.writeInt(w);
            buf.writeByte(value >>> 28);
        }
    }

    @Override
    public void writeLong(long l) throws IOException {
        this.buf.writeLong(l);
    }

    @Override
    public void writeVarLong(long l) throws IOException {
        while((l & ~0x7F) != 0) {
            this.writeByte((int) (l & 0x7F) | 0x80);
            l >>>= 7;
        }

        this.writeByte((int) l);
    }

    @Override
    public void writeFloat(float f) throws IOException {
        this.buf.writeFloat(f);
    }

    @Override
    public void writeDouble(double d) throws IOException {
        this.buf.writeDouble(d);
    }

    @Override
    public void writeBytes(byte b[]) throws IOException {
        this.buf.writeBytes(b);
    }

    @Override
    public void writeBytes(byte b[], int length) throws IOException {
        this.buf.writeBytes(b, 0, length);
    }

    @Override
    public void writeShorts(short[] s) throws IOException {
        this.writeShorts(s, s.length);
    }

    @Override
    public void writeShorts(short[] s, int length) throws IOException {
        for(int index = 0; index < length; index++) {
            this.writeShort(s[index]);
        }
    }

    @Override
    public void writeInts(int[] i) throws IOException {
        this.writeInts(i, i.length);
    }

    @Override
    public void writeInts(int[] i, int length) throws IOException {
        for(int index = 0; index < length; index++) {
            this.writeInt(i[index]);
        }
    }

    @Override
    public void writeLongs(long[] l) throws IOException {
        this.writeLongs(l, l.length);
    }

    @Override
    public void writeLongs(long[] l, int length) throws IOException {
        for(int index = 0; index < length; index++) {
            this.writeLong(l[index]);
        }
    }

    @Override
    public void writeString(String s) throws IOException {
        if(s == null) {
            throw new IllegalArgumentException("String cannot be null!");
        }
        int utf8Bytes = ByteBufUtil.utf8Bytes(s);
        if (utf8Bytes > 32767) {
            throw new IOException("String too big (was " + s.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            this.writeVarInt(utf8Bytes);
            this.buf.writeCharSequence(s, StandardCharsets.UTF_8);
        }
    }

    @Override
    public void writeUUID(UUID uuid) throws IOException {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
    }

    public ByteBuf getBuffer() {
        return this.buf;
    }
}
