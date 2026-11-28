# Prueba de aceptación — Proyecto DOPOSoccer

**Integrantes:** Juan Sebastián Garavito Mejía · Laila Zareth Romano Guerrero
**Entrega:** GaravitoM-RomanoG

Juego de fútbol de cabezones para dos jugadores, o un jugador contra la máquina, hecho en Java Swing. El dominio, que tiene las reglas del juego, está separado de la presentación, que se encarga de las pantallas.

A continuación se recorre el proyecto **entrega por entrega**. En cada entrega se explica qué se pidió, qué implementamos, el paso a paso de la prueba (la pantalla o tecla que se usa, el valor que se escribe y lo que se espera) y por qué se comporta así.

Para ejecutarlo, desde la raíz del repositorio:

```
javac --release 8 -d build -cp "lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar;src" src/domain/*.java src/presentation/*.java
java -cp build presentation.GameGUI
```

En Linux o Mac el separador del classpath es `:` en vez de `;`. El script `run_tests.sh` compila, corre las pruebas y genera el reporte de cobertura.

---

## Entrega 1 — El dominio

### Qué se pidió
Modelar el juego: jugadores, balón, arcos, marcador, cronómetro, barra de poder, personajes con su poder y los dos tipos de partido (**por tiempo** y **por goles**), usando herencia y polimorfismo.

### Qué implementamos
`Player`, `Ball`, `Goal`, `Score`, `Timer` y `PowerBar`; la clase abstracta **`Match`** con sus dos modos, **`Time`** (gana quien tenga más goles al acabarse el tiempo, con muerte súbita si hay empate) y **`Goals`** (gana el primero que llega al número de goles); **`Character`**, con nombre, país, poder y apariencia; y la clase abstracta **`Power`** con los cuatro poderes.

### Paso a paso
Esta entrega no tiene pantalla propia: se verifica con las pruebas de unidad.

| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | `bash run_tests.sh` | Compila todo y JUnit muestra **`OK (159 tests)`** |
| 2 | Abrir `coverage-report/index.html` | La cobertura del paquete `domain` es de **74 %** de instrucciones y **58 %** de ramas |

### Por qué se comporta así
Todo el comportamiento vive en el dominio y se prueba sin interfaz. Las pruebas encontraron un error que jugando no se veía: la vida inicial era 90 y no 100, porque el constructor de `Player` calculaba la vida máxima antes de asignar `survival`.

---

## Entrega 2 — El juego jugable

### Qué se pidió
Que se pueda jugar: configurar el partido, elegir personaje, moverse, saltar, patear, cargar y usar el poder, pausar y terminar, con vida y aturdimiento, tres resoluciones y una excepción propia con registro de errores.

### Qué implementamos
Las pantallas `ConfigPanel`, `CharacterSelectPanel` y `FieldPanel`, con un ciclo de juego a unos 60 cuadros por segundo. Todo error del dominio lanza **`DOPOSoccerException`**: el jugador ve un mensaje claro y `GameLogger` guarda el detalle en `errores.log`.

### Paso a paso
Abrir el juego. Aparece la pantalla **Configuración del partido**.

| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | Escribir `abc` en *Minutos* y presionar **JUGAR** | No avanza y lanza **DOPOSoccerException** con *"El valor debe ser un numero entero valido."* |
| 2 | Escribir `0` y presionar **JUGAR** | Lanza **DOPOSoccerException** con *"El valor debe ser un numero mayor que 0. Escribiste: 0"* |
| 3 | Escribir `1`, rival *Humano* y presionar **JUGAR**; luego J1 = Corea del Sur, J2 = Camerún y **JUGAR** | Se abre la cancha con marcador 0 - 0, cronómetro en 1:00, barras de poder vacías y vida en 100/100 |
| 4 | J1 junto al balón presiona **S** | El balón sale hacia el arco rival; el rival no pierde vida |
| 5 | J1 pegado a J2, sin balón cerca, presiona **S** | J2 pierde 10 de vida |
| 6 | Repetir el paso 5 hasta que la vida de J2 llegue a 0 | La barra pasa de verde a amarilla y a roja; J2 queda **ATURDIDO** 3 segundos, parpadea en gris y no se mueve. Luego recupera toda la vida |
| 7 | Meter el balón en el arco de J2 | El marcador queda 1 - 0, todo vuelve al centro y la barra de poder de J2 sube un cuarto |
| 8 | **P** | El juego se detiene y aparece *PAUSA*; **P** otra vez lo reanuda |
| 9 | Con la barra llena, J1 presiona **Q** y toca el balón con J2 en la trayectoria | **Aura Azul:** el balón sale recto, azul y rápido. Al golpear a J2 le quita 40 de vida y lo empuja hacia su arco |
| 10 | Con la barra llena, J2 presiona **M** y toca el balón hacia J1 | **Rayo:** el balón sale amarillo y con chispas. Al golpear a J1 le quita 60 de vida y lo deja electrocutado 3 segundos (se mueve a la mitad y no salta) |
| 11 | Otro partido con Nigeria y Estados Unidos, usando el poder de cada uno | **Tornado:** el balón se ve como un remolino y deja al rival inconsciente 3 segundos. **Ilusión:** aparecen 7 balones señuelo iguales al real que desaparecen a los 2,5 segundos, y el rival golpeado queda con los controles invertidos |
| 12 | Dejar que el cronómetro llegue a 0:00 con 1 - 0 | Termina el partido: *"Gana el Jugador 1"*, con 100 puntos para J1 y 25 para J2 |
| 13 | En otro partido, llegar a 0:00 empatados | El partido no termina: sigue en muerte súbita hasta el siguiente gol |
| 14 | Repetir con resolución *Pequena* y *Grande* | La cancha y el marcador se ven completos y proporcionales en los tres tamaños |

