package com.leoyuu.tto.client;

import com.leoyuu.proto.TwoEatOnePackets;

public interface Client {
    Integer getUid();
    void handle();
    void setGid(int gid);
    int getGid();
    void notifyNoGameError(int seq);
    void sendMsg(TwoEatOnePackets.Packet packet);
    void notifySync();
    void close();
    boolean isAlive();
    boolean needSync();
    boolean longTimeNoActive();
    boolean selfError();
}
