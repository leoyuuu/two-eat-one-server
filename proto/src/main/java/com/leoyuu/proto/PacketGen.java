package com.leoyuu.proto;

import java.util.concurrent.atomic.AtomicInteger;

public class PacketGen {
    private static AtomicInteger seq = new AtomicInteger();

    public static TwoEatOnePackets.Packet rspOk(int gid, int seq) {
        return TwoEatOnePackets.Packet.newBuilder()
                .setCode(0)
                .setSeq(seq)
                .setGid(gid)
                .setMsg("ok")
                .setType(TwoEatOnePackets.CmdType.CmdRsp)
                .build();
    }

    public static TwoEatOnePackets.Packet rspErr(int gid, int seq, String msg) {
        return TwoEatOnePackets.Packet.newBuilder()
                .setGid(gid)
                .setCode(-1)
                .setSeq(seq)
                .setMsg(msg)
                .setType(TwoEatOnePackets.CmdType.CmdRsp)
                .build();
    }

    public static TwoEatOnePackets.Packet startMatch() {
        return TwoEatOnePackets.Packet.newBuilder()
                .setCode(0)
                .setGid(0)
                .setSeq(seq.incrementAndGet())
                .setType(TwoEatOnePackets.CmdType.CmdStartMatch)
                .build();
    }

    public static TwoEatOnePackets.Packet cancelMatch() {
        return TwoEatOnePackets.Packet.newBuilder()
                .setCode(0)
                .setGid(0)
                .setSeq(seq.incrementAndGet())
                .setType(TwoEatOnePackets.CmdType.CmdCancelMatch)
                .build();
    }

    public static TwoEatOnePackets.Packet enterGame(int gid) {
        return TwoEatOnePackets.Packet.newBuilder()
                .setCode(0)
                .setGid(gid)
                .setSeq(seq.incrementAndGet())
                .setType(TwoEatOnePackets.CmdType.CmdEnterGame)
                .build();
    }

    public static TwoEatOnePackets.Packet syncReq(int gid) {
        return TwoEatOnePackets.Packet.newBuilder()
                .setCode(0)
                .setGid(gid)
                .setSeq(seq.incrementAndGet())
                .setType(TwoEatOnePackets.CmdType.CmdSyncGame)
                .build();
    }

    public static TwoEatOnePackets.Packet pushUser(int uid, int gid) {
        TwoEatOnePackets.PushUserInfo userInfo = TwoEatOnePackets.PushUserInfo
                .newBuilder()
                .setUid(uid)
                .setGid(gid)
                .build();
        return TwoEatOnePackets.Packet.newBuilder()
                .setCode(0)
                .setSeq(0)
                .setContent(TwoEatOnePackets.PacketContent.newBuilder().setUser(userInfo).build())
                .setType(TwoEatOnePackets.CmdType.CmdPushUserInfo)
                .build();
    }

    public static TwoEatOnePackets.Packet pushNeedSync() {
        return TwoEatOnePackets.Packet.newBuilder()
                .setCode(0)
                .setSeq(0)
                .setType(TwoEatOnePackets.CmdType.CmdPushNeedSync)
                .build();
    }

    public static TwoEatOnePackets.Packet chessMove(int gid, int fromIndex, int toIndex) {
        TwoEatOnePackets.ChessMoveContent moveContent = TwoEatOnePackets.ChessMoveContent.newBuilder()
                .setFromIndex(fromIndex)
                .setToIndex(toIndex).build();
        return TwoEatOnePackets.Packet.newBuilder()
                .setCode(0)
                .setGid(gid)
                .setContent(TwoEatOnePackets.PacketContent.newBuilder().setMove(moveContent).build())
                .setSeq(seq.incrementAndGet())
                .setType(TwoEatOnePackets.CmdType.CmdChessMove)
                .build();
    }

}
