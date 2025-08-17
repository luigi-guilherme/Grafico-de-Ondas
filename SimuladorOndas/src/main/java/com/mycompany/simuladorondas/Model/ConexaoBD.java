package com.mycompany.simuladorondas.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável por estabelecer a conexão com o banco de dados.
 */
public class ConexaoBD {
    /**
     * Obtém uma conexão com o banco de dados.
     * @return Objeto Connection se a conexão for bem-sucedida, null caso contrário.
     */
    public Connection obterConexao() {
        Connection conexao = null;

        try {
            // Dados de conexão
            String hostname = "localhost:1433";
            String sqlInstanceName = "NOTEBOOK_GUI"; // Substitua pelo nome da sua instância
            String sqlDatabase = "SIMULACOES";
            String sqlUser = "sa";
            String sqlPassword = "123456";

            // Carregar o driver JDBC
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Construir a URL de conexão
            String connectURL = "jdbc:sqlserver://" + hostname + ";instance=" + sqlInstanceName
                    + ";databaseName=" + sqlDatabase + ";encrypt=true;trustServerCertificate=true;";

            // Estabelecer a conexão
            conexao = DriverManager.getConnection(connectURL, sqlUser, sqlPassword);

            if (conexao != null) {
                System.out.println("Conexão bem-sucedida!");
            } else {
                System.out.println("Não foi possível conectar ao banco de dados.");
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC não encontrado.");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados.");
            e.printStackTrace();
        }
        return conexao;
    }
}

