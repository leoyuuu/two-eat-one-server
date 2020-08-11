package com.leoyuu.tto.client;

import com.leoyuu.utils.Logger;
import com.leoyuu.utils.ThreadUtil;

import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientManager {
    private Logger logger = new Logger("ClientManager");
    private final Set<Client> clientsWatcher = new HashSet<>();
    private boolean running = false;
    private AtomicInteger uidGen = new AtomicInteger(1000);

    public void startWatch() {
        running = true;
        ThreadUtil.get().fixExecutor(this::clientWatch);
    }

    private void clientWatch() {
        while (running) {
            synchronized (clientsWatcher) {
                Iterator<Client> clientIterator = clientsWatcher.iterator();
                while (clientIterator.hasNext()) {
                    Client c = clientIterator.next();
                    if (c.selfError() || c.longTimeNoActive()) {
                        clientIterator.remove();
                        c.close();
                    } else if (c.needSync()) {
                        c.notifySync();
                    }
                }
            }
            try {
                Thread.sleep(3_000);
            } catch (Exception e) {
                logger.err("clientWatch error when sleep {}", e);
            }
        }
    }

    public ClientImp genClient(Socket skt) {
        ClientImp client = new ClientImp(skt, uidGen.getAndIncrement());
        synchronized (clientsWatcher) {
            clientsWatcher.add(client);
        }
        return client;
    }
}
