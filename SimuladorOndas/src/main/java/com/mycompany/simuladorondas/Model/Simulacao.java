package com.mycompany.simuladorondas.Model;

import java.time.LocalDateTime;

/**
 * Classe que representa uma simulação.
 */
public class Simulacao {
    private int simulacaoID;
    private LocalDateTime dataHora;

    /**
     * Construtor da classe Simulacao.
     * @param simulacaoID ID da simulação.
     * @param dataHora Data e hora da simulação.
     */
    public Simulacao(int simulacaoID, LocalDateTime dataHora) {
        this.simulacaoID = simulacaoID;
        this.dataHora = dataHora;
    }

    // Getters
    public int getSimulacaoID() { return simulacaoID; }
    public LocalDateTime getDataHora() { return dataHora; }
}

