package com.leoyuu.teo.client;

import com.leoyuu.proto.TwoEatOnePackets;
import com.leoyuu.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SeqCallbackManager {
    private Logger logger = new Logger("SeqCallbackManager");
    private Map<Integer, SeqCallback> map = new HashMap<>();
    private Timer timer = new Timer();

    public void add(int seq, SeqCallback callback) {
        map.put(seq, callback);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SeqCallback callback = map.remove(seq);
                if (callback != null) {
                    logger.info("the seq {} timeout", seq);
                    callback.onError(-2, "请求超时");
                }
            }
        }, 10_000);
    }

    public void handleRsp(TwoEatOnePackets.Packet packet) {
        SeqCallback callback = map.remove(packet.getSeq());
        logger.info("the seq {} callback {}", packet.getSeq(), callback);
        if (callback != null) {
            if (packet.getCode() != 0) {
                callback.onError(packet.getCode(), packet.getMsg());
            } else {
                callback.handleRsp(packet);
            }
        }
    }
}
