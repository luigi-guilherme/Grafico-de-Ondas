package com.mycompany.simuladorondas.Model;

/**
 * Classe que representa os valores calculados da simulação em um determinado instante.
 */
public class ValoresSimulacao {
    private double x;
    private double y;
    private double t;

    /**
     * Construtor da classe ValoresSimulacao.
     * @param x Posição x ao longo da corda.
     * @param y Valor calculado de y(x, t).
     * @param t Tempo t da simulação.
     */
    public ValoresSimulacao(double x, double y, double t) {
        this.x = x;
        this.y = y;
        this.t = t;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getT() { return t; }
}

