package com.leoyuu.teo.client;


public class App {
    private static Client client;
    public static void main(String[] args) {
        client = new Client();
        client.connect();
        client.startMatch();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            if (client != null) {
                client.disconnect();
            }
        }));
    }
}
