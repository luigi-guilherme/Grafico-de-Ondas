package com.mycompany.simuladorondas.Model;

/**
 * Classe abstrata que representa uma onda genérica.
 * Contém métodos e atributos comuns a todas as ondas.
 */
public abstract class OndaBase {
    protected double erroMaximo;

    /**
     * Construtor da classe OndaBase.
     * @param erroMaximo Erro máximo permitido no cálculo do seno.
     */
    public OndaBase(double erroMaximo) {
        this.erroMaximo = erroMaximo;
    }

    /**
     * Método abstrato para calcular o valor de y(x, t).
     * Deve ser implementado pelas subclasses.
     * @param frequencia Frequência da onda (Hz).
     * @param comprimentoOnda Comprimento de onda (m).
     * @param x Posição ao longo da corda (m).
     * @param t Tempo (s).
     * @return Valor de y(x, t).
     */
    public abstract double calcularY(double frequencia, double comprimentoOnda, double x, double t);

    /**
     * Calcula o seno aproximado de um ângulo usando a série de Taylor.
     * @param angulo Ângulo em radianos.
     * @return Valor aproximado do seno do ângulo.
     */
    protected double senoAproximado(double angulo) {
        angulo = normalizarAngulo(angulo);

        int k = 0;
        double P = 0;
        double R = 1;
        int aux = 0;

        while (R >= erroMaximo) {
            if (k % 2 != 0) {
                if (aux == 1) {
                    P -= Math.pow(angulo, k) / fatorial(k);
                    aux = 0;
                } else {
                    P += Math.pow(angulo, k) / fatorial(k);
                    aux = 1;
                }
            }
            k++;
            R = Math.pow(Math.abs(angulo), k + 1) / fatorial(k + 1);
        }
        return P;
    }

    // Método para normalizar o ângulo entre -π e π.
    private double normalizarAngulo(double angulo) {
        angulo = angulo % (2 * Math.PI);
        if (angulo > Math.PI) {
            angulo -= 2 * Math.PI;
        } else if (angulo < -Math.PI) {
            angulo += 2 * Math.PI;
        }
        return angulo;
    }

    // Calcula o fatorial de um número.
    private double fatorial(int limite) {
        double resultado = 1;
        for (int i = 1; i <= limite; i++) {
            resultado *= i;
        }
        return resultado;
    }
}

