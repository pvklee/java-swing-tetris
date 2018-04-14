package com.kevin.tetris;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Board extends JPanel implements ActionListener {

    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 22;
    private static final int EXTRA_WIDTH = 3;
    private static final int TOTAL_WIDTH = BOARD_WIDTH+EXTRA_WIDTH;
    private static final int LIST = 0;
    private static final int HOLD = 1;
    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private JLabel statusBar;
    private Shape curPiece;
    private Shape holdPiece;
    private Shape[] nextPieces;
    private Tetrominoes[] board;
    private int holdCounter = 0;

    BufferedImage l;
    BufferedImage li;
    BufferedImage ml;
    BufferedImage s;
    BufferedImage sq;
    BufferedImage t;
    BufferedImage z;


    public Board(Tetris parent) {
        setFocusable(true);
        curPiece = new Shape();
        holdPiece = new Shape();
        nextPieces = new Shape[5];
        timer = new Timer(400, this); // timer for lines down
        statusBar = parent.getStatusBar();
        board = new Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
        setBackground(new Color(193, 203, 206));
        clearBoard();
        addKeyListener(new MyTetrisAdapter());

        try{
            l = ImageIO.read(new File("images/l.png"));
            li = ImageIO.read(new File("images/li.png"));
            ml = ImageIO.read(new File("images/ml.png"));
            s = ImageIO.read(new File("images/s.png"));
            sq = ImageIO.read(new File("images/sq.png"));
            t = ImageIO.read(new File("images/t.png"));
            z = ImageIO.read(new File("images/z.png"));
        } catch ( IOException e){
            e.printStackTrace();
        }
    }

    public int squareWidth() {
        return (int) getSize().getWidth() / TOTAL_WIDTH;
    }

    public int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    public Tetrominoes shapeAt(int x, int y) {
        return board[y * BOARD_WIDTH + x];
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Tetrominoes.NoShape;
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[y * BOARD_WIDTH + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece(LIST);
        }
    }

    // implemented FROM
    public void newPiece(int from) {
        if (from == LIST) {
            curPiece.setShape(nextPieces[0].getShape());
            updateNextPiecesList();
            holdCounter = 0;
        } else if (from == HOLD) {
            curPiece.setShape(holdPiece.getShape());
        }

        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY - 1)) {
            curPiece.setShape(Tetrominoes.NoShape);
            timer.stop();
            isStarted = false;
            statusBar.setText("Game Over");
        }
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1))
            pieceDropped();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece(LIST);
        } else {
            oneLineDown();
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
        Color color = shape.color;
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }

    private void drawSquareGrid(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRect(x, y, squareWidth(), squareHeight());
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }

    private void drawSquareNoGrid(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRect(x, y, squareWidth(), squareHeight());
        g.setColor(color.brighter());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);

                if (shape != Tetrominoes.NoShape) {
                    drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
                } else {
                    drawSquareGrid(g, j * squareWidth(), boardTop + i * squareHeight(), Color.WHITE);
                }
            }
            for(int j = 0; j < EXTRA_WIDTH; j++){
                drawSquareNoGrid(g, (j+BOARD_WIDTH) * squareWidth(),boardTop + i * squareHeight(), Color.WHITE);
            }
        }

        if (curPiece.getShape() != Tetrominoes.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(), curPiece.getShape());
            }
        }

        drawExtra(g);
    }

    public void start() {
        if (isPaused)
            return;

        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        clearBoard();
        fillNextPiecesList();
        newPiece(LIST);
        timer.start();
    }

    public void pause() {
        if (!isStarted)
            return;

        isPaused = !isPaused;

        if (isPaused) {
            timer.stop();
            statusBar.setText("Paused");
        } else {
            timer.start();
            statusBar.setText(String.valueOf(numLinesRemoved));
        }

        repaint();
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
                return false;

            if (shapeAt(x, y) != Tetrominoes.NoShape)
                return false;
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();

        return true;
    }


    private void removeFullLines() {

        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                ++numFullLines;

                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j) {
                        board[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                    }
                }
            }

            if (numFullLines > 0) {
                numLinesRemoved += numFullLines;
                statusBar.setText(String.valueOf(numLinesRemoved));
                isFallingFinished = true;
                curPiece.setShape(Tetrominoes.NoShape);
                repaint();
            }
        }
    }

    private void dropDown() {
        int newY = curY;

        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1))
                break;

            --newY;
        }

        pieceDropped();
    }

    // ADDITION


    private void hold() {
        if (holdCounter == 1) return;

        Shape tempShape = new Shape();

        tempShape.setShape(curPiece.getShape());
        if (holdPiece.getShape() == Tetrominoes.NoShape) {
            newPiece(LIST);
            holdPiece.setShape(tempShape.getShape());
        } else {
            newPiece(HOLD);
            holdPiece.setShape(tempShape.getShape());
        }

        holdCounter = 1;
    }

    private void fillNextPiecesList() {
        for (int i = 0; i < nextPieces.length; i++) {
            nextPieces[i] = new Shape();
            nextPieces[i].setRandomShape();
        }
    }

    private void updateNextPiecesList() {
        int length = nextPieces.length;
        Shape tempShape;
        tempShape = nextPieces[0];
        for (int i = 1; i < length; i++) {
            nextPieces[i - 1] = nextPieces[i];
        }
        nextPieces[length - 1] = tempShape;
        nextPieces[length - 1].setRandomShape();
    }

    private void drawExtra(Graphics g){
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
        int x = (BOARD_WIDTH) * squareWidth();

        g.setColor(Color.BLUE);
        g.setFont( new Font("Arial", Font.BOLD, squareWidth()/2));
        g.drawString(" -HOLD-", x, boardTop + squareHeight());

        Image tempImage = getShapeImage(holdPiece.getShape());
        g.drawImage(tempImage, x,boardTop + (squareHeight()), squareWidth()*3, squareHeight()*3, null);

        g.drawString(" -NEXT-", x , boardTop + 6*squareHeight());


        for(int i = 0; i < nextPieces.length; i++){
            tempImage = getShapeImage(nextPieces[i].getShape());
            g.drawImage(tempImage, x,boardTop + (i*3 + 6) * squareHeight(), squareWidth()*3, squareHeight()*3, null);
        }

    }

    private Image getShapeImage(Tetrominoes tetrominoes){
        switch (tetrominoes){
            case LShape:
                return l;
            case LineShape:
                return li;
            case MirroredLShape:
                return ml;
            case SShape:
                return s;
            case SquareShape:
               return sq;
            case TShape:
               return t;
            case ZShape:
               return z;
        }
        return null;
    }

    //---

    class MyTetrisAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent ke) {
            if (!isStarted || curPiece.getShape() == Tetrominoes.NoShape)
                return;

            int keyCode = ke.getKeyCode();

            if (keyCode == 'p' || keyCode == 'P')
                pause();

            if (isPaused)
                return;

            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                    tryMove(curPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(curPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_SHIFT:
                    tryMove(curPiece.rotateRight(), curX, curY);
                    break;
                case KeyEvent.VK_UP:
                    tryMove(curPiece.rotateLeft(), curX, curY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case KeyEvent.VK_CONTROL:
                    hold();
                    break;
                case KeyEvent.VK_DOWN:
                    oneLineDown();
                    break;
            }

        }
    }

}