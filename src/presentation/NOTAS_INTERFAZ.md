# Notas de interfaz — DOPOSoccer

Registro de técnicas de Java Swing usadas en la capa de presentación que
NO se habían visto en los laboratorios del curso, con su fuente.

## 1. Cargar y dibujar una imagen (la cancha)

- **Dónde:** `src/presentation/FieldPanel.java`
- **Qué se usó:**
  - `java.awt.image.BufferedImage` para guardar la imagen en memoria.
  - `javax.imageio.ImageIO.read(File)` para leer el archivo `resources/cancha.png`.
  - `java.awt.Graphics.drawImage(...)` dentro de `paintComponent` para pintarla.
- **Por qué:** la cancha se definió como una imagen de fondo en vez de dibujarse
  con figuras, para lograr el aspecto del estadio.
- **Fuente / referencia:**
  - Oracle, *The Java Tutorials – Reading/Loading an Image*:
    https://docs.oracle.com/javase/tutorial/2d/images/loadimage.html
  - Oracle, *The Java Tutorials – Drawing an Image*:
    https://docs.oracle.com/javase/tutorial/2d/images/drawimage.html
  - API: `ImageIO.read`, `Graphics.drawImage`.
- **Relación con lo ya visto:** es el mismo `paintComponent(Graphics g)` de los
  laboratorios (Snake, EcoSafari); el cambio es pintar una imagen en vez de
  `fillRect`/`drawLine`.


  ## 2. Dibujar texto y HUD (marcador, cronómetro, barras de poder)

- **Dónde:** `src/presentation/FieldPanel.java`, dentro de `paintComponent`
- **Qué se usó:**
  - `java.awt.Font` para definir tipografía, estilo y tamaño del texto.
  - `java.awt.FontMetrics` para medir el ancho de un string en píxeles
    y poder centrarlo horizontalmente en el panel.
  - `Graphics.drawString(texto, x, y)` para pintar texto en pantalla.
  - `new Color(r, g, b, alpha)` con cuatro parámetros para crear un color
    semitransparente (el fondo oscuro del HUD).
  - `String.format("%d:%02d", min, seg)` para formatear el cronómetro.
- **Por qué:** el marcador, el cronómetro y las barras de poder son información
  que el jugador necesita ver todo el tiempo encima de la cancha.
- **Fuente / referencia:**
  - Oracle, *The Java Tutorials – Drawing Text*:
    https://docs.oracle.com/javase/tutorial/2d/text/drawmulstring.html
  - Oracle, *The Java Tutorials – Measuring Text*:
    https://docs.oracle.com/javase/tutorial/2d/text/measuringtext.html
  - API: `Font`, `FontMetrics.stringWidth`, `Graphics.drawString`.
- **Relación con lo ya visto:** `fillRect` y `drawRect` ya se usaron en labs;
  lo nuevo es `Font`, `FontMetrics`, `drawString` para texto, y el cuarto
  parámetro de `Color` para transparencia.


  ## 3. Game loop, KeyListener y conexión con el dominio

- **Dónde:** `src/presentation/FieldPanel.java`
- **Qué se usó:**
  - `java.awt.event.KeyAdapter` y `java.awt.event.KeyEvent` para escuchar
    el teclado. Se usa `KeyAdapter` en vez de `KeyListener` porque nos
    permite sobreescribir solo `keyPressed` y `keyReleased` sin tener
    que implementar `keyTyped`.
  - `java.util.HashSet<Integer>` para guardar todas las teclas presionadas
    al mismo tiempo. Así dos jugadores pueden moverse a la vez en el
    mismo teclado.
  - `javax.swing.Timer` como game loop: cada 16 ms (≈60 FPS) lee las
    teclas, actualiza la física y llama `repaint()`. Es el mismo concepto
    del Timer del Snake.
  - `setFocusable(true)` para que el JPanel reciba eventos de teclado.
- **Por qué:** sin el game loop y el KeyListener, la pantalla es estática.
  El Timer convierte la imagen fija en un juego que se actualiza en
  tiempo real. El Set de teclas permite que ambos jugadores jueguen
  a la vez en el mismo teclado.
- **Fuente / referencia:**
  - Oracle, *The Java Tutorials – How to Write a Key Listener*:
    https://docs.oracle.com/javase/tutorial/uiswing/events/keylistener.html
  - Oracle, *The Java Tutorials – How to Use Swing Timers*:
    https://docs.oracle.com/javase/tutorial/uiswing/misc/timer.html
- **Relación con lo ya visto:** el Timer y el KeyListener son los mismos que
  se usaron en HungrySnake. Lo nuevo es el Set para multiples teclas
  simultáneas, y que `paintComponent` ahora lee posiciones del dominio
  en vez de usar valores fijos.

### Cambios al dominio para la conexión

Se hicieron ajustes mínimos a `Player` y `Ball` para que funcionen con
coordenadas de pantalla (píxeles):
- `Player`: se agregó `velocityY` y `update(groundY)` para la gravedad
  del salto, y se subió `speed` de 1 a 5 para que el movimiento sea visible.
- `Ball`: se bajó `gravity` de 9.8 a 0.4 (de metros/s² a píxeles/frame),
  y se agregaron setters y `bounceX()` para manejar rebotes en paredes.