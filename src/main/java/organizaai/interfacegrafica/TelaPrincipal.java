package organizaai.interfacegrafica;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import organizaai.gerenciamento.GerenciadorAgenda;
import organizaai.modelo.Atividade;
import organizaai.modelo.EventoComHorario;
import organizaai.modelo.Status;
import organizaai.modelo.TarefaComPrazo;
import organizaai.strategy.OrdenacaoPorCategoria;
import organizaai.strategy.OrdenacaoPorData;
import organizaai.strategy.OrdenacaoPorPrioridade;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Janela principal do sistema.
 * Possui três visões (Mês, Semana e Dia) e navegação entre períodos
 * Ordenação de atividades pelo padrão Strategy
 */
public class TelaPrincipal {

    private enum Visao {MES, SEMANA, DIA};

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FMT_MES = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PT_BR);
    private static final DateTimeFormatter FMT_CURTO = DateTimeFormatter.ofPattern("d 'de' MMM", PT_BR);
    private static final DateTimeFormatter FMT_DIA = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", PT_BR);
    private static final String[] DIAS_SEMANA = {"DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB"};

    //maximo de etiquetas exibidas na visao mes
    private static final int MAX_ETIQUETAS_MES = 3;

    private final BorderPane root;
    private final GerenciadorAgenda gerenciador;
    private final GridPane gridCalendario = new GridPane();
    private final Label lblPeriodo = new Label();
    private final Map<Visao, ToggleButton> botoesVisao = new EnumMap<>(Visao.class);

    private Visao visao = Visao.MES;
    private LocalDate dataReferencia = LocalDate.now();

    /**
     * Constroi a tela principal ligando a ao gerenciador.
     * 
     * @param gerenciador A instância central que controla as regras de negócio.
     */
    public TelaPrincipal(GerenciadorAgenda gerenciador) {
        this.gerenciador = gerenciador;
        this.root = new BorderPane();
        this.root.setPadding((new Insets(15)));

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

    public static String caminhoCss(){
        return TelaPrincipal.class
                .getResource("/organizaai/interfacegrafica/calendario.css")
                .toExternalForm();
    }

    /**
     * Inicializa os botões, listas e o ComboBox do Padrão Strategy.
     */
    private void inicializarComponentes() {
        Button btnHoje = new Button("Hoje");
        Button btnAnterior = new Button("<");
        Button btnProximo = new Button(">");

        btnHoje.setOnAction(e -> {dataReferencia = LocalDate.now(); atualizarLista(); });
        btnAnterior.setOnAction(e -> {dataReferencia = deslocar(-1); atualizarLista(); });
        btnProximo.setOnAction(e -> {dataReferencia = deslocar(1); atualizarLista(); });
        lblPeriodo.getStyleClass().add("titulo-periodo");

        // Botões de visão (Mês | Semana | Dia)
        ToggleGroup grupoVisao = new ToggleGroup();
        HBox boxVisao = new HBox();
        for (Visao v : Visao.values()) {
            ToggleButton tb = new ToggleButton(rotulo(v));
            tb.setUserData(v);
            tb.setToggleGroup(grupoVisao);
            tb.setSelected(v == visao);
            botoesVisao.put(v, tb);
            boxVisao.getChildren().add(tb);
        }
        grupoVisao.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) {      // clicou no botão já ativo: mantém selecionado
                antigo.setSelected(true);
                return;
            }
            visao = (Visao) novo.getUserData();
            atualizarLista();
        });

    // Padrão Strategy: ordenação das atividades
        ComboBox<String> comboOrdenacao = new ComboBox<>();
        comboOrdenacao.getItems().addAll("Data", "Prioridade", "Categoria");
        comboOrdenacao.setValue("Data");
        comboOrdenacao.setOnAction(e -> {
            switch (comboOrdenacao.getValue()) {
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

        // Botão de nova atividade (na visão Dia usa o dia mostrado; nas outras, hoje)
        Button btnNova = new Button("+ Nova Atividade");
        btnNova.getStyleClass().add("botao-principal");
        btnNova.setOnAction(e -> abrirCadastro(visao == Visao.DIA ? dataReferencia : LocalDate.now()));

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox topo = new HBox(8, btnHoje, btnAnterior, btnProximo, lblPeriodo, espaco,
                boxVisao, new Label("Ordenar por:"), comboOrdenacao, btnNova);
        topo.setAlignment(Pos.CENTER_LEFT);
        topo.getStyleClass().add("barra-topo");
        root.setTop(topo);

        // Área do calendário dentro de um ScrollPane (rola se a janela ficar pequena)
        gridCalendario.getStyleClass().add("grade-calendario");
        ScrollPane scroll = new ScrollPane(gridCalendario);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("area-calendario");
        root.setCenter(scroll);
    }


    /**
     * Consulta as atividades no gerenciadore atualiza a interface gráfica.
     */
    public void atualizarLista() {
        gridCalendario.getChildren().clear();
        gridCalendario.getColumnConstraints().clear();
        gridCalendario.getRowConstraints().clear();

        // Uma única consulta (já ordenada pela estratégia) para todas as células
        List<Atividade> todas = gerenciador.obterAtividadesOrdenadas();

        switch (visao) {
            case MES:
                desenharMes(todas);
                break;
            case SEMANA:
                desenharSemana(todas);
                break;
            default:
                desenharDia(todas);
                break;
        }
    }

    private void desenharMes(List<Atividade> todas) {
        lblPeriodo.setText(maiuscula(dataReferencia.format(FMT_MES)));

        LocalDate primeiro = dataReferencia.withDayOfMonth(1);
        LocalDate ultimo = dataReferencia.withDayOfMonth(dataReferencia.lengthOfMonth());

        // getValue(): segunda=1 ... domingo=7. Com "% 7" o domingo vira 0 (início da semana).
        // Recua até o domingo anterior e avança até o sábado seguinte para fechar as semanas.
        LocalDate inicio = primeiro.minusDays(primeiro.getDayOfWeek().getValue() % 7);
        LocalDate fim = ultimo.plusDays(6 - (ultimo.getDayOfWeek().getValue() % 7));

        definirColunas(7);
        adicionarCabecalhoDiasSemana();

        int indice = 0;
        for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1), indice++) {
            VBox celula = criarCelulaDia(d, todas, MAX_ETIQUETAS_MES);
            if (d.getMonth() != dataReferencia.getMonth()) {
                celula.getStyleClass().add("outro-mes");   // dias de meses vizinhos, em cinza
            }
            gridCalendario.add(celula, indice % 7, indice / 7 + 1);
        }
        definirLinhas(indice / 7, 90, true);
    }

    private void desenharSemana(List<Atividade> todas) {
        LocalDate inicio = dataReferencia.minusDays(dataReferencia.getDayOfWeek().getValue() % 7);
        LocalDate fim = inicio.plusDays(6);
        lblPeriodo.setText(inicio.format(FMT_CURTO) + " – " + fim.format(FMT_CURTO) + " de " + fim.getYear());

        definirColunas(7);
        adicionarCabecalhoDiasSemana();

        for (int i = 0; i < 7; i++) {
            // Sem limite de etiquetas: na semana a célula cresce e a tela rola
            gridCalendario.add(criarCelulaDia(inicio.plusDays(i), todas, Integer.MAX_VALUE), i, 1);
        }
        definirLinhas(1, 300, true);
    }

    private void desenharDia(List<Atividade> todas) {
        final LocalDate dia = dataReferencia;
        lblPeriodo.setText(maiuscula(dia.format(FMT_DIA)));

        definirColunas(1);

        VBox painel = new VBox(8);
        painel.getStyleClass().add("painel-dia");

        Button btnNovaNoDia = new Button("+ Nova atividade neste dia");
        btnNovaNoDia.setDisable(dia.isBefore(LocalDate.now()));   // o sistema não aceita datas passadas
        btnNovaNoDia.setOnAction(e -> abrirCadastro(dia));
        painel.getChildren().add(btnNovaNoDia);

        List<Atividade> doDia = atividadesDoDia(todas, dia);
        if (doDia.isEmpty()) {
            painel.getChildren().add(new Label("Nenhuma atividade neste dia."));
        } else {
            for (Atividade atv : doDia) {
                painel.getChildren().add(criarEtiqueta(atv, true));
            }
        }

        gridCalendario.add(painel, 0, 0);
        definirLinhas(1, 300, false);
    }

    /**
     * Monta o quadrado de um dia: número, etiquetas das atividades e "+ N mais".
     *
     * @param data   O dia que a célula representa.
     * @param todas  Todas as atividades (já ordenadas pela estratégia).
     * @param limite Quantas etiquetas mostrar antes de colapsar em "+ N mais".
     */
    private VBox criarCelulaDia(LocalDate data, List<Atividade> todas, int limite) {
        VBox celula = new VBox(3);
        celula.setMinWidth(0);
        celula.getStyleClass().add("celula-dia");

        LocalDate hoje = LocalDate.now();
        boolean ehHoje = data.equals(hoje);
        boolean passado = data.isBefore(hoje);
        if (ehHoje) celula.getStyleClass().add("hoje");
        if (passado) celula.getStyleClass().add("passado");

        // Clicar no número do dia abre a visão Dia daquela data
        Label lblDia = new Label(String.valueOf(data.getDayOfMonth()));
        lblDia.getStyleClass().add("numero-dia");
        if (ehHoje) lblDia.getStyleClass().add("numero-dia-hoje");
        lblDia.setOnMouseClicked(e -> {
            e.consume();
            selecionarVisao(Visao.DIA, data);
        });
        celula.getChildren().add(lblDia);

        List<Atividade> doDia = atividadesDoDia(todas, data);
        int exibidas = Math.min(limite, doDia.size());
        for (int i = 0; i < exibidas; i++) {
            celula.getChildren().add(criarEtiqueta(doDia.get(i), false));
        }

        // "+ N mais": leva para a visão Dia, onde cabem todas
        int restantes = doDia.size() - exibidas;
        if (restantes > 0) {
            Label lblMais = new Label("+ " + restantes + " mais");
            lblMais.getStyleClass().add("mais");
            lblMais.setOnMouseClicked(e -> {
                e.consume();
                selecionarVisao(Visao.DIA, data);
            });
            celula.getChildren().add(lblMais);
        }

        // Clicar em qualquer espaço livre do dia abre o cadastro com essa data
        // (dias passados não são clicáveis, pois o gerenciador recusa datas no passado)
        if (!passado) {
            celula.setCursor(Cursor.HAND);
            celula.setOnMouseClicked(e -> abrirCadastro(data));
        }
        return celula;
    }

    /**
     * Cria a etiqueta colorida de uma atividade.
     *
     * @param atv       A atividade.
     * @param detalhada true para o texto completo (visão Dia); false para o resumo (Mês/Semana).
     */
    private Label criarEtiqueta(Atividade atv, boolean detalhada) {
        String texto;
        if (detalhada) {
            texto = atv.exibirDetalhes();
        } else if (atv instanceof EventoComHorario) {
            texto = ((EventoComHorario) atv).getHoraInicio() + " " + atv.getTitulo();
        } else {
            texto = atv.getTitulo();
        }
        if (!detalhada && atv.getStatus() == Status.CONCLUIDA) {
            texto = "✓ " + texto;
        }

        Label etiqueta = new Label(texto);
        etiqueta.setMaxWidth(Double.MAX_VALUE);
        etiqueta.setMinWidth(0);
        etiqueta.setTextOverrun(OverrunStyle.ELLIPSIS);
        etiqueta.setWrapText(detalhada);

        // A cor vem do CSS, pela classe da categoria (ex.: "etiqueta-faculdade")
        etiqueta.getStyleClass().addAll("etiqueta",
                "etiqueta-" + atv.getCategoria().name().toLowerCase(Locale.ROOT));
        if (detalhada) etiqueta.getStyleClass().add("etiqueta-detalhada");
        if (atv.getStatus() == Status.CONCLUIDA) etiqueta.getStyleClass().add("concluida");
        if (atv instanceof TarefaComPrazo && ((TarefaComPrazo) atv).estaEmAtraso()) {
            etiqueta.getStyleClass().add("atrasada");
        }

        etiqueta.setTooltip(new Tooltip(atv.exibirDetalhes()));

        // consume() evita que o clique "vaze" para a célula e abra o cadastro também
        etiqueta.setOnMouseClicked(e -> {
            e.consume();
            abrirDetalhes(atv);
        });
        return etiqueta;
    }

    //Auxiliares

    /** Filtra as atividades de um dia, mantendo a ordem definida pela estratégia. */
    private List<Atividade> atividadesDoDia(List<Atividade> todas, LocalDate data) {
        List<Atividade> resultado = new ArrayList<>();
        for (Atividade a : todas) {
            if (a.getData().equals(data)) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    /** Troca de visão e de data de uma vez (usado por "+ N mais" e pelo clique no número). */
    private void selecionarVisao(Visao novaVisao, LocalDate data) {
        dataReferencia = data;
        ToggleButton alvo = botoesVisao.get(novaVisao);
        if (alvo.isSelected()) {
            atualizarLista();      // já estava nessa visão: só redesenha
        } else {
            alvo.setSelected(true); // o listener do grupo atualiza a visão e redesenha
        }
    }

    /** Move a data de referência um período para trás (-1) ou para frente (+1). */
    private LocalDate deslocar(int sentido) {
        switch (visao) {
            case MES:
                return dataReferencia.plusMonths(sentido);
            case SEMANA:
                return dataReferencia.plusWeeks(sentido);
            default:
                return dataReferencia.plusDays(sentido);
        }
    }

    private void adicionarCabecalhoDiasSemana() {
        for (int i = 0; i < 7; i++) {
            Label l = new Label(DIAS_SEMANA[i]);
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            l.getStyleClass().add("cabecalho-dia-semana");
            gridCalendario.add(l, i, 0);
        }
    }

    /** Divide a largura igualmente entre as colunas. */
    private void definirColunas(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / quantidade);
            gridCalendario.getColumnConstraints().add(cc);
        }
    }

    /** Linhas que dividem a altura livre; a linha 0 (cabeçalho) fica com altura mínima. */
    private void definirLinhas(int quantidade, double alturaMinima, boolean comCabecalho) {
        if (comCabecalho) {
            gridCalendario.getRowConstraints().add(new RowConstraints());
        }
        for (int i = 0; i < quantidade; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(alturaMinima);
            rc.setVgrow(Priority.ALWAYS);
            gridCalendario.getRowConstraints().add(rc);
        }
    }

    private String rotulo(Visao v) {
        switch (v) {
            case MES:
                return "Mês";
            case SEMANA:
                return "Semana";
            default:
                return "Dia";
        }
    }

    private String maiuscula(String texto) {
        return texto.substring(0, 1).toUpperCase(PT_BR) + texto.substring(1);
    }

    private void abrirCadastro(LocalDate data) {
        new TelaCadastroAtividade(gerenciador, this, data).exibir();
    }

    private void abrirDetalhes(Atividade atividade) {
        new TelaDetalhesAtividade(atividade, gerenciador, this).exibir();
    }
}