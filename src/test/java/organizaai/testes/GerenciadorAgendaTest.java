package organizaai.testes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import organizaai.excecoes.ConflitoDeHorarioException;
import organizaai.excecoes.DataInvalidaException;
import organizaai.gerenciamento.GerenciadorAgenda;
import organizaai.modelo.Atividade;
import organizaai.modelo.Categoria;
import organizaai.modelo.EventoComHorario;
import organizaai.modelo.Prioridade;
import organizaai.modelo.TarefaComPrazo;
import organizaai.modelo.Status;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de testes unitários para validar a lógica principal do GerenciadorAgenda.
 */
public class GerenciadorAgendaTest {

    private GerenciadorAgenda gerenciador;

    /**
     * Método de preparação executado  antes de cada teste.
     * Garante que a lista de atividades inicie sempre vazia
     */
    @BeforeEach
    public void setUp() {
        gerenciador = new GerenciadorAgenda();
        // Limpa os dados do disco para que um teste não interfira no outro
        gerenciador.getListaAtividades().clear(); 
    }

    /**
     * Testa se o sistema permite cadastrar uma tarefa com dados válidos.
     */
    @Test
    public void testCadastrarTarefaComSucesso() {
        try {
            // Define uma data futura para a tarefa
            LocalDate dataFutura = LocalDate.now().plusDays(2);

            // Cria uma nova tarefa
            TarefaComPrazo tarefa = new TarefaComPrazo(
                    "Entregar Projeto MC322",
                    "Fazer o push da release no GitHub",
                    dataFutura,
                    Categoria.FACULDADE,
                    Prioridade.ALTA,
                    Status.PENDENTE
            );

            // Cadastra a tarefa no gerenciador
            gerenciador.cadastrarAtividade(tarefa);

            // Verifica se a atividade foi cadastrada
            assertEquals(1, gerenciador.getListaAtividades().size());

            // Verifica se o titulo está correto
            assertEquals(
                    "Entregar Projeto MC322",
                    gerenciador.getListaAtividades().get(0).getTitulo()
            );

        } catch (DataInvalidaException | ConflitoDeHorarioException e) {
            fail("Não deveria ocorrer uma exceção neste teste.");
        }
    }
    

    /**
     * Testa o tratamento de exceção ao tentar cadastrar uma atividade com data no passado.
     * Valida a eficácia do método validarData().
     */
    @Test
    public void testDataInvalidaException() {
        LocalDate dataPassada = LocalDate.now().minusDays(5);
        
        TarefaComPrazo tarefaAtrasada = new TarefaComPrazo(
                "Tarefa Atrasada", 
                "Esta tarefa deve falhar ao salvar", 
                dataPassada, 
                Categoria.PESSOAL, 
                Prioridade.BAIXA,
                Status.PENDENTE
        );

        // O assertThrows verifica se o método lança exatamente a DataInvalidaException
        Exception excecao = assertThrows(DataInvalidaException.class, () -> {
            gerenciador.cadastrarAtividade(tarefaAtrasada);
        });

        // Verifica se a lista continuou vazia (ou seja, o cadastro foi bloqueado)
        assertEquals(0, gerenciador.getListaAtividades().size());
        assertTrue(excecao.getMessage().contains("data no passado"), "A mensagem de erro deve informar sobre a data no passado.");
    }

    /**
     * Testa o tratamento de exceção ao tentar cadastrar dois eventos que se sobrepõem no mesmo dia.
     * Valida a eficácia da intersecção de horários no método validarConflitoDeHorario().
     */
    @Test
    public void testConflitoDeHorarioException() throws DataInvalidaException, ConflitoDeHorarioException {
        LocalDate data = LocalDate.now().plusDays(1);
        
        // Primeiro evento: das 14:00 às 16:00
        EventoComHorario evento1 = new EventoComHorario(
                "Reunião de Grupo", "Discussão do Projeto", data,
                LocalTime.of(14, 0), LocalTime.of(16, 0),
                Categoria.FACULDADE, Prioridade.ALTA, Status.PENDENTE
        );

        // Segundo evento: das 15:00 às 17:00 (Sobrepõe o evento 1 em 1 hora)
        EventoComHorario evento2 = new EventoComHorario(
                "Consultoria CCU", "Workshop", data,
                LocalTime.of(15, 0), LocalTime.of(17, 0),
                Categoria.TRABALHO, Prioridade.MEDIA, Status.PENDENTE
        );

        // O primeiro evento deve ser cadastrado sem problemas
        gerenciador.cadastrarAtividade(evento1);

        // O segundo evento deve disparar a ConflitoDeHorarioException
        assertThrows(ConflitoDeHorarioException.class, () -> {
            gerenciador.cadastrarAtividade(evento2);
        });

        // Garante que o segundo evento não foi inserido na lista
        assertEquals(1, gerenciador.getListaAtividades().size(), "A lista deve conter apenas o primeiro evento.");
    }

