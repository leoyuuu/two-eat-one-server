package com.leoyuu.teo.cj;

import com.leoyuu.teo.cj.window.Client;

import javax.swing.*;

public class JApp {
    public static void main(String[] args) {
        Client c = new Client();
        c.setVisible(true);
        c.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
