package domain;

/**
 * Excepción propia de DOPOSoccer.
 * Señala un error específico del juego, como un valor de
 * configuración inválido o un problema al guardar o abrir una
 * partida, con un mensaje claro y detallado para mostrárselo al jugador.
 */
public class DOPOSoccerException extends Exception {

    /**
     * Crea la excepción con el mensaje que describe el error ocurrido.
     * @param mensaje mensaje claro y detallado que describe el error ocurrido
     */
    public DOPOSoccerException(String mensaje){
        super(mensaje);
    }
}
