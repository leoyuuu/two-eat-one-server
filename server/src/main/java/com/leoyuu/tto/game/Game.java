package com.leoyuu.tto.game;

import com.leoyuu.proto.PacketGen;
import com.leoyuu.proto.TwoEatOnePackets;
import com.leoyuu.tto.client.Client;
import com.leoyuu.utils.Logger;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

class Game {
    private static final int SIZE = 5;
    private static final Integer NONE = TwoEatOnePackets.ChessType.NoChess_VALUE;
    private static final Integer BLACK = TwoEatOnePackets.ChessType.BlackChess_VALUE;
    private static final Integer WHITE = TwoEatOnePackets.ChessType.WhiteChess_VALUE;
    private static final TwoEatOnePackets.GameStatus None =  TwoEatOnePackets.GameStatus.None;
    private static final TwoEatOnePackets.GameStatus Init = TwoEatOnePackets.GameStatus.Init;
    private static final TwoEatOnePackets.GameStatus Playing = TwoEatOnePackets.GameStatus.Playing;
    private static final TwoEatOnePackets.GameStatus Over = TwoEatOnePackets.GameStatus.Over;
    final Integer gid;
    private final Logger logger = new Logger("game");
    private final Integer[] chess = new Integer[25];
    private final Client blackClient;
    private final Client whiteClient;
    private Integer nextRole = BLACK;
    private Integer winner = NONE;
    private TwoEatOnePackets.GameStatus status = None;
    private boolean whiteEnterGame = false;
    private boolean blackEnterGame = false;
    private long overTime = -1;


    Game(int gid, Client blackClient, Client whiteClient) {
        this.gid = gid;
        this.blackClient = blackClient;
        this.whiteClient = whiteClient;
        initChess();
    }

    private void initChess() {
        for (int i = 0; i < SIZE * SIZE; i++) {
            chess[i] = NONE;
        }
        for (int i = 0; i < SIZE; i++) {
            chess[i] = BLACK;
            chess[i + (SIZE-1) * SIZE] = WHITE;
        }
    }

