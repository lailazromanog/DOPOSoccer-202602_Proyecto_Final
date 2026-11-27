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

    
    /**
     * Crea la excepcion que avisa que una estadisitica ya llego a su nivel maximo y no se puede mejorar mas.
     * @param stat nombre de la estadistica que ya no se puede mejorar
     * @param maxLevel nivel maximo permitido para las estadisticas
     * @return la excepcion con el mensaje que explica la razon
     */
    public static DOPOSoccerException maxLevelReached(String stat, int maxLevel) {
        return new DOPOSoccerException("La estadistica '" + stat + "' ya esta en su maximo nivel: " + maxLevel + " y no se puede mejorar mas.");
    }
}
