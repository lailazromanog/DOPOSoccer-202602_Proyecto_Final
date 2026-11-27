package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Guarda y abre partidas en archivos (serializacion de Java).
 *
 * La fachada no sabe de archivos; esta clase se
 * encarga de escribir y leer el objeto Game completo.
 */
public class GamePersistence {

    /**
     * Guarda el partido dado en el archivo dado, serializando el
     * objeto completo del partido.
     * @param game partido que se quiere guardar
     * @param file archivo donde se va a guardar el partido
     * @throws DOPOSoccerException si ocurre un error al escribir el archivo
     */
    public void save(Game game, File file) throws DOPOSoccerException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(game);
            oos.close();
        } catch (IOException e) {
            throw new DOPOSoccerException("No se pudo guardar la partida: " + e.getMessage());
        }
    }

    /**
     * Abre el partido guardado en el archivo dado, reconstruyendo el
     * objeto completo del partido a partir de su serialización.
     * @param file archivo del que se quiere abrir el partido
     * @return el partido reconstruido a partir del archivo
     * @throws DOPOSoccerException si ocurre un error al leer el archivo, o si el archivo no contiene una partida válida
     */
    public Game open(File file) throws DOPOSoccerException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Game game = (Game) ois.readObject();
            ois.close();
            return game;
        } catch (IOException e) {
            throw new DOPOSoccerException("No se pudo abrir la partida: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new DOPOSoccerException("El archivo no es una partida valida: " + e.getMessage());
        }
    }
}