package domain;

/**
 * Creador de bots.
 *
 * Unico lugar que sabe que dificultades de maquina existen y que bot usa cada una.
 * Game le pide un bot segun la dificultad y no conoce las subclases concretas.
 *
 */
public class BotFactory {

    public Bot create(String difficulty) {
        if (difficulty.equals("facil")) {
            return new Stupid();
        } else if (difficulty.equals("dificil")) {
            return new Expert();
        } else {
            return new Offensive();   // medio (por defecto)
        }
    }
}