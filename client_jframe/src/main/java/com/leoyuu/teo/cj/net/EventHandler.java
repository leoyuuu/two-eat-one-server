package com.leoyuu.teo.cj.net;

import com.leoyuu.proto.TwoEatOnePackets;

public interface EventHandler {
    boolean handleEvent(TwoEatOnePackets.Packet packet);
}
