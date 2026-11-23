package presentation;

import domain.DOPOSoccerException;
import domain.Game;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Pantalla de la cancha.
 *
 * Ahora SOLO se encarga de: dibujar, leer el teclado y marcar el ritmo (el
 * reloj). Toda la logica y la fisica estan en la fachada del dominio (Game):
 * por eso esta clase unicamente importa Game y le pide ordenes y datos.
 */
public class FieldPanel extends JPanel {

    private BufferedImage fieldImage;
    private Game game;   // la fachada: lo unico del dominio que usamos
    private GameGUI gui;

    // imagenes de los personajes (cabeza y zapato, por separado)
    private BufferedImage p1Head;
    private BufferedImage p1Shoe;
    private BufferedImage p2Head;
    private BufferedImage p2Shoe;

    // constantes de dibujo, tomadas de la fachada (el mundo logico es 769x450)
    private static final int WIDTH = Game.WIDTH;
    private static final int HEIGHT = Game.HEIGHT;
    private static final int PLAYER_DIAMETER = Game.PLAYER_DIAMETER;
    private static final int BALL_DIAMETER = Game.BALL_DIAMETER;
    private static final int LEFT_POST = Game.LEFT_POST;
    private static final int RIGHT_POST = Game.RIGHT_POST;
    private static final int GOAL_TOP = Game.GOAL_TOP;
    private static final int GOAL_BOTTOM = Game.GOAL_BOTTOM;

    private Set<Integer> pressedKeys;
    private Timer gameLoop;
    private boolean paused = false;

