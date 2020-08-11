package com.leoyuu.tto.game;

import com.leoyuu.proto.PacketGen;
import com.leoyuu.proto.TwoEatOnePackets;
import com.leoyuu.tto.client.Client;
import com.leoyuu.utils.Logger;
import com.leoyuu.utils.ThreadUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {
    private Logger logger = new Logger("GameManager");
    private static final GameManager manager = new GameManager();
    private final AtomicInteger gameId = new AtomicInteger(1000);
    private final ConcurrentHashMap<Integer, Game> gameInfoMap = new ConcurrentHashMap<>();

    private GameManager() {
        ThreadUtil.get().fixExecutor(this::clientWatch);
    }

    public void clientConnectionError(Client client) {
        synchronized (gameInfoMap) {
            Game game = gameInfoMap.get(client.getGid());
            if (game != null) {
                game.clientNotAlive(client);
            }
        }
    }

    private void clientWatch() {
        while (true) {
            try {
                Thread.sleep(30_000);
                synchronized (gameInfoMap) {
                    Set<Map.Entry<Integer, Game>> entrySet =  gameInfoMap.entrySet();
                    entrySet.removeIf((entry) -> {
                        boolean gameOver = entry.getValue().gameOver();
                        if (gameOver) {
                            logger.info("remove game because game over {}", entry.getValue());
                        }
                        return gameOver;
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clientEnterGame(Client client, TwoEatOnePackets.Packet packet) {
        synchronized (gameInfoMap) {
            Game game = gameInfoMap.get(client.getGid());
            if (game == null) {
                client.notifyNoGameError(packet.getSeq());
                logger.warn("clientEnterGame exists games: {}", gameInfoMap.keySet());
                return;
            }
            game.enterGame(client, packet);
        }
    }

    public void clientMoveChess(Client client, TwoEatOnePackets.Packet packet) {
        synchronized (gameInfoMap) {
            Game game = gameInfoMap.get(client.getGid());
            if (game == null) {
                client.notifyNoGameError(packet.getSeq());
                logger.warn("clientMoveChess exists games: {}", gameInfoMap.keySet());
                return;
            }
            game.moveChess(client, packet);
        }
    }

    public void syncGame(Client client, TwoEatOnePackets.Packet packet) {
        if (packet.getGid() == 0) {
            client.sendMsg(PacketGen.rspOk(packet.getGid(), packet.getSeq()));
            return;
        }
        synchronized (gameInfoMap) {
            Game game = gameInfoMap.get(packet.getGid());
            if (game == null) {
                client.notifyNoGameError(packet.getSeq());
                logger.warn("syncGame exists games: {}", gameInfoMap.keySet());
                return;
            }
            game.handleSync(client, packet);
        }
    }

    public void notifyClientNotAlive(Client client) {
        synchronized (gameInfoMap) {
            Game game = gameInfoMap.get(client.getGid());
            if (game == null) {
                logger.warn("notifyClientNotAlive exists games: {}", gameInfoMap.keySet());
                return;
            }
            game.clientNotAlive(client);
        }
    }

    public void genGame(Client black, Client white) {
        synchronized (gameInfoMap) {
            int id = gameId.incrementAndGet();
            Game game = new Game(id, black, white);
            gameInfoMap.put(game.gid, game);
            logger.info("gen game: {}, user:{}, {}", id, black.getUid(), white.getUid());
            game.start();
        }
    }

    public static GameManager get() {
        return manager;
    }
}
