package presentation;

import domain.DOPOSoccerException;
import domain.Game;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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
    private Image auraGif;   //aura animada

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

    /**
     * Crea la pantalla de la cancha para el partido dado, cargando la
     * imagen de la cancha y los sprites de ambos personajes, y pone a
     * correr el ciclo del juego a sesenta cuadros por segundo.
     * @param gui ventana principal que recibe la orden de mostrar la pantalla de fin
     * @param game partido que se va a jugar en esta pantalla
     * @param resW ancho de la ventana según la resolución elegida
     * @param resH alto de la ventana según la resolución elegida
     */
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
        //aura animada, ImageIcon reproduce el GIF
        auraGif = new ImageIcon("resources/aura_azul.gif").getImage();
        setPreferredSize(new Dimension(resW, resH));

        pressedKeys = new HashSet<>();
        addKeyListener(new KeyAdapter(){
            /**
             * Atiende cada tecla presionada: las teclas de acción
             * inmediata (pausar, terminar, usar poder, guardar) se
             * ejecutan al instante, y las demás quedan registradas
             * como presionadas para que el ciclo del juego las lea en
             * cada cuadro.
             * @param e evento de tecla presionada
             */
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
            /**
             * Quita la tecla soltada del conjunto de teclas presionadas.
             * @param e evento de tecla soltada
             */
            @Override
            public void keyReleased(KeyEvent e){
                pressedKeys.remove(e.getKeyCode());
            }
        });
        setFocusable(true);

        gameLoop = new Timer(16, new java.awt.event.ActionListener(){
            /**
             * Avanza un cuadro del juego: si el partido no está
             * pausado ni terminado, procesa las teclas presionadas y
             * le pide a la fachada que avance la física y las reglas;
             * siempre vuelve a dibujar la pantalla; y si el partido ya
             * terminó, detiene el ciclo y muestra la pantalla de fin.
             * @param e evento de cada disparo del reloj del ciclo del juego
             */
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e){
                if(!paused && !game.isFinished()){
                    processKeys();
                    game.update(16/1000.0);        // la fachada avanza un frame
                }
                repaint();
                if(game.isFinished()){
                    gameLoop.stop();
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

    /**
     * Dibuja toda la pantalla de la cancha en cada cuadro: escala el
     * dibujo lógico al tamaño real de la ventana y pinta la cancha,
     * los jugadores, el balón, los arcos, el marcador y la pantalla de
     * fin o de pausa, según corresponda.
     * @param g contexto gráfico sobre el que se dibuja el panel
     */
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

    /**
     * Pausa el juego, pide al jugador un archivo y guarda la partida
     * en él. Si ocurre un error al guardar, lo muestra al jugador y lo
     * registra en el log.
     */
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

    /**
     * Dibuja la imagen de la cancha, si se pudo cargar.
     * @param g contexto gráfico sobre el que se dibuja
     */
    private void drawField(Graphics g){
        if(fieldImage != null){
            g.drawImage(fieldImage, 0, 0, WIDTH, HEIGHT, this);
        }
    }

        /**
         * Dibuja a los dos jugadores en su posición actual, cada uno
         * mirando hacia el arco contrario.
         * @param g contexto gráfico sobre el que se dibuja
         */
        private void drawPlayers(Graphics g){
        // J1 mira a la derecha (frontDir = 1), J2 a la izquierda (frontDir = -1)
        drawCharacter(g, p1Head, p1Shoe,
                (int) game.getLeftPlayerX(), (int) game.getLeftPlayerY(),
                game.isLeftPlayerAura(), 1);
        drawCharacter(g, p2Head, p2Shoe,
                (int) game.getRightPlayerX(), (int) game.getRightPlayerY(),
                game.isRightPlayerAura(), -1);
    }

    /**
     * Dibuja un personaje en la posición dada: primero su aura si la
     * tiene activa, para que quede detrás, y luego su cabeza y su
     * zapato por separado.
     * @param g contexto gráfico sobre el que se dibuja
     * @param head imagen de la cabeza del personaje
     * @param shoe imagen del zapato del personaje
     * @param x posición horizontal del personaje
     * @param y posición vertical del personaje
     * @param aura verdadero si el personaje tiene el aura de su poder activa
     * @param frontDir dirección hacia la que mira el personaje, uno hacia la derecha o menos uno hacia la izquierda
     */
    private void drawCharacter(Graphics g, BufferedImage head, BufferedImage shoe, int x, int y, boolean aura, int frontDir){
        int d = PLAYER_DIAMETER;
        int bottom = y + d;          // el piso del jugador (abajo del hitbox)
        int cx = x + d / 2;          // centro horizontal

        //aura animada (GIF) primero, para que quede DETRAS del personaje
        if(aura && auraGif != null){
            int auraSize = (int)(d * 1.7);           // un poco mas grande que el jugador
            int auraX = cx - auraSize / 2;           // centrada en horizontal
            int auraY = (y + d / 2) - auraSize / 2;  // centrada en el cuerpo
            g.drawImage(auraGif, auraX, auraY, auraSize, auraSize, this);
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

    /**
     * Dibuja el balón en su posición actual.
     * @param g contexto gráfico sobre el que se dibuja
     */
    private void drawBall(Graphics g){
        int d = BALL_DIAMETER;
        int x = (int) game.getBallX();
        int y = (int) game.getBallY();
        g.setColor(Color.WHITE);
        g.fillOval(x, y, d, d);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, d, d);
    }

    /**
     * Dibuja los dos arcos de la cancha, con sus postes y sus travesaños.
     * @param g contexto gráfico sobre el que se dibuja
     */
    private void drawGoals(Graphics g){
        g.setColor(Color.WHITE);
        int grosor = 6;
        g.fillRect(LEFT_POST - grosor/2, GOAL_TOP, grosor, GOAL_BOTTOM - GOAL_TOP);
        g.fillRect(RIGHT_POST - grosor/2, GOAL_TOP, grosor, GOAL_BOTTOM - GOAL_TOP);
        g.fillRect(0, GOAL_TOP - grosor/2, LEFT_POST, grosor);
        g.fillRect(RIGHT_POST, GOAL_TOP - grosor/2, WIDTH - RIGHT_POST, grosor);
    }

    /**
     * Dibuja la barra superior del juego: el marcador, el tiempo
     * restante y las barras de poder y de vida de ambos jugadores.
     * @param g contexto gráfico sobre el que se dibuja
     */
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

    /**
     * Dibuja una barra con un fondo gris y un relleno según la
     * proporción dada, con un borde opcional alrededor.
     * @param g contexto gráfico sobre el que se dibuja
     * @param x posición horizontal de la barra
     * @param y posición vertical de la barra
     * @param w ancho total de la barra
     * @param h alto de la barra
     * @param ratio proporción llena de la barra, entre cero y uno
     * @param fill color de relleno de la barra
     * @param border verdadero para dibujar un borde alrededor de la barra
     */
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

    /**
     * Dibuja la pantalla de fin de partido sobre la cancha: el
     * resultado, el marcador final y los puntos obtenidos por cada jugador.
     * @param g contexto gráfico sobre el que se dibuja
     */
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

    /**
     * Dibuja la pantalla de pausa sobre la cancha, con la ayuda de las
     * teclas para reanudar o terminar el partido.
     * @param g contexto gráfico sobre el que se dibuja
     */
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