    public FieldPanel(GameGUI gui, Game game, int resW, int resH) {
        this.gui = gui;
        this.game = game;
        try {
            fieldImage = ImageIO.read(new File("resources/cancha.png"));
        } catch (IOException e) {
            System.out.println("No se pudo cargar la cancha: " + e.getMessage());
        }

        //carga los sprites segun la clave de cada jugador (la da la apariencia del dominio)
        String sprite1 = game.getLeftPlayerSprite();
        String sprite2 = game.getRightPlayerSprite();
        try {
            p1Head = ImageIO.read(new File("resources/personajes/cabeza/" + sprite1 + "_derecha.png"));
            p1Shoe = ImageIO.read(new File("resources/personajes/zapato/" + sprite1 + "_derecha.png"));
            p2Head = ImageIO.read(new File("resources/personajes/cabeza/" + sprite2 + "_izquierda.png"));
            p2Shoe = ImageIO.read(new File("resources/personajes/zapato/" + sprite2 + "_izquierda.png"));
        } catch (IOException e) {
            System.out.println("No se pudieron cargar los personajes: " + e.getMessage());
        }
        setPreferredSize(new Dimension(resW, resH));

        pressedKeys = new HashSet<>();
        addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                int code = e.getKeyCode();
                if(code == KeyEvent.VK_P){
                    paused = !paused;              // P: pausar / reanudar
                }else if(code == KeyEvent.VK_ESCAPE){
                    game.finishMatch();            // ESC: terminar el partido
                }else if(code == KeyEvent.VK_Q){
                    game.usePowerLeftPlayer();      // Q: poder J1
                }else if(code == KeyEvent.VK_M){
                    game.usePowerRightPlayer();  // M: poder J2
                }else if(code == KeyEvent.VK_G){
                    saveGame();    // G: guardar la partida
                }else{
                    pressedKeys.add(code);
                }
            }
            @Override
            public void keyReleased(KeyEvent e){
                pressedKeys.remove(e.getKeyCode());
            }
        });
        setFocusable(true);

        gameLoop = new Timer(16, new java.awt.event.ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e){
                if(!paused && !game.isFinished()){
                    processKeys();
                    game.update(16/1000.0);        // la fachada avanza un frame
                }
                repaint();
                if(game.isFinished()){
                    gameLoop.stop();
                    gui.saveProgress(); //guarda puntos o estadisticas al terminar
                    gui.showEndScreen(game); //pantalla de fin con navegación
                }
            }
        });
        gameLoop.start();
    }

    /** Lee las teclas presionadas y le manda las ordenes a la fachada. */
    private void processKeys(){
        // Jugador 1: A/D/W/S
        if(pressedKeys.contains(KeyEvent.VK_A)){ game.moveLeftPlayerLeft(); }
        if(pressedKeys.contains(KeyEvent.VK_D)){ game.moveLeftPlayerRight(); }
        if(pressedKeys.contains(KeyEvent.VK_W)){ game.jumpLeftPlayer(); }
        if(pressedKeys.contains(KeyEvent.VK_S)){
            game.kickLeftPlayer();
            pressedKeys.remove(KeyEvent.VK_S);     // patear es one-shot
        }
        // Jugador 2: flechas
        if(pressedKeys.contains(KeyEvent.VK_LEFT)){  game.moveRightPlayerLeft(); }
        if(pressedKeys.contains(KeyEvent.VK_RIGHT)){ game.moveRightPlayerRight(); }
        if(pressedKeys.contains(KeyEvent.VK_UP)){    game.jumpRightPlayer(); }
        if(pressedKeys.contains(KeyEvent.VK_DOWN)){
            game.kickRightPlayer();
            pressedKeys.remove(KeyEvent.VK_DOWN);
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        // escala el dibujo 769x450 (logico) al tamaño real de la ventana
        Graphics2D g2 = (Graphics2D) g;
        g2.scale(getSize().width / (double) WIDTH, getSize().height / (double) HEIGHT);
        drawField(g);
        drawPlayers(g);
        drawBall(g);
        drawGoals(g);
        drawHUD(g);
        if(game.isFinished()){
            drawMatchEnd(g);
        }else if(paused){
            drawPaused(g);
        }
    }

    /** Pausa, pide un archivo y guarda la partida. */
    private void saveGame(){
        paused = true;   // que no avance mientras elijo el archivo
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);
        if(result == JFileChooser.APPROVE_OPTION){
            try {
                game.saveTo(chooser.getSelectedFile());
            } catch (DOPOSoccerException ex) {
                GameLogger.error("No se pudo guardar la partida", ex);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        paused = false;
        requestFocusInWindow();
    }

    private void drawField(Graphics g){
        if(fieldImage != null){
            g.drawImage(fieldImage, 0, 0, WIDTH, HEIGHT, this);
        }
    }

        private void drawPlayers(Graphics g){
        // J1 mira a la derecha (frontDir = 1), J2 a la izquierda (frontDir = -1)
        drawCharacter(g, p1Head, p1Shoe,
                (int) game.getLeftPlayerX(), (int) game.getLeftPlayerY(),
                game.isLeftPlayerAura(), 1);
        drawCharacter(g, p2Head, p2Shoe,
                (int) game.getRightPlayerX(), (int) game.getRightPlayerY(),
                game.isRightPlayerAura(), -1);
    }

    /** Dibuja un personaje: aura (atras) + cabeza + zapato, por separado. */
    private void drawCharacter(Graphics g, BufferedImage head, BufferedImage shoe, int x, int y, boolean aura, int frontDir){
        int d = PLAYER_DIAMETER;
        int bottom = y + d;          // el piso del jugador (abajo del hitbox)
        int cx = x + d / 2;          // centro horizontal

        // aura primero, para que quede DETRAS del personaje
        if(aura){
            g.setColor(new Color(80, 180, 255, 150));
            g.fillOval(x - 8, y - 8, d + 16, d + 16);
        }

        // cabeza: un poco mas grande que el hitbox (estilo cabezon)
        if(head != null){
            int headW = 66;
            int headH = headW * head.getHeight() / head.getWidth();
            int headX = cx - headW / 2;
            int headY = bottom - headH - 4;
            g.drawImage(head, headX, headY, headW, headH, this);
        }

        // zapato: pegado al piso, corrido un poco hacia el frente
        if(shoe != null){
            int shoeW = 44;
            int shoeH = shoeW * shoe.getHeight() / shoe.getWidth();
            int shoeX = cx - shoeW / 2 + frontDir * 8;
            int shoeY = bottom - shoeH;
            g.drawImage(shoe, shoeX, shoeY, shoeW, shoeH, this);
        }
    }

    private void drawBall(Graphics g){
        int d = BALL_DIAMETER;
        int x = (int) game.getBallX();
        int y = (int) game.getBallY();
        g.setColor(Color.WHITE);
        g.fillOval(x, y, d, d);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, d, d);
    }

    private void drawGoals(Graphics g){
        g.setColor(Color.WHITE);
        int grosor = 6;
        g.fillRect(LEFT_POST - grosor/2, GOAL_TOP, grosor, GOAL_BOTTOM - GOAL_TOP);
        g.fillRect(RIGHT_POST - grosor/2, GOAL_TOP, grosor, GOAL_BOTTOM - GOAL_TOP);
        g.fillRect(0, GOAL_TOP - grosor/2, LEFT_POST, grosor);
        g.fillRect(RIGHT_POST, GOAL_TOP - grosor/2, WIDTH - RIGHT_POST, grosor);
    }

    private void drawHUD(Graphics g){
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, WIDTH, 58);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String scoreText = game.getLeftScore() + " - " + game.getRightScore();
        int sw = fm.stringWidth(scoreText);
        g.drawString(scoreText, (WIDTH - sw) / 2, 28);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics fmSmall = g.getFontMetrics();
        g.drawString("J1", WIDTH/2 - sw/2 - fmSmall.stringWidth("J1") - 8, 28);
        g.drawString("J2", WIDTH/2 + sw/2 + 8, 28);

        int t = game.getTimeLeft();
        String timeText = String.format("%d:%02d", t/60, t%60);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fmTime = g.getFontMetrics();
        g.drawString(timeText, WIDTH - fmTime.stringWidth(timeText) - 15, 28);

        int barW = 100, barY = 34;
        int barX1 = 15;
        int barX2 = WIDTH - barW - 15;
        // barras de poder (naranja, con borde)
        drawBar(g, barX1, barY, barW, 10, game.getLeftPlayerPower(), new Color(0xE8A033), true);
        drawBar(g, barX2, barY, barW, 10, game.getRightPlayerPower(), new Color(0xE8A033), true);
        // barras de vida (verde)
        drawBar(g, barX1, 47, barW, 6, game.getLeftPlayerLife(), new Color(0x3ddc84), false);
        drawBar(g, barX2, 47, barW, 6, game.getRightPlayerLife(), new Color(0x3ddc84), false);
    }

    /** Dibuja una barra (fondo gris + relleno segun el porcentaje). */
    private void drawBar(Graphics g, int x, int y, int w, int h, double ratio, Color fill, boolean border){
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, w, h);
        g.setColor(fill);
        g.fillRect(x, y, (int)(w * ratio), h);
        if(border){
            g.setColor(Color.WHITE);
            g.drawRect(x, y, w, h);
        }
    }

    private void drawMatchEnd(Graphics g){
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        int w = game.getWinner();
        String result;
        if(w == 1){
            result = "Gana Jugador 1";
        }else if(w == 2){
            result = "Gana Jugador 2";
        }else{
            result = "EMPATE";
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(result, (WIDTH - fm.stringWidth(result))/2, HEIGHT/2 - 20);

        g.setFont(new Font("Arial", Font.BOLD, 28));
        String scoreText = game.getLeftScore() + " - " + game.getRightScore();
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(scoreText, (WIDTH - fm2.stringWidth(scoreText))/2, HEIGHT/2 + 30);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String puntos = "Puntos   J1: " + game.getLeftPlayerPoints() + "    J2: " + game.getRightPlayerPoints();
        FontMetrics fm3 = g.getFontMetrics();
        g.drawString(puntos, (WIDTH - fm3.stringWidth(puntos))/2, HEIGHT/2 + 72);
    }

    private void drawPaused(Graphics g){
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 44));
        FontMetrics fm = g.getFontMetrics();
        String text = "PAUSA";
        g.drawString(text, (WIDTH - fm.stringWidth(text))/2, HEIGHT/2 - 10);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        FontMetrics fm2 = g.getFontMetrics();
        String ayuda = "P: reanudar    ESC: terminar";
        g.drawString(ayuda, (WIDTH - fm2.stringWidth(ayuda))/2, HEIGHT/2 + 30);
    }
}