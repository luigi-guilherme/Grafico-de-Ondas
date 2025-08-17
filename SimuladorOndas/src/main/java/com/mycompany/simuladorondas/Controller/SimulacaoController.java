package com.mycompany.simuladorondas.Controller;

import javafx.scene.control.Alert.AlertType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.geometry.Side;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.mycompany.simuladorondas.Model.OndaBase;
import com.mycompany.simuladorondas.Model.OndaTransversal;
import com.mycompany.simuladorondas.Model.Simulacao;
import com.mycompany.simuladorondas.Model.SimulacaoDAO;
import com.mycompany.simuladorondas.Model.ValoresSimulacao;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * Controlador da simulação de ondas.
 * Gerencia a interação do usuário e atualiza o gráfico com base nos parâmetros fornecidos.
 */
public class SimulacaoController {
    @FXML
    private LineChart<Number, Number> graficoLinha;
    @FXML
    private NumberAxis eixoX;
    @FXML
    private NumberAxis eixoY;

    @FXML
    private TextField campoFrequencia;
    @FXML
    private TextField campoComprimentoOnda;
    @FXML
    private TextField campoTempoSimulacao;
    @FXML
    private TextField campoErroMaximo;
    @FXML
    private TextField campoPontoP;

    @FXML
    private Button botaoIniciar;
    @FXML
    private Button botaoMostrarHistorico;

    private XYChart.Series<Number, Number> serie;
    private XYChart.Series<Number, Number> seriePontoP;
    private static final double PASSO = 0.05;
    private OndaBase simulacao;
    private SimulacaoDAO simulacaoDAO;
    private int simulacaoID;
    private double tempoSimulacao;
    private double frequencia;
    private double comprimentoOnda;
    private double t;
    private double posicaoPontoP;
    private Stage janelaHistorico;

    // Método para injetar o SimulacaoDAO
    public void setSimulacaoDAO(SimulacaoDAO simulacaoDAO) {
        this.simulacaoDAO = simulacaoDAO;
    }

