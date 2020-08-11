package com.leoyuu.tto.match;

import com.leoyuu.proto.PacketGen;
import com.leoyuu.proto.TwoEatOnePackets;
import com.leoyuu.tto.client.Client;
import com.leoyuu.tto.game.GameManager;
import com.leoyuu.utils.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MatchManager {
    private Logger logger = new Logger("MatchManager");
    private static final MatchManager manager = new MatchManager();
    private final Set<Client> preparedClients = new HashSet<>();

    private MatchManager() { }

    public void clientStartMatch(Client client, TwoEatOnePackets.Packet packet) {
        logger.info("client start match {}", client.getUid());
        synchronized (preparedClients) {
            if (preparedClients.size() > 0) {
                Iterator<Client> ite = preparedClients.iterator();
                Client black = ite.next();
                client.sendMsg(PacketGen.rspOk(packet.getGid(), packet.getSeq()));
                if (black.getUid().equals(client.getUid())) {
                    logger.info("client {} match repeat", client.getUid());
                } else {
                    ite.remove();
                    GameManager.get().genGame(black, client);
                    logger.info("client {}, {} match success", black.getUid(), client.getUid());
                }
            } else {
                preparedClients.add(client);
                client.sendMsg(PacketGen.rspOk(packet.getGid(), packet.getSeq()));
            }
        }
    }

    public void clientCancelMatch(Client client, TwoEatOnePackets.Packet packet) {
        logger.info("client cancel match {}", client.getUid());
        synchronized (preparedClients) {
            boolean removed = preparedClients.removeIf((c -> c.getUid().equals(client.getUid())));
            if (removed) {
                client.sendMsg(PacketGen.rspOk(packet.getGid(), packet.getSeq()));
            } else {
                client.sendMsg(PacketGen.rspErr(packet.getGid(), packet.getSeq(), "当前没有在匹配中"));
            }
        }
    }

    public void clientConnectionError(Client client) {
        synchronized (preparedClients) {
            preparedClients.removeIf((c -> c.getUid().equals(client.getUid())));
        }
    }

    public static MatchManager get() {
        return manager;
    }
}
