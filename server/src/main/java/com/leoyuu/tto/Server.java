package com.leoyuu.tto;

import com.leoyuu.tto.client.ClientManager;
import com.leoyuu.utils.Logger;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private Logger logger = new Logger("server");
    private int port = 10009;
    private boolean run = false;
    private ClientManager clientManager = new ClientManager();

    public void start() {
        run = true;
        try {
            ServerSocket sskt = new ServerSocket(port);
            logger.info("server started with port: {}", port);
            clientManager.startWatch();
            while (run) {
                Socket skt = sskt.accept();
                clientManager.genClient(skt).handle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        run = false;
    }
}
