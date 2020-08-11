package com.leoyuu.teo.cj.window;

import com.leoyuu.proto.TwoEatOnePackets;
import com.leoyuu.teo.cj.net.EventHandler;
import com.leoyuu.teo.cj.net.NetHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Client extends JFrame implements EventHandler {
    private NetHandler handler = new NetHandler();

    private JButton connectButton;
    private JButton matchButton;
    private ClientPane chessBoard;

    public Client() throws HeadlessException {
        setTitle("客户端");
        setupMenu();
        connectButton = new JButton("开始连接");
        matchButton = new JButton("开始匹配");
        chessBoard = new ClientPane();
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.add(connectButton);
        panel.add(chessBoard);
        panel.add(matchButton);
        panel.setLayout(null);
        connectButton.setBounds(155 , 0, 145, 50);
        matchButton.setBounds(0 , 0, 145, 50);
        chessBoard.setBounds(0, 60, 300, 300);
        setSize(300, 400);
        setResizable(false);
        updateChess();
        connectButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (handler.isConnected()) {
                    handler.disconnect();
                    connectButton.setText("开始连接");
                } else {
                    handler.connect();
                    connectButton.setText("断开连接");
                }
            }
        });
        matchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (handler.isMatching()) {
                    handler.cancelMatch();
                    matchButton.setText("开始匹配");
                } else {
                    handler.startMatch();
                    matchButton.setText("停止匹配");
                }
            }
        });
        handler.setEventHandler(this);
    }

    private void updateChess() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(1);
        }
        for (int i = 5; i < 20;i++) {
            list.add(0);
        }
        for (int i = 20; i < 25; i++) {
            list.add(2);
        }
        chessBoard.updateChess(list);
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getFileMenu());
        setJMenuBar(menuBar);
    }

    private JMenu getFileMenu() {
        JMenu menu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        menu.add(openItem);
        return menu;
    }

    @Override
    public boolean handleEvent(TwoEatOnePackets.Packet packet) {
        if (packet.getType() == TwoEatOnePackets.CmdType.CmdSyncGame) {
            updateGame(packet);
            return true;
        }
        return false;
    }

    private void updateGame(TwoEatOnePackets.Packet packet) {
        TwoEatOnePackets.TwoEatOneGame game = packet.getContent().getGame();
        chessBoard.updateChess(new ArrayList<>(game.getChessList()));
    }
}
