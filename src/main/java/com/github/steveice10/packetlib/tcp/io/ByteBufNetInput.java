package com.github.steveice10.packetlib.tcp.io;

import com.github.steveice10.packetlib.io.NetInput;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A NetInput implementation using a ByteBuf as a backend.
 */
public class ByteBufNetInput implements NetInput {
    private ByteBuf buf;

    public ByteBufNetInput(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return this.buf.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return this.buf.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return this.buf.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return this.buf.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return this.buf.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return this.buf.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return this.buf.readInt();
    }

    @Override
    public int readVarInt() throws IOException {
        int value = 0;
        int size = 0;
        int b;
        while(((b = this.readByte()) & 0x80) == 0x80) {
            value |= (b & 0x7F) << size;
            size += 7;
            if (size > 35) {
                throw new IllegalArgumentException("VarInt wider than 35-bit");
            }
        }

        return value | ((b & 0x7F) << size);
    }

    @Override
    public long readLong() throws IOException {
        return this.buf.readLong();
    }

    @Override
    public long readVarLong() throws IOException {
        long value = 0;
        int size = 0;
        int b;
        while(((b = this.readByte()) & 0x80) == 0x80) {
            value |= (b & 0x7FL) << size;
            size += 7;
            if (size > 70) {
                throw new IllegalArgumentException("VarLong wider than 70-bit");
            }
        }

        return value | ((b & 0x7FL) << size);
    }

    @Override
    public float readFloat() throws IOException {
        return this.buf.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return this.buf.readDouble();
    }

    @Override
    public byte[] readBytes(int length) throws IOException {
        // todo: deprecate this method usages where possible and prefer returning a ByteBuf
        if(length < 0) {
            throw new IllegalArgumentException("Array cannot have length less than 0.");
        }

        byte b[] = new byte[length];
        this.buf.readBytes(b);
        return b;
    }

    public ByteBuf readBytesToBuf(int length) {
        if(length < 0) {
            throw new IllegalArgumentException("Array cannot have length less than 0.");
        }
        return this.buf.readBytes(length);
    }

    @Override
    public int readBytes(byte[] b) throws IOException {
        return this.readBytes(b, 0, b.length);
    }

    @Override
    public int readBytes(byte[] b, int offset, int length) throws IOException {
        int readable = this.buf.readableBytes();
        if(readable <= 0) {
            return -1;
        }

        if(readable < length) {
            length = readable;
        }

        this.buf.readBytes(b, offset, length);
        return length;
    }

    @Override
    public short[] readShorts(int length) throws IOException {
        if(length < 0) {
            throw new IllegalArgumentException("Array cannot have length less than 0.");
        }

        short s[] = new short[length];
        for(int index = 0; index < length; index++) {
            s[index] = this.readShort();
        }

        return s;
    }

    @Override
    public int readShorts(short[] s) throws IOException {
        return this.readShorts(s, 0, s.length);
    }

    @Override
    public int readShorts(short[] s, int offset, int length) throws IOException {
        int readable = this.buf.readableBytes();
        if(readable <= 0) {
            return -1;
        }

        if(readable < length * 2) {
            length = readable / 2;
        }

        for(int index = offset; index < offset + length; index++) {
            s[index] = this.readShort();
        }

        return length;
    }

    @Override
    public int[] readInts(int length) throws IOException {
        if(length < 0) {
            throw new IllegalArgumentException("Array cannot have length less than 0.");
        }

        int i[] = new int[length];
        for(int index = 0; index < length; index++) {
            i[index] = this.readInt();
        }

        return i;
    }

    @Override
    public int readInts(int[] i) throws IOException {
        return this.readInts(i, 0, i.length);
    }

    @Override
    public int readInts(int[] i, int offset, int length) throws IOException {
        int readable = this.buf.readableBytes();
        if(readable <= 0) {
            return -1;
        }

        if(readable < length * 4) {
            length = readable / 4;
        }

        for(int index = offset; index < offset + length; index++) {
            i[index] = this.readInt();
        }

        return length;
    }

    @Override
    public long[] readLongs(int length) throws IOException {
        if(length < 0) {
            throw new IllegalArgumentException("Array cannot have length less than 0.");
        }

        long l[] = new long[length];
        for(int index = 0; index < length; index++) {
            l[index] = this.readLong();
        }

        return l;
    }

    @Override
    public int readLongs(long[] l) throws IOException {
        return this.readLongs(l, 0, l.length);
    }

    @Override
    public int readLongs(long[] l, int offset, int length) throws IOException {
        int readable = this.buf.readableBytes();
        if(readable <= 0) {
            return -1;
        }

        if(readable < length * 2) {
            length = readable / 2;
        }

        for(int index = offset; index < offset + length; index++) {
            l[index] = this.readLong();
        }

        return length;
    }

    @Override
    public String readString() throws IOException {
        int length = this.readVarInt();
        ByteBuf byteBuf = this.readBytesToBuf(length);
        String s = byteBuf.readCharSequence(length, StandardCharsets.UTF_8).toString();
        byteBuf.release();
        return s;
    }

    @Override
    public UUID readUUID() throws IOException {
        return new UUID(this.readLong(), this.readLong());
    }

    @Override
    public int available() throws IOException {
        return this.buf.readableBytes();
    }

    public ByteBuf getBuffer() {
        return this.buf;
    }
}
