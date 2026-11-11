package organizaai.interfacegrafica;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import organizaai.excecoes.ConflitoDeHorarioException;
import organizaai.excecoes.DataInvalidaException;
import organizaai.gerenciamento.GerenciadorAgenda;
import organizaai.modelo.Categoria;
import organizaai.modelo.EventoComHorario;
import organizaai.modelo.Prioridade;
import organizaai.modelo.TarefaComPrazo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Tela para cadastrar uma nova atividade
 * Faz as validações da interface gráfica e invoca o controlador.
 */
public class TelaCadastroAtividade {

    private GerenciadorAgenda gerenciador;
    private TelaPrincipal telaPrincipal;
    private Stage stage;

    /**
     * Construtor da tela  de cadastro.
     * 
     * @param gerenciador A instância central para salvar a nova atividade.
     * @param telaPrincipal A janela principal, para forçar a atualização da lista após o cadastro.
     */
    public TelaCadastroAtividade(GerenciadorAgenda gerenciador, TelaPrincipal telaPrincipal) {
        this.gerenciador = gerenciador;
        this.telaPrincipal = telaPrincipal;
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); // Impede clicar na tela de trás enquanto esta estiver aberta
        stage.setTitle("Cadastrar Nova Atividade");
    }

    /**
     * Desenha a interface e exibe a janela.
     */
    public void exibir() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        // Campos genéricos
        ComboBox<String> comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll("Tarefa com Prazo", "Evento com Horário");
        comboTipo.setValue("Tarefa com Prazo");

        TextField txtTitulo = new TextField();
        TextField txtDescricao = new TextField();
        DatePicker dpData = new DatePicker(LocalDate.now());
        
        ComboBox<Categoria> comboCategoria = new ComboBox<>();
        comboCategoria.getItems().addAll(Categoria.values());
        comboCategoria.setValue(Categoria.PESSOAL);

        ComboBox<Prioridade> comboPrioridade = new ComboBox<>();
        comboPrioridade.getItems().addAll(Prioridade.values());
        comboPrioridade.setValue(Prioridade.MEDIA);

        // Campos específicos de Evento (Hora Início e Fim) - formato HH:mm
        TextField txtHoraInicio = new TextField("08:00");
        TextField txtHoraFim = new TextField("09:00");
        Label lblHoraInicio = new Label("Hora Início (HH:mm):");
        Label lblHoraFim = new Label("Hora Fim (HH:mm):");

        // Oculta os campos de horário inicialmente, pois o padrão é "Tarefa"
        lblHoraInicio.setVisible(false);
        txtHoraInicio.setVisible(false);
        lblHoraFim.setVisible(false);
        txtHoraFim.setVisible(false);

        // Altera a visibilidade dinamicamente se o usuário mudar para Evento
        comboTipo.setOnAction(e -> {
            boolean isEvento = comboTipo.getValue().equals("Evento com Horário");
            lblHoraInicio.setVisible(isEvento);
            txtHoraInicio.setVisible(isEvento);
            lblHoraFim.setVisible(isEvento);
            txtHoraFim.setVisible(isEvento);
        });

        // Adiciona ao grid
        grid.add(new Label("Tipo:"), 0, 0); grid.add(comboTipo, 1, 0);
        grid.add(new Label("Título:"), 0, 1); grid.add(txtTitulo, 1, 1);
        grid.add(new Label("Descrição:"), 0, 2); grid.add(txtDescricao, 1, 2);
        grid.add(new Label("Data:"), 0, 3); grid.add(dpData, 1, 3);
        grid.add(new Label("Categoria:"), 0, 4); grid.add(comboCategoria, 1, 4);
        grid.add(new Label("Prioridade:"), 0, 5); grid.add(comboPrioridade, 1, 5);
        grid.add(lblHoraInicio, 0, 6); grid.add(txtHoraInicio, 1, 6);
        grid.add(lblHoraFim, 0, 7); grid.add(txtHoraFim, 1, 7);

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setOnAction(e -> {
            try {
                // Valida preenchimento
                if (txtTitulo.getText().trim().isEmpty()) {
                    mostrarErro("O título não pode estar vazio.");
                    return;
                }

                // Criação polimórfica baseada na escolha
                if (comboTipo.getValue().equals("Tarefa com Prazo")) {
                    TarefaComPrazo novaTarefa = new TarefaComPrazo(
                            txtTitulo.getText(), txtDescricao.getText(), dpData.getValue(), 
                            comboCategoria.getValue(), comboPrioridade.getValue()
                    );
                    gerenciador.cadastrarAtividade(novaTarefa); // Pode lançar DataInvalidaException

                } else {
                    LocalTime hrInicio = LocalTime.parse(txtHoraInicio.getText());
                    LocalTime hrFim = LocalTime.parse(txtHoraFim.getText());

                    EventoComHorario novoEvento = new EventoComHorario(
                            txtTitulo.getText(), txtDescricao.getText(), dpData.getValue(),
                            hrInicio, hrFim, comboCategoria.getValue(), comboPrioridade.getValue()
                    );
                    gerenciador.cadastrarAtividade(novoEvento); // Pode lançar Data/Conflito Exceptions
                }

                telaPrincipal.atualizarLista(); // Atualiza a tela de trás
                stage.close(); // Fecha o formulário

            } catch (DataInvalidaException | ConflitoDeHorarioException ex) {
                mostrarErro(ex.getMessage());
            } catch (Exception ex) {
                mostrarErro("Formato de horário inválido. Use HH:mm (Ex: 08:30).");
            }
        });

        grid.add(btnSalvar, 1, 8);

        Scene scene = new Scene(grid, 400, 350);
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Exibe um erro para o usuário
     * 
     * @param mensagem A mensagem da exceção a ser mostrada.
     */
    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Validação");
        alert.setHeaderText("Não foi possível cadastrar a atividade");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}