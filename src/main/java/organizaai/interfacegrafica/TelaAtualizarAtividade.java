package organizaai.interfacegrafica;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import organizaai.excecoes.ConflitoDeHorarioException;
import organizaai.gerenciamento.GerenciadorAgenda;
import organizaai.modelo.Atividade;
import organizaai.modelo.EventoComHorario;
import organizaai.modelo.Prioridade;
import organizaai.modelo.TarefaComPrazo;

import java.time.LocalTime;

/**
 * Tela para editar o nome, a prioridade e o tipo de uma atividade já cadastrada.
 * Os demais dados (data, categoria, descrição e status) são mantidos.
 */
public class TelaAtualizarAtividade {

    private static final String TIPO_TAREFA = "Tarefa com Prazo";
    private static final String TIPO_EVENTO = "Evento com Horário";

    private final Atividade atividade;
    private final GerenciadorAgenda gerenciador;
    private final TelaPrincipal telaPrincipal;
    private final Stage stage;

    /**
     * @param atividade     A atividade que será editada.
     * @param gerenciador   Para gravar a versão editada.
     * @param telaPrincipal Para redesenhar o calendário depois de salvar.
     */
    public TelaAtualizarAtividade(Atividade atividade, GerenciadorAgenda gerenciador,
                                  TelaPrincipal telaPrincipal, Stage stage) {
        this.atividade = atividade;
        this.gerenciador = gerenciador;
        this.telaPrincipal = telaPrincipal;
        this.stage = stage;
    }

    /** Desenha o formulário já preenchido com os dados atuais e o exibe. */
    public void exibir() {
        boolean ehEvento = atividade instanceof EventoComHorario;

        ComboBox<String> comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll(TIPO_TAREFA, TIPO_EVENTO);
        comboTipo.setValue(ehEvento ? TIPO_EVENTO : TIPO_TAREFA);

        TextField txtTitulo = new TextField(atividade.getTitulo());

        ComboBox<Prioridade> comboPrioridade = new ComboBox<>();
        comboPrioridade.getItems().addAll(Prioridade.values());
        comboPrioridade.setValue(atividade.getPrioridade());

        // Horários: se já é evento, mostra os atuais; se virar evento, começa com o padrão do cadastro
        TextField txtHoraInicio = new TextField("08:00");
        TextField txtHoraFim = new TextField("09:00");
        if (ehEvento) {
            EventoComHorario evento = (EventoComHorario) atividade;
            txtHoraInicio.setText(evento.getHoraInicio().toString());
            txtHoraFim.setText(evento.getHoraFim().toString());
        }
        Label lblHoraInicio = new Label("Hora Início (HH:mm):");
        Label lblHoraFim = new Label("Hora Fim (HH:mm):");

        // Os campos de horário só aparecem quando o tipo escolhido é Evento
        Runnable atualizarVisibilidade = () -> {
            boolean mostrar = TIPO_EVENTO.equals(comboTipo.getValue());
            lblHoraInicio.setVisible(mostrar);
            txtHoraInicio.setVisible(mostrar);
            lblHoraFim.setVisible(mostrar);
            txtHoraFim.setVisible(mostrar);
        };
        atualizarVisibilidade.run();
        comboTipo.setOnAction(e -> atualizarVisibilidade.run());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Tipo:"), 0, 0); grid.add(comboTipo, 1, 0);
        grid.add(new Label("Título:"), 0, 1); grid.add(txtTitulo, 1, 1);
        grid.add(new Label("Prioridade:"), 0, 2); grid.add(comboPrioridade, 1, 2);
        grid.add(lblHoraInicio, 0, 3); grid.add(txtHoraInicio, 1, 3);
        grid.add(lblHoraFim, 0, 4); grid.add(txtHoraFim, 1, 4);

        Button btnSalvar = new Button("Salvar alterações");
        btnSalvar.getStyleClass().add("botao-principal");
        btnSalvar.setOnAction(e -> salvar(
                comboTipo.getValue(), txtTitulo.getText(), comboPrioridade.getValue(),
                txtHoraInicio.getText(), txtHoraFim.getText()));

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> stage.close());

        grid.add(btnSalvar, 1, 5);
        grid.add(btnCancelar, 1, 6);

        Scene scene = new Scene(grid, 400, 330);
        scene.getStylesheets().add(TelaPrincipal.caminhoCss());
        stage.setTitle("Editar Atividade");
        stage.setScene(scene);
        stage.sizeToScene();   // a janela dos detalhes tinha outro tamanho
        txtTitulo.requestFocus();
    }

    /**
     * Valida os campos, monta a atividade editada (mesmo ID, data, categoria, descrição e status)
     * e a entrega ao gerenciador.
     */
    private void salvar(String tipo, String titulo, Prioridade prioridade, String textoInicio, String textoFim) {
        if (titulo.trim().isEmpty()) {
            mostrarErro("O título não pode estar vazio.");
            return;
        }

        Atividade atualizada;
        if (TIPO_EVENTO.equals(tipo)) {
            LocalTime horaInicio;
            LocalTime horaFim;
            try {
                horaInicio = LocalTime.parse(textoInicio.trim());
                horaFim = LocalTime.parse(textoFim.trim());
                if (horaFim.isBefore(horaInicio)){
                    mostrarErro("O horário do fim não pode ser antes do horário de início.");
                    return;
                }
                if (horaFim.equals(horaInicio)){
                    mostrarErro("O horário do fim não pode ser ao mesmo tempo do horário de início.");
                    return;
                }
            } catch (Exception ex) {
                mostrarErro("Formato de horário inválido. Use HH:mm (Ex: 08:30).");
                return;
            }
            atualizada = new EventoComHorario(titulo.trim(), atividade.getDescricao(), atividade.getData(),
                    horaInicio, horaFim, atividade.getCategoria(), prioridade, atividade.getStatus());
        } else {
            atualizada = new TarefaComPrazo(titulo.trim(), atividade.getDescricao(), atividade.getData(),
                    atividade.getCategoria(), prioridade, atividade.getStatus());
        }
        atualizada.setId(atividade.getId());   // mesma atividade, só que com dados novos

        try {
            gerenciador.atualizarAtividade(atualizada);
        } catch (ConflitoDeHorarioException ex) {
            mostrarErro(ex.getMessage());
            return;
        }

        telaPrincipal.atualizarLista();
        stage.close();
    }

    private void mostrarErro(String mensagem) {
        Dialogos.mostrarErro("Erro de Validação", "Não foi possível editar a atividade", mensagem);
    }
}
