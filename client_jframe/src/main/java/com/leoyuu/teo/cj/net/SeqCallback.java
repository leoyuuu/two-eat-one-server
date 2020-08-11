package com.leoyuu.teo.cj.net;

import com.leoyuu.proto.TwoEatOnePackets;

public interface SeqCallback {
    void handleRsp(TwoEatOnePackets.Packet packet);
    void onError(int code, String msg);
}
