package com.calimeraw.atrackpad.app.networks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by cedric.creusot on 07/04/14.
 * class Packet
 * desc: This class is used to prepare the packet to send over network.
 */
public class Packet {
    private static final int SIZE = 512;
    private ByteArrayOutputStream mData;
    private DataOutputStream mBuffer;

    public Packet() {
        mData = new ByteArrayOutputStream(SIZE);
        mBuffer = new DataOutputStream(mData);
    }

    public Packet writeInt(int value) throws IOException {
        mBuffer.writeInt(value);
        return this;
    }

    public Packet writeLong(long value) throws IOException {
        mBuffer.writeLong(value);
        return this;
    }

    public Packet writeFloat(float value) throws IOException {
        mBuffer.writeFloat(value);
        return this;
    }

    public Packet writeDouble(double value) throws IOException {
        mBuffer.writeDouble(value);
        return this;
    }

    public Packet writeChar(char value) throws IOException {
        mBuffer.writeChar(value);
        return this;
    }

    public Packet writeShort(short value) throws IOException {
        mBuffer.writeShort(value);
        return this;
    }

    public Packet writeString(String value) throws IOException {
        mBuffer.writeUTF(value);
        return this;
    }

    public byte[] value() {
        return mData.toByteArray();
    }

    public int getOffset() {
        return mBuffer.size();
    }

    public void release() {
        try {
            mBuffer.close();
            mData.close();
        } catch (IOException e) {
            // do nothing
        }
    }
}
