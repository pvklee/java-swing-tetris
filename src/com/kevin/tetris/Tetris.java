/**
 *  Based off of https://www.ssaurel.com/blog/learn-to-create-a-tetris-game-in-java-with-swing/
 *
 *  Implemented hold, list of next tetrominoes.
 */

package com.kevin.tetris;

import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

    private JLabel statusBar;

    public Tetris() {
        statusBar = new JLabel("0");
        add(statusBar, BorderLayout.SOUTH);
        Board board = new Board(this);
        add(board);
        board.start();
        setSize(260, 400);
        setTitle("My Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public JLabel getStatusBar() {
        return statusBar;
    }

    public static void main(String[] args) {
        Tetris myTetris = new Tetris();
        myTetris.setLocationRelativeTo(null);
        myTetris.setVisible(true);
    }

}