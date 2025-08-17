package com.mycompany.simuladorondas.Model;

import java.sql.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Classe responsável pelo acesso aos dados da simulação no banco de dados.
 */
public class SimulacaoDAO {
    private Connection conexao;

    /**
     * Construtor da classe SimulacaoDAO.
     * @param conexao Conexão com o banco de dados.
     */
    public SimulacaoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /**
     * Insere uma nova simulação e retorna o ID gerado.
     * @param duracaoSegundos Duração da simulação em segundos.
     * @param erroMaximo Erro máximo permitido no cálculo.
     * @return ID da simulação inserida.
     * @throws SQLException Se ocorrer um erro ao inserir a simulação.
     */
    public int inserirSimulacao(double duracaoSegundos, double erroMaximo) throws SQLException {
        String sql = "{CALL InserirSimulacao(?, ?)}";
        try (CallableStatement stmt = conexao.prepareCall(sql)) {
            stmt.setBigDecimal(1, new BigDecimal(duracaoSegundos).setScale(2, RoundingMode.HALF_UP));
            stmt.setBigDecimal(2, new BigDecimal(erroMaximo).setScale(4, RoundingMode.HALF_UP));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("SimulacaoID");
            } else {
                throw new SQLException("Erro ao inserir simulação, nenhum ID gerado retornado.");
            }
        }
    }

    /**
     * Insere os parâmetros iniciais da simulação.
     * @param simulacaoID ID da simulação.
     * @param frequenciaHz Frequência em Hz.
     * @param comprimentoOnda Comprimento de onda.
     * @throws SQLException Se ocorrer um erro ao inserir os parâmetros.
     */
    public void inserirParametroInicial(int SimulacaoID, double FrequenciaHz, double ComprimentoOnda) throws SQLException {
        String sql = "{CALL InserirParametroInicial(?, ?, ?)}";
        try (CallableStatement stmt = conexao.prepareCall(sql)) {
            stmt.setInt(1, SimulacaoID);
            stmt.setBigDecimal(2, new BigDecimal(FrequenciaHz).setScale(3, RoundingMode.HALF_UP));
            stmt.setBigDecimal(3, new BigDecimal(ComprimentoOnda).setScale(2, RoundingMode.HALF_UP));
            stmt.executeUpdate();
        }
    }

    /**
     * Insere os valores calculados da simulação.
     * @param simulacaoID ID da simulação.
     * @param posicaoX Posição x ao longo da corda.
     * @param tempoT Tempo t da simulação.
     * @param valorY Valor calculado de y(x, t).
     * @throws SQLException Se ocorrer um erro ao inserir os valores.
     */
    public void inserirValorSimulacao(int simulacaoID, double posicaoX, double tempoT, double valorY) throws SQLException {
        String sql = "{CALL InserirValorSimulacao(?, ?, ?, ?)}";
        try (CallableStatement stmt = conexao.prepareCall(sql)) {
            stmt.setInt(1, simulacaoID);
            stmt.setBigDecimal(2, new BigDecimal(posicaoX).setScale(2, RoundingMode.HALF_UP));
            stmt.setBigDecimal(3, new BigDecimal(tempoT).setScale(3, RoundingMode.HALF_UP));
            stmt.setBigDecimal(4, new BigDecimal(valorY).setScale(5, RoundingMode.HALF_UP));
            stmt.executeUpdate();
        }
    }

    /**
     * Lista as simulações existentes no banco de dados.
     * @return Lista de simulações.
     * @throws SQLException Se ocorrer um erro ao listar as simulações.
     */
    public List<Simulacao> listarSimulacoes() throws SQLException {
        List<Simulacao> simulacoes = new ArrayList<>();
        String sql = "{CALL ListarSimulacoes()}";

        try (CallableStatement stmt = conexao.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int SimulacaoID = rs.getInt("SimulacaoID");
                Timestamp timestamp = rs.getTimestamp("DataHora");
                LocalDateTime dataHora = timestamp.toLocalDateTime();
                simulacoes.add(new Simulacao(SimulacaoID, dataHora));
            }
        }
        return simulacoes;
    }

    /**
     * Busca os valores de uma simulação específica.
     * @param simulacaoID ID da simulação.
     * @return Lista de valores da simulação.
     * @throws SQLException Se ocorrer um erro ao buscar os valores.
     */
    public List<ValoresSimulacao> buscarValoresSimulacao(int simulacaoID) throws SQLException {
        List<ValoresSimulacao> valores = new ArrayList<>();
        String sql = "{CALL BuscarValoresSimulacao(?)}";

        try (CallableStatement stmt = conexao.prepareCall(sql)) {
            stmt.setInt(1, simulacaoID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double t = rs.getDouble("t");

                    valores.add(new ValoresSimulacao(x, y, t));
                }
            }
        }
        return valores;
    }
}




