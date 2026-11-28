package domain;

import java.io.Serializable;

/**
 * Mantiene el progreso de los dos jugadores (perfiles J1 y J2). Es
 * serializable para que Progress lo pueda guardar en disco y abrirlo
 * de nuevo al volver a iniciar el juego.
 */
public class ProfileStore implements Serializable {
    private Profile left;
    private Profile right;

    /**
     * Crea el almacén de progreso con un perfil nuevo para cada jugador.
     */
    public ProfileStore() {
        left = new Profile();
        right = new Profile();
    }

    /**
     * Entrega el perfil persistente del jugador izquierdo.
     * @return el perfil del jugador izquierdo
     */
    public Profile getLeft()  { return left; }

    /**
     * Entrega el perfil persistente del jugador derecho.
     * @return el perfil del jugador derecho
     */
    public Profile getRight() { return right; }
}