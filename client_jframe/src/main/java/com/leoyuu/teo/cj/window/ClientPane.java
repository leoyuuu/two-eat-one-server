package com.leoyuu.teo.cj.window;

import com.leoyuu.proto.TwoEatOnePackets;
import com.leoyuu.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class ClientPane extends JComponent implements MouseListener {
    private final Logger logger = new Logger("ClientPane");
    private Color bg = new Color(0xE9A614);
    private Color line = Color.BLACK;
    private Color white = Color.WHITE;
    private Color black = Color.BLACK;
    private Color selected = new Color(0xff24c572);
    private final int NONE = TwoEatOnePackets.ChessType.NoChess_VALUE;
    private final int BLACK = TwoEatOnePackets.ChessType.BlackChess_VALUE;
    private final int WHITE = TwoEatOnePackets.ChessType.WhiteChess_VALUE;
    private int myChessType = BLACK;
    private OnMoveListener listener;

    private ArrayList<ChessItem> chess = new ArrayList<>();
    private int selectedChess = -1;

    ClientPane() {
        for (int i = 0; i < 25; i++) {
            chess.add(new ChessItem(i, 0));
        }
        addMouseListener(this);
    }

    void setMyRole(int myRole) {
        this.myChessType = myRole;
    }

    void updateChess(ArrayList<Integer> chess) {
        if (this.chess.size() != chess.size()) {
            return;
        }
        for (int i = 0; i < chess.size(); i++) {
            this.chess.get(i).value = chess.get(i);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        logger.info("paintComponent, select index: {}", selectedChess);
        updateItemRect();
        paintBg(g);
        paintBoard(g);
        paintChess(g);
    }

    private void updateItemRect() {
        int space = getWidth() / 20;
        int l = space;
        int r = getWidth() - space;
        int t = space;
        int b = getHeight() - space;

        int w = r - l;
        int h = b - t;

        int sw = w / 4;
        int sh = h / 4;

        for (int i = 0; i < 25; i++) {
            ChessItem item = chess.get(i);
            int row = i / 5;
            int col = i % 5;

            int cx = sw * col + l;
            int cy = sh * row + t;
            item.rect.setBounds(cx - 10, cy - 10, 20, 20);
        }
    }

    private void paintBg(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        g.setColor(bg);
        g.fillRect(0, 0, width, height);
    }

    private void paintBoard(Graphics g) {
        g.setColor(line);
        int space = getWidth() / 20;
        int l = space;
        int r = getWidth() - space;
        int t = space;
        int b = getHeight() - space;

        int w = r - l;
        int h = b - t;

        int sw = w / 4;
        int sh = h / 4;
        for (int i = 0; i < 5; i++) {
            int x = l + sw * i;
            g.drawLine(x, t, x, b);
        }
        for (int i = 0; i < 5; i++) {
            int y = t + sh * i;
            g.drawLine(l, y, r, y);
        }
    }

    private void paintChess(Graphics g) {
        if (selectedChess >= 0 && selectedChess < chess.size()) {
            ChessItem c = chess.get(selectedChess);
            if (c.value.equals(myChessType)) {
                ChessItem chessItem = new ChessItem(c);
                chessItem.value = -1;
                paintChessItem(g, chessItem);
            }
        }
        for (ChessItem c:chess) {
            paintChessItem(g, c);
        }
    }

    private void paintChessItem(Graphics g, ChessItem item) {
        int type = item.value;
        Rectangle rect = item.rect;
        if (item.value < 0) {
            g.setColor(selected);
            g.fillOval(rect.x - 5, rect.y - 5, rect.width + 10, rect.height+10);
        } else if (type == WHITE) {
            g.setColor(white);
            g.fillOval(rect.x, rect.y, rect.width, rect.height);
        } else if (type == BLACK){
            g.setColor(black);
            g.fillOval(rect.x, rect.y, rect.width, rect.height);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (selectedChess >= 0) {
            for (ChessItem c:chess) {
                if (c.rect.contains(x, y)) {
                    if (c.value.equals(myChessType)) {
                        selectedChess = c.index;
                    } else if (c.value.equals(NONE)) {
                        c.value = myChessType;
                        chess.get(selectedChess).value = NONE;
                        selectedChess = -1;
                        if (listener != null) {
                            listener.onMove(selectedChess, c.index);
                        }
                    }
                    repaint();
                }
            }
        } else {
            for (ChessItem c:chess) {
                if (c.value.equals(myChessType) && c.rect.contains(x, y)) {
                    selectedChess = c.index;
                    repaint();
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private static class ChessItem {
        final int index;
        final Rectangle rect = new Rectangle();
        Integer value;


        ChessItem(ChessItem c) {
            this.index = c.index;
            this.rect.setBounds(c.rect);
            this.value = c.value;
        }

        ChessItem(int index, Integer value) {
            this.index = index;
            this.value = value;
        }
    }

    interface OnMoveListener {
        void onMove(int from, int to);
    }
}
