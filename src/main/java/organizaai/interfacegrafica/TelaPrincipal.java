package organizaai.interfacegrafica;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import organizaai.gerenciamento.GerenciadorAgenda;
import organizaai.modelo.Atividade;
import organizaai.strategy.OrdenacaoPorCategoria;
import organizaai.strategy.OrdenacaoPorData;
import organizaai.strategy.OrdenacaoPorPrioridade;

/**
 * Representa a janela principal do sistema, contendo a lista de compromissos 
 * e os botões de controle gerais (filtros, adicionar, concluir, excluir).
 */
public class TelaPrincipal {

    private BorderPane root;
    private GerenciadorAgenda gerenciador;
    private ListView<Atividade> listViewAtividades;

    /**
     * Constroi a tela principal ligando a ao gerenciador.
     * 
     * @param gerenciador A instância central que controla as regras de negócio.
     */
    public TelaPrincipal(GerenciadorAgenda gerenciador) {
        this.gerenciador = gerenciador;
        this.root = new BorderPane();
        this.root.setPadding(new Insets(15));

        inicializarComponentes();
        atualizarLista();
    }

    /**
     * Retorna o painel raiz desta tela
     * @return BorderPane principal.
     */
    public BorderPane getRoot() {
        return root;
    }

    /**
     * Inicializa os botões, listas e o ComboBox do Padrão Strategy.
     */
    private void inicializarComponentes() {
        // ComboBox de Estratégias de Ordenação
        HBox painelTopo = new HBox(10);
        painelTopo.setPadding(new Insets(0, 0, 15, 0));
        
        Label lblFiltro = new Label("Ordenar por:");
        ComboBox<String> comboOrdenacao = new ComboBox<>();
        comboOrdenacao.getItems().addAll("Data", "Prioridade", "Categoria");
        comboOrdenacao.setValue("Data"); // Valor padrão
        
        // Aplicação visual do strategy conforme a escolha do usuário
        comboOrdenacao.setOnAction(e -> {
            String escolha = comboOrdenacao.getValue();
            switch (escolha) {
                case "Data":
                    gerenciador.setEstrategiaOrdenacao(new OrdenacaoPorData());
                    break;
                case "Prioridade":
                    gerenciador.setEstrategiaOrdenacao(new OrdenacaoPorPrioridade());
                    break;
                case "Categoria":
                    gerenciador.setEstrategiaOrdenacao(new OrdenacaoPorCategoria());
                    break;
            }
            atualizarLista();
        });

        painelTopo.getChildren().addAll(lblFiltro, comboOrdenacao);
        root.setTop(painelTopo);

        // Lista de Atividades
        listViewAtividades = new ListView<>();
        // Formata a célula para usar o método polimórfico exibirDetalhes() da Atividade
        listViewAtividades.setCellFactory(param -> new ListCell<Atividade>() {
            @Override
            protected void updateItem(Atividade item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.exibirDetalhes()); // Exibe o texto formatado adequadamente
                }
            }
        });
        root.setCenter(listViewAtividades);

        // Botões de Ação
        HBox painelBase = new HBox(10);
        painelBase.setPadding(new Insets(15, 0, 0, 0));

        Button btnNovaAtividade = new Button("Nova Atividade");
        Button btnConcluir = new Button("Marcar como Concluída");
        Button btnExcluir = new Button("Excluir Selecionada");

        btnNovaAtividade.setOnAction(e -> abrirTelaCadastro());

        btnConcluir.setOnAction(e -> {
            Atividade selecionada = listViewAtividades.getSelectionModel().getSelectedItem();
            if (selecionada != null) {
                gerenciador.marcarComoConcluida(selecionada.getId());
                atualizarLista();
            }
        });

        btnExcluir.setOnAction(e -> {
            Atividade selecionada = listViewAtividades.getSelectionModel().getSelectedItem();
            if (selecionada != null) {
                gerenciador.excluirAtividade(selecionada.getId());
                atualizarLista();
            }
        });

        painelBase.getChildren().addAll(btnNovaAtividade, btnConcluir, btnExcluir);
        root.setBottom(painelBase);
    }

    /**
     * Consulta as atividades no gerenciadore atualiza a interface gráfica.
     */
    public void atualizarLista() {
        listViewAtividades.setItems(FXCollections.observableArrayList(gerenciador.obterAtividadesOrdenadas()));
    }

    /**
     * Abre a janela dee cadastro de novas tarefas/eventos.
     */
    private void abrirTelaCadastro() {
        TelaCadastroAtividade telaCadastro = new TelaCadastroAtividade(gerenciador, this);
        telaCadastro.exibir();
    }
}