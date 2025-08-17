package com.mycompany.simuladorondas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mycompany.simuladorondas.Controller.SimulacaoController;
import com.mycompany.simuladorondas.Model.ConexaoBD;
import com.mycompany.simuladorondas.Model.SimulacaoDAO;

public class App extends Application {

    @Override
    public void start(Stage stage) throws ClassNotFoundException, SQLException, IOException {
        // Conectar ao banco de dados
        ConexaoBD conexaoBD = new ConexaoBD();
        Connection conexao = conexaoBD.obterConexao();
        SimulacaoDAO simulacaoDAO = new SimulacaoDAO(conexao);

        // Carregar o layout FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/simuladorondas/simulacao_layout.fxml"));
        Parent root = loader.load();

        // Injetar o SimulacaoDAO no controlador
        SimulacaoController controller = loader.getController();
        controller.setSimulacaoDAO(simulacaoDAO);

        // Criar uma cena
        Scene scene = new Scene(root, 800, 600);

        // Carregar o CSS no nível da cena 
        scene.getStylesheets().add(getClass().getResource("/com/mycompany/simuladorondas/style.css").toExternalForm());

        // Configurar o palco
        stage.setTitle("Simulador de Ondas");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
  

    public static void main(String[] args) {
        launch();
    }
}