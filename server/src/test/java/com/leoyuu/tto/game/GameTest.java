package com.leoyuu.tto.game;

import org.junit.Assert;
import org.junit.Test;

public class GameTest {
    @Test
    public void testHit() {
        Assert.assertTrue(Game.checkHit(new Integer[]{1, 1, 0, 0, 2}, 1, 2) < 0);
        Assert.assertTrue(Game.checkHit(new Integer[]{1, 1, 2, 0, 2}, 1, 2) < 0);
        Assert.assertTrue(Game.checkHit(new Integer[]{0, 1, 2, 0, 2}, 1, 2) < 0);
        Assert.assertTrue(Game.checkHit(new Integer[]{0, 1, 2, 2, 0}, 1, 2) < 0);
        Assert.assertFalse(Game.checkHit(new Integer[]{0, 1, 1, 2, 0}, 1, 2) < 0);
        Assert.assertTrue(Game.checkHit(new Integer[]{1, 0, 1, 2, 0}, 1, 2) < 0);
        Assert.assertTrue(Game.checkHit(new Integer[]{1, 2, 2, 0, 0}, 2, 1) >= 0);
    }

    @Test
    public void testChess() {
        Integer[] chess = new Integer[] {
                1, 1, 1, 1, 1,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                2, 2, 2, 2, 2,
        };
        Assert.assertFalse(Game.updateChess(chess, 0, 5, 1, 2));
        printChess(chess);
        Assert.assertFalse(Game.updateChess(chess, 1, 6, 1, 2));
        printChess(chess);
        Assert.assertFalse(Game.updateChess(chess, 6, 11, 1, 2));
        printChess(chess);
        Assert.assertFalse(Game.updateChess(chess, 11, 10, 1, 2));
        printChess(chess);
        Assert.assertFalse(Game.updateChess(chess, 10, 15, 1, 2));
        printChess(chess);
        Assert.assertTrue(Game.updateChess(chess, 5, 10, 1, 2));
        printChess(chess);
        Assert.assertFalse(Game.updateChess(chess, 21, 16, 2, 1));
        printChess(chess);
        Assert.assertTrue(Game.updateChess(chess, 22, 17, 2, 1));
        printChess(chess);
    }

    private void printChess(Integer[] chess) {
        System.out.println("------------------------------");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.printf(" %d", chess[i * 5 + j]);
            }
            System.out.println();
        }
    }
}
