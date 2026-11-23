package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Guarda el progreso de los dos jugadores  en un archivo fijo,
 * para que los puntos y las estadisticas se conserven entre partidos.
 */
public class ProfileStore {
    private static final String FILE = "progreso.dat";
    private Profile left;
    private Profile right;

    public ProfileStore() {
        load();
    }

    public Profile getLeft()  { return left; }
    public Profile getRight() { return right; }

    /** Carga el progreso del archivo; si no existe o falla, arranca perfiles nuevos. */
    public void load() {
        File file = new File(FILE);
        if (!file.exists()) {
            left = new Profile();
            right = new Profile();
            return;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            left = (Profile) ois.readObject();
            right = (Profile) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            left = new Profile();
            right = new Profile();
        }
    }

    /** Guarda el progreso en el archivo. */
    public void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE));
            oos.writeObject(left);
            oos.writeObject(right);
            oos.close();
        } catch (IOException e) {
            System.out.println("No se pudo guardar el progreso: " + e.getMessage());
        }
    }
}