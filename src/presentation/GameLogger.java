package presentation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Registro de errores para los programadores
 */
public class GameLogger {
    public static void error(String mensaje, Exception causa){
        try{
            FileWriter fw = new FileWriter("errores.log", true); //true: agregar al final el error
            PrintWriter out = new PrintWriter(fw);
            out.println(LocalDateTime.now() + " -ERROR " + mensaje);
            if(causa != null){
                out.println("   cause: " + causa.getMessage());
            }
            out.close();
        }catch(IOException io){
            System.out.println("No se pudo escribir el log " + io.getMessage());
        }
    }
}
