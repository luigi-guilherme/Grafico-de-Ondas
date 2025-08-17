package com.mycompany.simuladorondas.Model;

/**
 * Classe que representa uma onda transversal.
 * Implementa o cálculo específico de y(x, t) para ondas transversais.
 */
public class OndaTransversal extends OndaBase {

    /**
     * Construtor da classe OndaTransversal.
     * @param erroMaximo Erro máximo permitido no cálculo do seno.
     */
    public OndaTransversal(double erroMaximo) {
        super(erroMaximo);
    }

    /**
     * Implementação do método calcularY para ondas transversais.
     * @param frequencia Frequência da onda (Hz).
     * @param comprimentoOnda Comprimento de onda (m).
     * @param x Posição ao longo da corda (m).
     * @param t Tempo (s).
     * @return Valor de y(x, t).
     */
    @Override
    public double calcularY(double frequencia, double comprimentoOnda, double x, double t) {
        double equacao = 2 * Math.PI * (frequencia * t - x / comprimentoOnda);
        return senoAproximado(equacao);
    }
}