    @FXML
    public void initialize() {
        graficoLinha.getStylesheets().add(getClass().getResource("/com/mycompany/simuladorondas/style.css").toExternalForm());
        // Configurar o gráfico
        eixoX.setLabel("Posição (x)");
        eixoX.setAutoRanging(false);
        eixoX.setLowerBound(0);
        eixoX.setUpperBound(1.01);
        eixoX.setTickUnit(0.1);

        eixoY.setLabel("Deflexão (y)");
        eixoY.setAutoRanging(false);
        eixoY.setLowerBound(-1.5);
        eixoY.setUpperBound(1.5);
        eixoY.setTickUnit(0.5);

        graficoLinha.setTitle("Gráfico de Onda na Corda");
        graficoLinha.setAnimated(false);
        graficoLinha.setCreateSymbols(false);

        // Adicionar a primeira série (onda)
        serie = new XYChart.Series<>();
        serie.setName("Onda");
        graficoLinha.getData().add(serie);
        
        // Adicionar a série do ponto P
        seriePontoP = new XYChart.Series<>();
        seriePontoP.setName("Ponto P");
        graficoLinha.getData().add(seriePontoP);
        

        // Alterar os itens da legenda após renderização
        Platform.runLater(() -> {
            // Localizar os itens da legenda
            List<Node> legendItems = new ArrayList<>(graficoLinha.lookupAll(".chart-legend-item"));

            // Configurar o estilo da legenda "Onda" (primeira série)
            if (legendItems.size() > 0) {
                Node legendItemOnda = legendItems.get(0);
                Node symbolOnda = legendItemOnda.lookup(".chart-legend-item-symbol");
                if (symbolOnda != null) {
                    symbolOnda.setStyle("-fx-background-color: orangered; -fx-background-radius: 50%;");
                }
            }

            // Configurar o estilo da legenda "Ponto P" (segunda série)
            if (legendItems.size() > 1) {
                Node legendItemPontoP = legendItems.get(1);
                Node symbolPontoP = legendItemPontoP.lookup(".chart-legend-item-symbol");
                if (symbolPontoP != null) {
                    symbolPontoP.setStyle("-fx-background-color: blue; -fx-background-radius: 50%;");
                }
            }
        });
        
    


        // Posicionar a legenda na parte inferior
        graficoLinha.setLegendSide(Side.BOTTOM);

        // Aplicar o CSS personalizado na legenda
        graficoLinha.getStylesheets().add(getClass().getResource("/com/mycompany/simuladorondas/style.css").toExternalForm());
        graficoLinha.applyCss();

        // Configurar ações dos botões
        botaoIniciar.setOnAction(e -> iniciarSimulacao());
        botaoMostrarHistorico.setOnAction(e -> mostrarHistorico());

        // Adicionar funcionalidade para mover o foco para o próximo campo ao pressionar Enter
        campoFrequencia.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                campoComprimentoOnda.requestFocus();
            }
        });

        campoComprimentoOnda.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                campoTempoSimulacao.requestFocus();
            }
        });

        campoTempoSimulacao.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                campoErroMaximo.requestFocus();
            }
        });

        campoErroMaximo.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                campoPontoP.requestFocus();
            }
        });

        campoPontoP.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                botaoIniciar.requestFocus();
            }
        });

    }

    /**
     * Método para iniciar a simulação com os parâmetros fornecidos pelo usuário.
     */
    private void iniciarSimulacao() {
    try {
        // Verificar se o simulacaoDAO foi inicializado
        if (simulacaoDAO == null) {
            mostrarErroConexao();
            return;
        }

        // Obter os valores dos campos de entrada e converter para double
        try {
            frequencia = Double.parseDouble(campoFrequencia.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(AlertType.ERROR, "Erro de Entrada", 
                "O valor inserido para a frequência é inválido. Por favor, insira um número.");
            return;
        }

        try {
            comprimentoOnda = Double.parseDouble(campoComprimentoOnda.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(AlertType.ERROR, "Erro de Entrada", 
                "O valor inserido para o comprimento de onda é inválido. Por favor, insira um número.");
            return;
        }

        try {
            tempoSimulacao = Double.parseDouble(campoTempoSimulacao.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(AlertType.ERROR, "Erro de Entrada", 
                "O valor inserido para o tempo de simulação é inválido. Por favor, insira um número.");
            return;
        }

        double erroMaximo;
        try {
            erroMaximo = Double.parseDouble(campoErroMaximo.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(AlertType.ERROR, "Erro de Entrada", 
                "O valor inserido para o erro máximo é inválido. Por favor, insira um número.");
            return;
        }

        try {
            posicaoPontoP = Double.parseDouble(campoPontoP.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta(AlertType.ERROR, "Erro de Entrada", 
                "O valor inserido para a posição do ponto P é inválido. Por favor, insira um número.");
            return;
        }

        // Validar os parâmetros fornecidos
        if (frequencia <= 0 || frequencia >= 0.5 || comprimentoOnda <= 0.2 || comprimentoOnda >= 2.0 ||
                tempoSimulacao <= 1 || tempoSimulacao >= 10 || erroMaximo <= 0 ||
                posicaoPontoP < 0 || posicaoPontoP > 1) {
            mostrarAlerta(AlertType.WARNING, "Erro de Validação", 
                "Erro nos parâmetros fornecidos. Verifique os valores e tente novamente.");
            return;
        }

        // Inserir a simulação no banco de dados e obter o ID gerado
        simulacaoID = simulacaoDAO.inserirSimulacao(tempoSimulacao, erroMaximo);
        simulacaoDAO.inserirParametroInicial(simulacaoID, frequencia, comprimentoOnda);

        // Criar uma nova simulação de onda transversal
        simulacao = new OndaTransversal(erroMaximo);

        t = 0.0;
        iniciarAnimacao();
    } catch (SQLException ex) {
        mostrarAlerta(AlertType.ERROR, "Erro de Banco de Dados", 
            "Erro ao acessar o banco de dados: " + ex.getMessage());
        ex.printStackTrace();
    } catch (Exception ex) {
        mostrarAlerta(AlertType.ERROR, "Erro Desconhecido", 
            "Ocorreu um erro inesperado: " + ex.getMessage());
        ex.printStackTrace();
    }
}

private void mostrarAlerta(AlertType tipo, String titulo, String mensagem) {
    Alert alerta = new Alert(tipo);
    alerta.setTitle(titulo);
    alerta.setHeaderText(null);
    alerta.setContentText(mensagem);
    alerta.showAndWait();
}

    /**
     * Método para iniciar a animação da simulação.
     */
    private void iniciarAnimacao() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.05), e -> {
                    try {
                        atualizarGrafico(t);
                        t += PASSO;
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                })
        );

        int numCiclos = (int) (tempoSimulacao / PASSO);
        timeline.setCycleCount(numCiclos);
        timeline.play();
    }

    /**
     * Atualiza o gráfico com os novos valores calculados para o tempo t.
     * @param t Tempo atual da simulação.
     * @throws SQLException Se ocorrer um erro ao inserir os valores no banco de dados.
     */
    private void atualizarGrafico(double t) throws SQLException {
        // Limpar os dados das séries
        serie.getData().clear();
        seriePontoP.getData().clear();

        // Calcular os valores de y para cada x e atualizar o gráfico
        for (double x = 0; x <= 1.01; x += 0.01) {
            double y = simulacao.calcularY(frequencia, comprimentoOnda, x, t);
            serie.getData().add(new XYChart.Data<>(x, y));
            simulacaoDAO.inserirValorSimulacao(simulacaoID, x, t, y);
        }

        // Calcular e exibir o ponto P
        double yPontoP = simulacao.calcularY(frequencia, comprimentoOnda, posicaoPontoP, t);
        XYChart.Data<Number, Number> pontoPData = new XYChart.Data<>(posicaoPontoP, yPontoP);
        Circle marcador = new Circle(4, Color.BLUE);
        pontoPData.setNode(marcador);
        seriePontoP.getData().add(pontoPData);
    }

    /**
     * Exibe o histórico de simulações disponíveis para reprodução.
     */
    private void mostrarHistorico() {
        try {
            System.out.println("Botão Mostrar Histórico clicado.");

            if (simulacaoDAO == null) {
                mostrarErroConexao();
                return;
            }

            List<Simulacao> simulacoes = simulacaoDAO.listarSimulacoes();

            if (simulacoes.isEmpty()) {
                System.out.println("Nenhuma simulação encontrada.");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Histórico de Simulações");
                alert.setHeaderText(null);
                alert.setContentText("Nenhuma simulação encontrada no histórico.");
                alert.showAndWait();
                return;
            }

            janelaHistorico = new Stage();
            janelaHistorico.setTitle("Histórico de Simulações");

            // Criar a tabela para exibir as simulações
            TableView<Simulacao> tabela = new TableView<>();

            TableColumn<Simulacao, Integer> colunaID = new TableColumn<>("Simulação ID");
            colunaID.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSimulacaoID()).asObject());

            TableColumn<Simulacao, LocalDateTime> colunaDataHora = new TableColumn<>("Data e Hora");
            colunaDataHora.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDataHora()));

            tabela.getColumns().addAll(colunaID, colunaDataHora);
            tabela.getItems().addAll(simulacoes);

            // Criar o campo de entrada e o botão para reproduzir
            Label labelSimulacaoID = new Label("Simulação ID para Reproduzir:");
            TextField campoSimulacaoID = new TextField();
            Button botaoReproduzir = new Button("Reproduzir Simulação");
            botaoReproduzir.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");

            // Configurar ação do botão de reprodução
            botaoReproduzir.setOnAction(e -> {
                reproduzirSimulacao(campoSimulacaoID.getText());
                // Fechar o painel de histórico após iniciar a reprodução
                janelaHistorico.close();
            });

            // Organizar os componentes em um layout
            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));
            vbox.getChildren().addAll(tabela, labelSimulacaoID, campoSimulacaoID, botaoReproduzir);

            Scene scene = new Scene(vbox, 400, 400);
            janelaHistorico.setScene(scene);
            janelaHistorico.show();

        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao Mostrar Histórico");
            alert.setHeaderText(null);
            alert.setContentText("Ocorreu um erro ao tentar mostrar o histórico: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Reproduz uma simulação selecionada pelo usuário a partir do histórico.
     * @param simulacaoIdTexto Texto contendo o ID da simulação a ser reproduzida.
     */
    private void reproduzirSimulacao(String simulacaoIdTexto) {
        try {
            if (simulacaoDAO == null) {
                mostrarErroConexao();
                return;
            }

            int id = Integer.parseInt(simulacaoIdTexto);
            List<ValoresSimulacao> valores = simulacaoDAO.buscarValoresSimulacao(id);

            if (valores.isEmpty()) {
                System.out.println("Nenhum dado encontrado para Simulação ID " + id);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Reproduzir Simulação");
                alert.setHeaderText(null);
                alert.setContentText("Nenhum dado encontrado para Simulação ID " + id);
                alert.showAndWait();
                return;
            }

            // Definir o valor fixo para o Ponto P
            this.posicaoPontoP = 0.5; // Valor padrão

            // Continuar com a reprodução da simulação
            // Agrupar os dados por tempo
            Map<Double, List<ValoresSimulacao>> dadosPorTempo = valores.stream()
                    .collect(Collectors.groupingBy(ValoresSimulacao::getT));

            List<Double> temposOrdenados = new ArrayList<>(dadosPorTempo.keySet());
            Collections.sort(temposOrdenados);

            int numFrames = temposOrdenados.size();
            Iterator<Double> iteradorTempo = temposOrdenados.iterator();

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.05), e -> {
                        if (iteradorTempo.hasNext()) {
                            double tempoAtual = iteradorTempo.next();
                            List<ValoresSimulacao> valoresNoTempo = dadosPorTempo.get(tempoAtual);
                            atualizarGraficoComDados(valoresNoTempo);
                        }
                    })
            );

            timeline.setCycleCount(numFrames);
            timeline.play();

            // Fechar o painel de histórico após iniciar a reprodução
            if (janelaHistorico != null) {
                janelaHistorico.close();
            }

        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao Reproduzir Simulação");
            alert.setHeaderText(null);
            alert.setContentText("ID de simulação inválido.");
            alert.showAndWait();
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao Reproduzir Simulação");
            alert.setHeaderText(null);
            alert.setContentText("Ocorreu um erro ao buscar os dados da simulação: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Atualiza o gráfico com os dados fornecidos, normalmente usados ao reproduzir uma simulação.
     * @param valoresNoTempo Lista de valores da simulação em um determinado tempo.
     */
    private void atualizarGraficoComDados(List<ValoresSimulacao> valoresNoTempo) {
        // Limpar os dados das séries
        serie.getData().clear();
        seriePontoP.getData().clear();

        // Atualizar o gráfico com os novos valores
        for (ValoresSimulacao valor : valoresNoTempo) {
            double x = valor.getX();
            double y = valor.getY();
            serie.getData().add(new XYChart.Data<>(x, y));
            if (Math.abs(x - posicaoPontoP) < 1e-5) {
                XYChart.Data<Number, Number> pontoPData = new XYChart.Data<>(x, y);
                Circle marcador = new Circle(4, Color.BLUE);
                pontoPData.setNode(marcador);
                seriePontoP.getData().add(pontoPData);
            }
        }
    }

    /**
     * Exibe uma mensagem de erro indicando problemas na conexão com o banco de dados.
     */
    private void mostrarErroConexao() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Conexão");
        alert.setHeaderText(null);
        alert.setContentText("Não foi possível conectar ao banco de dados.");
        alert.showAndWait();
    }
}






