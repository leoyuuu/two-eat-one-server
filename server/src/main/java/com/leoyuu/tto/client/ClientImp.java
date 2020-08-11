package com.leoyuu.tto.client;

import java.io.IOException;
import java.net.Socket;

import com.leoyuu.proto.PacketGen;
import com.leoyuu.proto.ProtoIOHandler;
import com.leoyuu.proto.TwoEatOnePackets;
import com.leoyuu.tto.game.GameManager;
import com.leoyuu.tto.match.MatchManager;
import com.leoyuu.utils.Logger;
import com.leoyuu.utils.ThreadUtil;

public class ClientImp implements Client{
    private final Integer uid;
    private final Socket skt;
    private final Logger logger;
    private boolean selfError = false;
    private long lastActiveTime = System.currentTimeMillis();
    private volatile int gid = 0;

    ClientImp(Socket skt, int uid) {
        this.skt = skt;
        this.uid = uid;
        logger = new Logger("client-" + uid);
    }

    @Override
    public void handle() {
        notifyUserChange();
        ThreadUtil.get().clientThread(this::waitAndHandle);
    }

    @Override
    public Integer getUid() {
        return uid;
    }

    @Override
    public void setGid(int gid) {
        this.gid = gid;
        logger.info("user game id change: {}", gid);
        notifyUserChange();
    }

    @Override
    public int getGid() {
        return gid;
    }

    @Override
    public void notifyNoGameError(int seq) {
        logger.warn("no game exist {} ", gid);
        sendMsg(PacketGen.rspErr(gid, seq, "游戏不存在，请重新匹配"));
    }

    private void notifyUserChange() {
        sendMsg(PacketGen.pushUser(uid, gid));
    }

    private void waitAndHandle() {
        while (!selfError) {
            try {
                TwoEatOnePackets.Packet packet = ProtoIOHandler.readMsg(skt.getInputStream());
                lastActiveTime = System.currentTimeMillis();
                handleReq(packet);
            } catch (IOException e) {
                selfError = true;
                logger.err("error handle client req {}", e);
                MatchManager.get().clientConnectionError(this);
                GameManager.get().clientConnectionError(this);
            }
        }
    }

    private void handleReq(TwoEatOnePackets.Packet packet) {
        logger.info("receive client packet: {}", packet.getType());
        switch (packet.getType()) {
            case CmdStartMatch:startMatch(packet);break;
            case CmdCancelMatch:cancelMatch(packet);break;
            case CmdChessMove:chessMove(packet);break;
            case CmdSyncGame:syncGame(packet);break;
            case CmdEnterGame:enterGame(packet);break;
            case CmdPushNeedSync:
            case CmdPushToast:
            case CmdRsp:
            case CmdPushUserInfo:
            case UNRECOGNIZED:
            case UnknownCommand:
            default:errorTpe(packet);break;
        }
    }

    private void errorTpe(TwoEatOnePackets.Packet packet) {
        logger.warn("warning error type: {}" + packet);
    }

    private void startMatch(TwoEatOnePackets.Packet packet) {
        if (gid > 0) {
            sendMsg(PacketGen.rspErr(gid, packet.getSeq(), "你已经在游戏中了"));
        } else {
            MatchManager.get().clientStartMatch(this, packet);
        }
    }

    private void cancelMatch(TwoEatOnePackets.Packet packet) {
        MatchManager.get().clientCancelMatch(this, packet);
    }

    private void chessMove(TwoEatOnePackets.Packet packet) {
        GameManager.get().clientMoveChess(this, packet);
    }

    private void enterGame(TwoEatOnePackets.Packet packet) {
        GameManager.get().clientEnterGame(this, packet);
    }
    private void syncGame(TwoEatOnePackets.Packet packet) {
        if (packet.getGid() > 0) {
            GameManager.get().syncGame(this, packet);
        } else {
            sendMsg(PacketGen.rspOk(0, packet.getSeq()));
        }
    }

    @Override
    public void sendMsg(TwoEatOnePackets.Packet packet) {
        try {
            logger.info("send packet: {} to client {}", packet.getType(), getUid());
            ProtoIOHandler.sendMsg(skt.getOutputStream(), packet);
        } catch (IOException e) {
            selfError = true;
            logger.err("error send packet {} to client {}", packet, e);
        }
    }

    @Override
    public void notifySync() {
        sendMsg(PacketGen.pushNeedSync());
    }

    @Override
    public void close() {
        selfError = true;
        try {
            if (!skt.isClosed()) {
                skt.close();
            }
        } catch (Exception e) {
            logger.err("client close error {}", e);
        }
        GameManager.get().notifyClientNotAlive(this);
    }

    @Override
    public boolean isAlive() {
        return !selfError;
    }

    @Override
    public boolean needSync() {
        return System.currentTimeMillis() - lastActiveTime > 30_000;
    }

    @Override
    public boolean longTimeNoActive() {
        return System.currentTimeMillis() - lastActiveTime > 60_000;
    }

    @Override
    public boolean selfError() {
        return selfError;
    }

    @Override
    public int hashCode() {
        return getUid();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Client && ((Client)obj).getUid().equals(getUid());
    }
}