    /**
     * Testa se aceita a adição dois eventos em horarios consecutivos
     */
    @Test
    public void testEventosEmHorariosConsecutivos() throws DataInvalidaException, ConflitoDeHorarioException {
        LocalDate data = LocalDate.now().plusDays(1);
        
        // Primeiro evento: das 14:00 às 15:00
        EventoComHorario evento1 = new EventoComHorario(
                "Reunião de Grupo", "Discussão do Projeto", data,
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                Categoria.FACULDADE, Prioridade.ALTA, Status.PENDENTE
        );

        // Segundo evento: das 15:00 às 16:00
        EventoComHorario evento2 = new EventoComHorario(
                "Consultoria CCU", "Workshop", data,
                LocalTime.of(15, 0), LocalTime.of(16, 0),
                Categoria.TRABALHO, Prioridade.MEDIA, Status.PENDENTE
        );

        gerenciador.cadastrarAtividade(evento1);
        gerenciador.cadastrarAtividade(evento2);

        assertEquals(2, gerenciador.getListaAtividades().size(), "A lista deve conter ambos os eventos.");
    }

    /**
     * Testa se aceita adição de eventos no mesmo horário, porém em dias diferentes
     */
    @Test
    public void testEventosNoMesmoHorarioEmDiasDiferentes() throws DataInvalidaException, ConflitoDeHorarioException {
        LocalDate data1 = LocalDate.now().plusDays(1);
        LocalDate data2 = LocalDate.now().plusDays(2);
        
        // Primeiro evento: das 14:00 às 15:00
        EventoComHorario evento1 = new EventoComHorario(
                "Reunião de Grupo", "Discussão do Projeto", data1,
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                Categoria.FACULDADE, Prioridade.ALTA, Status.PENDENTE
        );

        // Segundo evento: das 14:00 às 15:00 do dia seguinte
        EventoComHorario evento2 = new EventoComHorario(
                "Consultoria CCU", "Workshop", data2,
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                Categoria.TRABALHO, Prioridade.MEDIA, Status.PENDENTE
        );

        gerenciador.cadastrarAtividade(evento1);
        gerenciador.cadastrarAtividade(evento2);

        assertEquals(2, gerenciador.getListaAtividades().size(), "A lista deve conter ambos os eventos.");
    }

    /**
     * Testa se a atualização de status (pendente, em andamento, concluída) funciona pelo ID gerado.
     */
    @Test
    public void testAtualizarStatus() throws Exception {
        LocalDate data = LocalDate.now().plusDays(1);
        TarefaComPrazo tarefa = new TarefaComPrazo(
                "Comprar presente", "Shopping", data, 
                Categoria.PESSOAL, Prioridade.MEDIA, Status.PENDENTE
        );
        
        gerenciador.cadastrarAtividade(tarefa);
        
        // Recupera o ID (UUID) que foi gerado automaticamente pela superclasse Atividade
        String idGerado = gerenciador.getListaAtividades().get(0).getId();
        
        assertTrue(gerenciador.atualizarStatus(idGerado, Status.EM_ANDAMENTO));
        assertEquals(Status.EM_ANDAMENTO, gerenciador.getListaAtividades().get(0).getStatus());

        assertTrue(gerenciador.atualizarStatus(idGerado, Status.CONCLUIDA));
        assertEquals(Status.CONCLUIDA, gerenciador.getListaAtividades().get(0).getStatus(),
                    "A atividade deveria estar marcada como concluída.");
    }

    /**
     * Atualizar o status de um ID que não existe não deve alterar nada e deve devolver false.
     */
    @Test
    public void testAtualizarStatusComIdInexistente() {
        assertFalse(gerenciador.atualizarStatus("id-que-nao-existe", Status.CONCLUIDA));
    }