### Por qué se comporta así
La presentación solo dibuja, lee el teclado y marca el ritmo; **las reglas las decide el dominio**. Por eso un poder definido en `Power` se ve en pantalla sin que `FieldPanel` sepa cómo funciona. El tiro de poder sale cuando el jugador con aura toca el balón, pero el daño y el efecto **solo se aplican si el tiro golpea al rival**: si entra al arco sin tocarlo, el gol cuenta sin efecto.

---

## Entrega 3 — Fachada, máquina y partidas guardadas

### Qué se pidió
Jugar contra la máquina con varios perfiles, guardar y abrir partidas, y que el juego no se pueda abrir dos veces.

### Qué implementamos
**`Game`** como fachada: es la única puerta de la presentación al partido. Las fábricas `CharacterFactory`, `MatchFactory` y `BotFactory`. Cuatro perfiles de máquina que heredan de `Bot`: **`Stupid`, `Offensive`, `Defensive` y `Expert`**. Guardar y abrir con `GamePersistence`. Y **`GameGUI`** como Singleton.

### Paso a paso

| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | **Abrir partida** y elegir un archivo de texto cualquiera | No se abre y lanza **DOPOSoccerException** con *"No se pudo abrir la partida: invalid stream header…"*; los puntos quedan intactos |
| 2 | **Abrir partida** con un nombre de archivo que no existe | Lanza **DOPOSoccerException** con *"No se pudo abrir la partida…"* y el mensaje de archivo no encontrado |
| 3 | En un partido de 2 minutos, anotar un gol y presionar **G**; guardar como `partida1.dat` | El juego se detiene y la partida queda guardada |
| 4 | **ESC**, **Menu**, **Abrir partida** y elegir `partida1.dat` | El partido sigue donde quedó: mismo marcador, tiempo y personajes |
| 5 | *Por goles* `2`, rival **Torpe (facil)** | La máquina se mueve, salta y patea al azar |
| 6 | Rival **Ofensiva (media)** | Persigue el balón, cabecea, patea y usa el poder apenas puede |
| 7 | Rival **Defensiva (media)** | Cuida su arco y solo sale cuando el balón se acerca |
| 8 | Rival **Experta (dificil)** | Ataca si va perdiendo o empatando, y defiende si va ganando |

### Por qué se comporta así
El bot **pide** patear y es `Game` quien ejecuta la patada por el mismo camino que la de un humano, así las reglas de daño no se duplican. Agregar un perfil de máquina es crear una subclase de `Bot` y añadir una rama en `BotFactory`. Al guardar se serializa el `Game` completo; los perfiles no van en la partida, y al abrirla se le asocian los perfiles actuales.

---

## Entrega 4 — Tienda de mejoras y progreso

### Qué se pidió
Ganar puntos en los partidos y gastarlos en mejorar **SPEED, KICK, JUMP y SURVIVAL**, con un precio que se duplica en cada nivel, y conservar el progreso entre partidas.

### Qué implementamos
**`Profile`** (puntos y niveles del 1 al 10, con +10 % por nivel y precios de 500, 1000, 2000…), **`ProfileStore`** (los perfiles de J1 y J2), **`Progress`** (guarda y abre el progreso en `progreso.dat`) y la pantalla **`ShopPanel`**. Se ganan 100 puntos por victoria y 25 por derrota o empate.

### Paso a paso

| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | Abrir **Mejorar estadisticas** con el progreso en cero | Dos columnas con 0 puntos, todo en nivel 1 y los botones deshabilitados con *"FALTAN 500 pts"* |
| 2 | Ganar 5 partidos con J1 (*Por goles* `1`) y volver a la tienda | J1 tiene 500 puntos y el botón dice *"MEJORAR - 500"* |
| 3 | Mejorar SPEED de J1 y confirmar | J1 queda con 0 puntos, SPEED en nivel 2 (+10 %) y el siguiente precio es 1000 |
| 4 | Cerrar el juego y volver a abrirlo | Los puntos y los niveles se conservan |
| 5 | Mejorar teniendo solo 25 puntos (prueba de unidad) | Lanza **DOPOSoccerException** con *"Puntos insuficientes para mejorar 'speed'. Cuesta 500 puntos y tienes 25. Faltan 475."* |

### Por qué se comporta así
El progreso vive en `Progress`, separado de la partida: la partida guarda el juego y el progreso guarda a los jugadores. La tienda le pregunta a `Progress` antes de dibujar cada botón, así nunca ofrece una mejora imposible. Por eso el error de puntos insuficientes solo se puede provocar desde las pruebas de unidad.
