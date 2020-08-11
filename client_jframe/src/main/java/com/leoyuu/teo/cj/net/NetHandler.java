package com.leoyuu.teo.cj.net;

import com.leoyuu.proto.PacketGen;
import com.leoyuu.proto.ProtoIOHandler;
import com.leoyuu.proto.TwoEatOnePackets;
import com.leoyuu.utils.Logger;
import com.leoyuu.utils.ThreadUtil;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NetHandler {
    private static final Integer NONE = TwoEatOnePackets.ChessType.NoChess_VALUE;
    private static final Integer BLACK = TwoEatOnePackets.ChessType.BlackChess_VALUE;
    private static final Integer WHITE = TwoEatOnePackets.ChessType.WhiteChess_VALUE;
    private Logger logger = new Logger("client");
    private int uid = -1;
    private int gid = -1;
    private Socket skt = new Socket();
    private boolean noError = true;
    private Integer myRole = NONE;
    private boolean isMatching = false;
    private boolean enterGameSent = false;
    private List<Integer> chess = new ArrayList<>(25);
    private EventHandler eventHandler;
    private SeqCallbackManager manager = new SeqCallbackManager();

    public void connect() {
        try {
            skt = new Socket();
            skt.connect(new InetSocketAddress("localhost", 10009));
            ThreadUtil.get().fixExecutor(this::readMsg);
            logger.info("client connected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            skt.close();
            logger.info("client disconnect");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void startMatch() {
        sendMsg(PacketGen.startMatch(), new SeqCallback() {
            @Override
            public void handleRsp(TwoEatOnePackets.Packet packet) {
                logger.info("start match res: {}", packet.getCode());
                isMatching = packet.getCode() == 0;
            }

            @Override
            public void onError(int code, String msg) {
                logger.info("start match res: {}", msg);
            }
        });
    }

    public void cancelMatch() {
        sendMsg(PacketGen.cancelMatch(), new SeqCallback() {
            @Override
            public void handleRsp(TwoEatOnePackets.Packet packet) {
                logger.info("start match res: {}", packet.getCode());
                isMatching = packet.getCode() == 0;
            }

            @Override
            public void onError(int code, String msg) {
                logger.info("start match res: {}", msg);
            }
        });
    }

    public boolean isConnected() {
        return skt.isConnected() && !skt.isClosed();
    }

    public boolean isMatching() {
        return isMatching;
    }

    private void readMsg() {
        while (noError) {
            try {
                TwoEatOnePackets.Packet packet = ProtoIOHandler.readMsg(skt.getInputStream());
                logger.info("receive a packet from server seq={}, code={}, type={}",
                        packet.getSeq(), packet.getCode(), packet.getType());
                handlePacket(packet);
                if (eventHandler != null) {
                    eventHandler.handleEvent(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
                noError = false;
            }
        }
    }

    private void handlePacket(TwoEatOnePackets.Packet packet) {
        switch (packet.getType()) {
            case CmdRsp: {
                handleRsp(packet);
                break;
            }
            case CmdPushNeedSync: {
                sendMsg(PacketGen.syncReq(gid));
                break;
            }
            case CmdPushUserInfo:{
                handlePushUserInfo(packet);
                break;
            }
            case CmdSyncGame:{
                updateGame(packet);
                break;
            }
            case CmdChessMove:
            case CmdEnterGame:
            case CmdPushToast:
            case CmdStartMatch:
            case CmdCancelMatch:
            case UnknownCommand:
            case UNRECOGNIZED:
            default:showRspError(packet);break;
        }
    }

    private void handlePushUserInfo(TwoEatOnePackets.Packet packet) {
        TwoEatOnePackets.PushUserInfo userInfo = packet.getContent().getUser();
        uid = userInfo.getUid();
        gid = userInfo.getGid();
    }

    private void handleRsp(TwoEatOnePackets.Packet packet) {
        manager.handleRsp(packet);
    }

    private void updateGame(TwoEatOnePackets.Packet packet) {
        TwoEatOnePackets.TwoEatOneGame game = packet.getContent().getGame();
        myRole = game.getBlackPlayer() == uid ? BLACK : WHITE;
        logger.info("game updated: \n{}", getGameInfo(game));
        chess.clear();
        chess.addAll(game.getChessList());
        logger.info("my role: {}", myRole);

        switch (game.getStatus()) {
            case None:{
                logger.info("game status: {}", game.getStatus());
                break;
            }
            case Init:{
                if (!enterGameSent) {
                    sendMsg(PacketGen.enterGame(gid), new SeqCallback() {
                        @Override
                        public void handleRsp(TwoEatOnePackets.Packet packet) {
                            enterGameSent = true;
                        }

                        @Override
                        public void onError(int code, String msg) {
                            logger.err("enter game error: {}, {}", code, msg);
                        }
                    });
                }
                break;
            }
            case Over: {
                enterGameSent = false;
                logger.info("game over, and winner is: {}", game.getWinner());
                break;
            }
            case Playing:{
                enterGameSent = false;
                if (myRole.equals(game.getNextRole())) {
                    ThreadUtil.get().clientThread(this::randomMoveChess);
                }
                break;
            }
            case UNRECOGNIZED:
            default:{
                logger.warn("unsupported game status: {}", game.getStatus());
                break;
            }
        }
    }

    private void randomMoveChess() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.err("error when pause {}", e);
        }
        Random random = new Random(System.currentTimeMillis());
        while (true) {
            int count = 0;
            for (Integer c:chess) {
                if (c.equals(myRole)) {
                    count++;
                }
            }
            int moveIndex = random.nextInt(count);
            int dir = random.nextInt(4);
            boolean moved = false;
            int findIndex = 0;
            for (int i = 0; i < chess.size(); i++) {
                Integer c = chess.get(i);
                if (c.equals(myRole)) {
                    if (findIndex != moveIndex) {
                        findIndex++;
                        continue;
                    }
                    findIndex++;
                    int row = i / 5;
                    int col = i % 5;
                    if (dir == 0) {
                        int targetIndex = (row - 1) * 5 + col;
                        if (targetIndex > 0 && chess.get(targetIndex).equals(NONE)) {
                            sendMsg(PacketGen.chessMove(gid, i, targetIndex));
                            moved = true;
                            break;
                        }
                    } else if (dir == 1) {
                        int targetIndex = (row + 1) * 5 + col;
                        if (targetIndex < 25 && chess.get(targetIndex).equals(NONE)) {
                            sendMsg(PacketGen.chessMove(gid, i, targetIndex));
                            moved = true;
                            break;
                        }
                    } else if (dir == 2) {
                        if (col > 0) {
                            int targetIndex = row * 5 + col - 1;
                            if (chess.get(targetIndex).equals(NONE)) {
                                sendMsg(PacketGen.chessMove(gid, i, targetIndex));
                                moved = true;
                                break;
                            }
                        }
                    } else if (dir == 3) {
                        if (col < 4) {
                            int targetIndex = row * 5 + col + 1;
                            if (chess.get(targetIndex).equals(NONE)) {
                                sendMsg(PacketGen.chessMove(gid, i, targetIndex));
                                moved = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (moved) {
                break;
            }
        }
    }

    private void showRspError(TwoEatOnePackets.Packet packet) {
        logger.err("error rsp packet type: {}", packet);
    }

    private void sendMsg(TwoEatOnePackets.Packet packet, SeqCallback callback) {
        try {
            ProtoIOHandler.sendMsg(skt.getOutputStream(), packet);
            manager.add(packet.getSeq(), callback);
            logger.info("send packet: {}", packet.getType());
        } catch (Exception e) {
            try {
                skt.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void sendMsg(TwoEatOnePackets.Packet packet) {
        sendMsg(packet, null);
    }

    private String getGameInfo(TwoEatOnePackets.TwoEatOneGame game) {
        StringBuilder sb = new StringBuilder();
        sb.append("G S: ").append(game.getStatus())
                .append("  next: ").append(game.getNextRole());
        for (int i = 0; i < game.getChessList().size(); i++) {
            if (i % 5 == 0) {
                sb.append("\n");
            }
            sb.append(" ");
            sb.append(game.getChess(i));
        }
        return sb.toString();
    }
}