    /**
     * Tarefa e evento não concluídos cuja data já passou devem virar EM_ATRASO automaticamente.
     * (Entram direto na lista porque cadastrarAtividade recusa datas no passado.)
     */
    @Test
    public void testAtividadesVencidasViramAtrasadas() {
        LocalDate ontem = LocalDate.now().minusDays(1);
        TarefaComPrazo tarefa = new TarefaComPrazo("Tarefa vencida", "", ontem,
                Categoria.FACULDADE, Prioridade.ALTA, Status.PENDENTE);
        EventoComHorario evento = new EventoComHorario("Evento passado", "", ontem,
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                Categoria.TRABALHO, Prioridade.MEDIA, Status.EM_ANDAMENTO);
        gerenciador.getListaAtividades().add(tarefa);
        gerenciador.getListaAtividades().add(evento);

        assertTrue(gerenciador.atualizarStatusAtrasados(), "Deveria informar que houve mudança.");
        assertEquals(Status.EM_ATRASO, tarefa.getStatus());
        assertEquals(Status.EM_ATRASO, evento.getStatus());

        // Rodar de novo não muda mais nada
        assertFalse(gerenciador.atualizarStatusAtrasados());
    }

    /**
     * Atividade concluída não vira atrasada, mesmo com a data no passado; atividade futura também não.
     */
    @Test
    public void testConcluidaEFuturaNaoViramAtrasadas() throws Exception {
        TarefaComPrazo concluida = new TarefaComPrazo("Já feita", "", LocalDate.now().minusDays(3),
                Categoria.PESSOAL, Prioridade.BAIXA, Status.CONCLUIDA);
        gerenciador.getListaAtividades().add(concluida);
        TarefaComPrazo futura = new TarefaComPrazo("No prazo", "", LocalDate.now().plusDays(3),
                Categoria.PESSOAL, Prioridade.BAIXA, Status.PENDENTE);
        gerenciador.cadastrarAtividade(futura);
        TarefaComPrazo hoje = new TarefaComPrazo("Vence hoje", "", LocalDate.now(),
                Categoria.PESSOAL, Prioridade.BAIXA, Status.PENDENTE);
        gerenciador.cadastrarAtividade(hoje);

        assertFalse(gerenciador.atualizarStatusAtrasados());
        assertEquals(Status.CONCLUIDA, concluida.getStatus());
        assertEquals(Status.PENDENTE, futura.getStatus());
        assertEquals(Status.PENDENTE, hoje.getStatus(), "No dia do prazo ainda não está atrasada.");
    }

    /**
     * Atualizar nome e prioridade mantém o ID, a data, a categoria, a descrição e o status.
     */
    @Test
    public void testAtualizarNomeEPrioridade() throws Exception {
        LocalDate data = LocalDate.now().plusDays(2);
        TarefaComPrazo original = new TarefaComPrazo("Nome antigo", "Detalhes", data,
                Categoria.FACULDADE, Prioridade.BAIXA, Status.EM_ANDAMENTO);
        gerenciador.cadastrarAtividade(original);

        TarefaComPrazo nova = new TarefaComPrazo("Nome novo", original.getDescricao(), original.getData(),
                original.getCategoria(), Prioridade.ALTA, original.getStatus());
        nova.setId(original.getId());
        gerenciador.atualizarAtividade(nova);

        assertEquals(1, gerenciador.getListaAtividades().size(), "Não deve duplicar a atividade.");
        Atividade salva = gerenciador.getListaAtividades().get(0);
        assertEquals("Nome novo", salva.getTitulo());
        assertEquals(Prioridade.ALTA, salva.getPrioridade());
        assertEquals(original.getId(), salva.getId());
        assertEquals(Status.EM_ANDAMENTO, salva.getStatus());
        assertEquals(data, salva.getData());
    }

    /**
     * Mudar o tipo de tarefa para evento troca a classe do objeto, mantendo o mesmo ID.
     */
    @Test
    public void testAtualizarTipoDeTarefaParaEvento() throws Exception {
        LocalDate data = LocalDate.now().plusDays(2);
        TarefaComPrazo tarefa = new TarefaComPrazo("Reunião", "", data,
                Categoria.TRABALHO, Prioridade.MEDIA, Status.PENDENTE);
        gerenciador.cadastrarAtividade(tarefa);

        EventoComHorario evento = new EventoComHorario("Reunião", "", data,
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                Categoria.TRABALHO, Prioridade.MEDIA, Status.PENDENTE);
        evento.setId(tarefa.getId());
        gerenciador.atualizarAtividade(evento);

        Atividade salva = gerenciador.getListaAtividades().get(0);
        assertTrue(salva instanceof EventoComHorario, "A atividade deveria agora ser um evento.");
        assertEquals(tarefa.getId(), salva.getId());
    }

