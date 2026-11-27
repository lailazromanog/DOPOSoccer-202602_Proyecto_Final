package domain;

/**
 * Mantiene el progreso de los dos jugadores (perfiles J1 y J2) durante la sesion.
 * El progreso se REINICIA cada vez que se arranca el juego: no se guarda en disco,
 * solo vive mientras la aplicacion esta abierta.
 */
public class ProfileStore {
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