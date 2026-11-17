package organizaai.interfacegrafica;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import organizaai.gerenciamento.GerenciadorAgenda;
import organizaai.modelo.Atividade;
import organizaai.modelo.EventoComHorario;
import organizaai.modelo.Status;
import organizaai.modelo.TarefaComPrazo;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

/**
 * Janela com os detalhes de uma atividade e as ações de atualizar status, atualizar e excluir.
 */
public class TelaDetalhesAtividade {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Atividade atividade;
    private final GerenciadorAgenda gerenciador;
    private final TelaPrincipal telaPrincipal;
    private final Stage stage;

    /**
     * @param atividade     A atividade que foi clicada no calendário.
     * @param gerenciador   Para atualizar ou excluir a atividade.
     * @param telaPrincipal Para redesenhar o calendário após a ação.
     */
    public TelaDetalhesAtividade(Atividade atividade, GerenciadorAgenda gerenciador, TelaPrincipal telaPrincipal) {
        this.atividade = atividade;
        this.gerenciador = gerenciador;
        this.telaPrincipal = telaPrincipal;
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Detalhes da Atividade");
    }

    /** Monta a janela e a exibe. */
    public void exibir() {
        Label lblTitulo = new Label(atividade.getTitulo());
        lblTitulo.setWrapText(true);
        lblTitulo.getStyleClass().add("detalhe-titulo");

        // Selo colorido da categoria (reaproveita as classes do CSS)
        Label lblCategoria = new Label(rotulo(atividade.getCategoria()));
        lblCategoria.getStyleClass().addAll("etiqueta",
                "etiqueta-" + atividade.getCategoria().name().toLowerCase(Locale.ROOT));

        boolean ehTarefa = atividade instanceof TarefaComPrazo;

        GridPane info = new GridPane();
        info.setHgap(12);
        info.setVgap(8);
        int linha = 0;
        adicionarLinha(info, linha++, "Tipo:", ehTarefa ? "Tarefa com prazo" : "Evento com horário");
        adicionarLinha(info, linha++, ehTarefa ? "Prazo:" : "Data:", atividade.getData().format(FMT_DATA));
        if (atividade instanceof EventoComHorario) {
            EventoComHorario ev = (EventoComHorario) atividade;
            adicionarLinha(info, linha++, "Horário:", ev.getHoraInicio() + " às " + ev.getHoraFim());
        }
        adicionarLinha(info, linha++, "Prioridade:", rotulo(atividade.getPrioridade()));
        Label lblStatus = adicionarLinha(info, linha++, "Status:", rotulo(statusEfetivo()));
        if (statusEfetivo() == Status.EM_ATRASO) {
            lblStatus.getStyleClass().add("status-atrasado");
        }
        String desc = atividade.getDescricao();
        if (desc != null && !desc.trim().isEmpty()) {
            adicionarLinha(info, linha++, "Descrição:", desc);
        }

        // Botões de ação
        Button btnStatus = new Button("Atualizar status");
        btnStatus.setOnAction(e -> escolherNovoStatus());

        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setOnAction(e -> {
            stage.close();   // fecha os detalhes e abre o formulário de edição
            new TelaAtualizarAtividade(atividade, gerenciador, telaPrincipal).exibir();
        });

        Button btnExcluir = new Button("Excluir");
        btnExcluir.getStyleClass().add("botao-perigo");
        btnExcluir.setOnAction(e -> confirmarExclusao());

        Button btnFechar = new Button("Fechar");
        btnFechar.setOnAction(e -> stage.close());

        HBox botoes = new HBox(10, btnStatus, btnAtualizar, btnExcluir, btnFechar);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        VBox raiz = new VBox(14, lblTitulo, lblCategoria, info, botoes);
        raiz.setPadding(new Insets(20));
        raiz.setPrefWidth(480);

        Scene scene = new Scene(raiz);
        scene.getStylesheets().add(TelaPrincipal.caminhoCss());
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Abre uma caixa para escolher o novo status (pendente, em andamento ou concluída)
     * e aplica a escolha. "Atrasado" não é opção: é definido automaticamente pelo sistema.
     */
    private void escolherNovoStatus() {
        Status atual = atividade.getStatus();
        // Se está atrasada, sugere "Pendente" como ponto de partida, pois "Atrasado" não é escolhível
        Status sugerido = (atual == Status.EM_ATRASO) ? Status.PENDENTE : atual;

        ChoiceDialog<Status> dialogo = new ChoiceDialog<>(sugerido,
                Arrays.asList(Status.PENDENTE, Status.EM_ANDAMENTO, Status.CONCLUIDA));
        dialogo.initOwner(stage);
        dialogo.setTitle("Atualizar status");
        dialogo.setHeaderText(atividade.getTitulo());
        dialogo.setContentText(atividade.estaEmAtraso()
                ? "Novo status (se o prazo já passou e a atividade não for concluída, ela volta a ficar atrasada):"
                : "Novo status:");

        dialogo.showAndWait().ifPresent(novoStatus -> {
            gerenciador.atualizarStatus(atividade.getId(), novoStatus);
            telaPrincipal.atualizarLista();
            stage.close();
        });
    }

    /** Pede confirmação antes de apagar, pois a exclusão não tem desfazer. */
    private void confirmarExclusao() {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir \"" + atividade.getTitulo() + "\"? Esta ação não pode ser desfeita.",
                ButtonType.YES, ButtonType.NO);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.YES) {
                gerenciador.excluirAtividade(atividade.getId());
                telaPrincipal.atualizarLista();
                stage.close();
            }
        });
    }

    /** Status mostrado ao usuário: atividade vencida e não concluída aparece como EM_ATRASO. */
    private Status statusEfetivo() {
        return atividade.estaEmAtraso() ? Status.EM_ATRASO : atividade.getStatus();
    }

    private Label adicionarLinha(GridPane grid, int linha, String nome, String valor) {
        Label lblNome = new Label(nome);
        lblNome.getStyleClass().add("detalhe-rotulo");
        Label lblValor = new Label(valor);
        lblValor.setWrapText(true);
        lblValor.setMaxWidth(300);
        grid.add(lblNome, 0, linha);
        grid.add(lblValor, 1, linha);
        return lblValor;
    }

    /** Converte o nome do enum em texto amigável (com acentos). */
    private static String rotulo(Enum<?> valor) {
        switch (valor.name()) {
            case "EM_ANDAMENTO":
                return "Em andamento";
            case "EM_ATRASO":
                return "Atrasado";
            case "CONCLUIDA":
                return "Concluída";
            case "SAUDE":
                return "Saúde";
            case "MEDIA":
                return "Média";
            default:
                String t = valor.name().toLowerCase(PT_BR);
                return t.substring(0, 1).toUpperCase(PT_BR) + t.substring(1);
        }
    }
}