    void start() {
        blackClient.setGid(gid);
        whiteClient.setGid(gid);
        status = Init;
        notifySync();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                notifySomeOneNoEnter();
            }
        }, 10_000);
    }

    private void notifySomeOneNoEnter() {
        if (whiteEnterGame && blackEnterGame) {
            return;
        }
        status = Over;
        logger.info("some one no enter so game over");
        notifySync();
    }

    void enterGame(Client client, TwoEatOnePackets.Packet packet) {
        if (client.getUid().equals(whiteClient.getUid())) {
            whiteEnterGame = true;
            client.sendMsg(PacketGen.rspOk(packet.getGid(), packet.getSeq()));
        } else if (client.getUid().equals(blackClient.getUid())) {
            client.sendMsg(PacketGen.rspOk(packet.getGid(), packet.getSeq()));
            blackEnterGame = true;
        } else {
            client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "非游戏用户无法进入游戏"));
        }
        if (whiteEnterGame && blackEnterGame) {
            status = Playing;
            logger.info("players entered, game {} start", gid);
            notifySync();
        }
    }

    void moveChess(Client client, TwoEatOnePackets.Packet packet) {
        logger.info("current role: {}", nextRole);
        synchronized (chess) {
            if (status.equals(None)) {
                client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "游戏不存在，请重新匹配"));
            }
            if (status.equals(Over)) {
                client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "游戏已结束，请重新开始"));
                return;
            }
            if (status.equals(Init)) {
                client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "有玩家还未准备，请稍后再试"));
            }
            if (errMove(client, packet)) {
                return;
            }
            TwoEatOnePackets.ChessMoveContent move = packet.getContent().getMove();
            int fromIndex = move.getFromIndex();
            int toIndex = move.getToIndex();
            Integer shooter = nextRole;
            Integer target = nextRole.equals(BLACK) ? WHITE : BLACK;
            boolean hit = updateChess(chess, fromIndex, toIndex, shooter, target);
            if (hit) {
                int targetCount = 0;
                for (Integer c:chess) {
                    if (c.equals(target)) {
                        targetCount++;
                    }
                }
                if (targetCount < 2) {
                    winner = nextRole;
                    status = Over;
                    overTime = System.currentTimeMillis();
                }
            }
            nextRole = target;
            client.sendMsg(PacketGen.rspOk(packet.getGid(), packet.getSeq()));
            blackClient.notifySync();
            whiteClient.notifySync();
        }
    }

    static boolean updateChess(Integer[] chess, int fromIndex, int toIndex, Integer shooter, Integer target) {
        chess[toIndex] = chess[fromIndex];
        chess[fromIndex] = NONE;
        int toRow = toIndex / SIZE;
        int toCol = toIndex % SIZE;
        Integer[] line = new Integer[SIZE];
        System.arraycopy(chess, toRow * SIZE, line, 0, SIZE);
        int hitIndex = checkHit(line, shooter, target);
        if (hitIndex < 0) {
            for (int i = 0; i < SIZE; i++) {
                line[i] = chess[toCol+ i * SIZE];
            }
            hitIndex = checkHit(line, shooter, target);
            if (hitIndex >= 0) {
                chess[toCol + hitIndex * SIZE] = NONE;
                return true;
            }
        } else {
            chess[toRow*SIZE + hitIndex] = NONE;
            return true;
        }
        return false;
    }

    static int checkHit(Integer[] line, Integer shooter, Integer target) {
        int spaceCount = 0;
        for (Integer c:line) {
            if (!c.equals(shooter) && !c.equals(target)) {
                spaceCount++;
            }
        }
        // 空白区域长度为 2 才可
        if (spaceCount != 2) {
            return -1;
        }

        int preShooterIndex = -1;
        int nextShooterIndex = -1;
        int targetIndex = -1;
        for (int i = 0; i < line.length; i++) {
            Integer c = line[i];
            if (c.equals(shooter)) {
                if (preShooterIndex < 0) {
                    preShooterIndex = i;
                } else {
                    nextShooterIndex = i;
                    if (nextShooterIndex - preShooterIndex != 1) {
                        //放枪者连续
                        return -1;
                    }
                }
            } else if (c.equals(target)) {
                if (targetIndex < 0) {
                    targetIndex = i;
                } else {
                    // target 只有 1 个
                    return -1;
                }
            }
        }

        // target 紧挨着枪
        if (preShooterIndex - targetIndex == 1) {
            return targetIndex;
        }
        if (targetIndex - nextShooterIndex == 1) {
            return targetIndex;
        }
        return -1;
    }


    private boolean errMove(Client client, TwoEatOnePackets.Packet packet) {
        TwoEatOnePackets.ChessMoveContent move = packet.getContent().getMove();
        int fromIndex = move.getFromIndex();
        int toIndex = move.getToIndex();
        if (fromIndex < 0 || fromIndex >= SIZE * SIZE) {
            client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "不能移动"));
            logger.warn("error from index {} from uid {} ", move.getFromIndex(), client.getUid());
            return true;
        }
        if (toIndex < 0 || toIndex >= SIZE * SIZE) {
            client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "不能移动"));
            logger.warn("error to index {} from uid {} ", move.getToIndex(), client.getUid());
            return true;
        }
        int moveChess = chess[fromIndex];
        if (moveChess == BLACK && !blackClient.getUid().equals(client.getUid())){
            client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "不是你的棋子"));
            logger.warn("error move chess {} from uid {} ", move.getToIndex(), client.getUid());
            return true;
        }
        if (moveChess == WHITE && !whiteClient.getUid().equals(client.getUid())){
            client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "不是你的棋子"));
            logger.warn("error move chess {} from uid {} ", move.getToIndex(), client.getUid());
            return true;
        }
        if (moveChess != BLACK && moveChess != WHITE) {
            client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "棋子错误"));
            logger.warn("error chess {} from uid {} ", move.getToIndex(), client.getUid());
            return true;
        }
        if (!chess[toIndex].equals(NONE)) {
            client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "当前位置有棋子"));
            logger.warn("error to index {} from uid {} ", move.getToIndex(), client.getUid());
            return true;
        }
        return false;
    }

    void clientNotAlive(Client client) {
        status = Over;
        if (blackClient.getUid().equals(client.getUid())) {
            winner = WHITE;
            whiteClient.notifySync();
        } else if (whiteClient.getUid().equals(client.getUid())){
            winner = BLACK;
            blackClient.notifySync();
        }
        logger.info("client {} not alive, game over ", client.getUid());
    }

    void handleSync(Client client, TwoEatOnePackets.Packet syncPacket) {
        synchronized (chess) {
            Integer blackPlayer = blackClient.getUid();
            Integer whitePlayer = whiteClient.getUid();
            if (!blackClient.isAlive()) {
                blackPlayer = -1;
            }
            if (!whiteClient.isAlive()) {
                whitePlayer = -1;
            }
            TwoEatOnePackets.TwoEatOneGame.Builder builder = TwoEatOnePackets.TwoEatOneGame.newBuilder();
            builder.addAllChess(Arrays.asList(chess));
            builder.setBlackPlayer(blackPlayer);
            builder.setWhitePlayer(whitePlayer);
            builder.setNextRole(nextRole);
            builder.setWinner(winner);
            builder.setStatus(status);
            TwoEatOnePackets.Packet rsp = TwoEatOnePackets
                    .Packet
                    .newBuilder()
                    .setCode(0)
                    .setType(TwoEatOnePackets.CmdType.CmdSyncGame)
                    .setSeq(syncPacket.getSeq())
                    .setContent(TwoEatOnePackets.PacketContent.newBuilder().setGame(builder.build()))
                    .build();
            client.sendMsg(rsp);
            if (status.equals(Over)) {
                client.setGid(0);
            }
        }
    }

    boolean gameOver() {
        return status.equals(Over) && System.currentTimeMillis() - overTime > 10_000;
    }

    private void notifySync() {
        blackClient.sendMsg(PacketGen.pushNeedSync());
        whiteClient.sendMsg(PacketGen.pushNeedSync());
    }

    @Override
    public String toString() {
        return String.format("Game{gid:%d, black:%d, white:%d, status:%s}",
                gid, blackClient.getUid(), whiteClient.getUid(), status);
    }
}