    /**
     * Atualizar um evento sem mudar o horário não pode acusar conflito com ele mesmo,
     * mas continua acusando conflito com outro evento.
     */
    @Test
    public void testAtualizarEventoValidaConflitoSemContarOProprioEvento() throws Exception {
        LocalDate data = LocalDate.now().plusDays(1);
        EventoComHorario e1 = new EventoComHorario("Aula", "", data,
                LocalTime.of(14, 0), LocalTime.of(16, 0),
                Categoria.FACULDADE, Prioridade.ALTA, Status.PENDENTE);
        EventoComHorario e2 = new EventoComHorario("Academia", "", data,
                LocalTime.of(18, 0), LocalTime.of(19, 0),
                Categoria.SAUDE, Prioridade.BAIXA, Status.PENDENTE);
        gerenciador.cadastrarAtividade(e1);
        gerenciador.cadastrarAtividade(e2);

        // Só muda o nome do primeiro: não deve dar conflito consigo mesmo
        EventoComHorario e1Renomeado = new EventoComHorario("Aula de Controle", "", data,
                e1.getHoraInicio(), e1.getHoraFim(), e1.getCategoria(), e1.getPrioridade(), e1.getStatus());
        e1Renomeado.setId(e1.getId());
        gerenciador.atualizarAtividade(e1Renomeado);
        assertEquals("Aula de Controle", gerenciador.getListaAtividades().get(0).getTitulo());

        // Empurra o primeiro para cima do segundo: deve dar conflito e não alterar nada
        EventoComHorario e1Conflitante = new EventoComHorario("Aula de Controle", "", data,
                LocalTime.of(18, 30), LocalTime.of(19, 30),
                e1.getCategoria(), e1.getPrioridade(), e1.getStatus());
        e1Conflitante.setId(e1.getId());
        assertThrows(ConflitoDeHorarioException.class, () -> gerenciador.atualizarAtividade(e1Conflitante));
        assertEquals(LocalTime.of(14, 0),
                ((EventoComHorario) gerenciador.getListaAtividades().get(0)).getHoraInicio());
    }

    /**
     * Atualizar uma atividade que não existe na lista deve falhar de forma explícita.
     */
    @Test
    public void testAtualizarAtividadeInexistente() {
        TarefaComPrazo fantasma = new TarefaComPrazo("Fantasma", "", LocalDate.now().plusDays(1),
                Categoria.PESSOAL, Prioridade.BAIXA, Status.PENDENTE);
        assertThrows(IllegalArgumentException.class, () -> gerenciador.atualizarAtividade(fantasma));
    }

    /**
     * Testa se a funcionalidade de exclusão remove corretamente o objeto da lista pelo ID.
     */
    @Test
    public void testExcluirAtividade() throws Exception {
        LocalDate data = LocalDate.now().plusDays(1);
        TarefaComPrazo tarefa = new TarefaComPrazo(
                "Estudar Microcontroladores", "Revisar STM32", data,
                Categoria.FACULDADE, Prioridade.ALTA, Status.PENDENTE
        );
        
        gerenciador.cadastrarAtividade(tarefa);
        assertEquals(1, gerenciador.getListaAtividades().size());

        String idGerado = gerenciador.getListaAtividades().get(0).getId();
        
        // Invoca a exclusão
        boolean removido = gerenciador.excluirAtividade(idGerado);

        // Verifica se o método retornou true e se a lista ficou vazia
        assertTrue(removido, "O método deve retornar true ao encontrar e remover a atividade.");
        assertEquals(0, gerenciador.getListaAtividades().size(), "A lista deve ficar vazia após a exclusão.");
    }

    /**
     * Excluir uma atividade que não existe na lista deve falhar de forma explícita.
     */
    @Test
    public void testExcluirAtividadeInexistente() throws Exception {
        assertFalse(gerenciador.excluirAtividade("id-que-nao-existe"));
    }
}
