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

## Pantalla de configuración (ConfigPanel / GameGUI)

Técnicas de Java Swing usadas en la pantalla de configuración del partido
(elegir "por tiempo" o "por goles" y definir minutos/goles).

### JRadioButton + ButtonGroup
- **Qué es:** botones de opción (radio). El `ButtonGroup` los agrupa para que
  solo uno quede seleccionado a la vez (si marcas "Por goles", se desmarca
  "Por tiempo").
- **Para qué lo usé:** elegir el tipo de partido, que es una decisión
  excluyente (o tiempo o goles, no ambos).
- **Fuente:** Oracle — "How to Use Buttons, Check Boxes, and Radio Buttons"
  https://docs.oracle.com/javase/tutorial/uiswing/components/button.html#radiobutton

### JTextField + Integer.parseInt con try/catch
- **Qué es:** un campo de texto para escribir; `Integer.parseInt` convierte ese
  texto a número. El `try/catch` atrapa el error si lo escrito no es un número.
- **Para qué lo usé:** leer los minutos o los goles. Si el usuario escribe algo
  inválido, no se cae el programa: uso un valor por defecto (3 min o 5 goles).
- **Fuente:** Oracle — "How to Use Text Fields"
  https://docs.oracle.com/javase/tutorial/uiswing/components/textfield.html

### BoxLayout
- **Qué es:** un administrador de layout que apila los componentes en una sola
  columna (eje Y). `Box.createVerticalStrut(n)` mete espacios entre ellos.
- **Para qué lo usé:** ordenar título, opciones, campo y botón de arriba hacia
  abajo y centrados.
- **Fuente:** Oracle — "How to Use BoxLayout"
  https://docs.oracle.com/javase/tutorial/uiswing/layout/box.html

### setContentPane(...)
- **Qué es:** cambia todo el contenido de la ventana (JFrame) por otro panel,
  sin abrir una ventana nueva.
- **Para qué lo usé:** pasar de la pantalla de configuración a la cancha dentro
  de la misma ventana, cuando se da "JUGAR".
- **Fuente:** Oracle — "Using Top-Level Containers"
  https://docs.oracle.com/javase/tutorial/uiswing/components/toplevel.html

### requestFocusInWindow()
- **Qué es:** le pide el foco del teclado a un componente.
- **Para qué lo usé:** después de cambiar a la cancha con setContentPane, hay
  que pedir el foco para que el panel del juego reciba las teclas (A/D/W/S y las
  flechas). Si no, el teclado no responde.
- **Fuente:** Oracle — "How to Use the Focus Subsystem"
  https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html


## Pantalla de configuración — estética (ConfigPanel / RoundedButton)

### Botón pintado a mano (RoundedButton extends JButton)
- **Qué es:** un JButton al que le dibujo yo mismo el fondo, con esquinas
  redondeadas, en vez del botón gris rectangular por defecto de Swing.
- **Cómo:** en el constructor apago el fondo estándar con
  `setContentAreaFilled(false)`, `setBorderPainted(false)` y `setOpaque(false)`;
  luego en `paintComponent` dibujo un rectángulo redondeado con
  `g2.fillRoundRect(...)` y dejo que `super.paintComponent(g)` pinte el texto
  encima. Uso `RenderingHints.VALUE_ANTIALIAS_ON` para que el borde salga suave.
- **Para qué lo usé:** el botón "JUGAR", para que se vea llamativo (naranja,
  redondeado) como en la maqueta.
- **Fuente:** Oracle — "Performing Custom Painting"
  https://docs.oracle.com/javase/tutorial/uiswing/painting/

### GridBagLayout para centrar la tarjeta
- **Qué es:** un administrador de layout. Si le agrego un solo componente, lo
  deja centrado (horizontal y vertical) dentro del panel.
- **Para qué lo usé:** centrar la "tarjeta" oscura con los controles encima de
  la foto de fondo del estadio.
- **Fuente:** Oracle — "How to Use GridBagLayout"
  https://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html

### Imagen de fondo en un panel (repaso)
- **Qué es:** cargar una imagen con `ImageIO.read(...)` y dibujarla como fondo
  sobreescribiendo `paintComponent` con `g.drawImage(fondo, 0, 0, getWidth(),
  getHeight(), this)` (la estira al tamaño del panel).
- **Para qué lo usé:** la foto del estadio (`menu.png`) detrás de la
  configuración. Es la misma técnica que ya uso para la cancha.
- **Fuente:** Oracle — "How to Use Icons" / "Reading/Loading an Image"
  https://docs.oracle.com/javase/tutorial/2d/images/loadimage.html