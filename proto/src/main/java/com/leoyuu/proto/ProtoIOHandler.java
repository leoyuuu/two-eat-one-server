package com.leoyuu.proto;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ProtoIOHandler {

    public static void sendMsg(OutputStream os, TwoEatOnePackets.Packet packet) throws IOException {
        byte[] packetBuf = packet.toByteArray();
        ByteBuffer lenBuf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        lenBuf.putInt(packetBuf.length);
        lenBuf.position(0);
        os.write(lenBuf.array());
        os.write(packetBuf);
        os.flush();
    }

    public static TwoEatOnePackets.Packet readMsg(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        byte[] lenBytes = new byte[4];
        dis.readFully(lenBytes);
        ByteBuffer lenBuf = ByteBuffer.wrap(lenBytes).order(ByteOrder.BIG_ENDIAN);
        int len = lenBuf.getInt();
        if (len < 0 || len > 32476) {
            throw new IllegalDataException("len error: " + len);
        }
        byte[] bytes = new byte[len];
        dis.readFully(bytes);
        return TwoEatOnePackets.Packet.parseFrom(bytes);
    }
}
