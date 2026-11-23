package domain;

/**
 * Mantiene el progreso de los dos jugadores (perfiles J1 y J2) durante la sesion.
 * El progreso se REINICIA cada vez que se arranca el juego: no se guarda en disco,
 * solo vive mientras la aplicacion esta abierta.
 */
public class ProfileStore {
    private Profile left;
    private Profile right;

    public ProfileStore() {
        left = new Profile();
        right = new Profile();
    }

    public Profile getLeft()  { return left; }
    public Profile getRight() { return right; }
}