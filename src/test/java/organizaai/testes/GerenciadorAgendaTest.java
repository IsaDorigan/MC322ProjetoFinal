package src.test.java.organizaai.testes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import organizaai.excecoes.ConflitoDeHorarioException;
import organizaai.excecoes.DataInvalidaException;
import organizaai.gerenciamento.GerenciadorAgenda;
import organizaai.modelo.Categoria;
import organizaai.modelo.EventoComHorario;
import organizaai.modelo.Prioridade;
import organizaai.modelo.TarefaComPrazo;

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
                    Prioridade.ALTA
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
                Prioridade.BAIXA
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
                Categoria.FACULDADE, Prioridade.ALTA
        );

        // Segundo evento: das 15:00 às 17:00 (Sobrepõe o evento 1 em 1 hora)
        EventoComHorario evento2 = new EventoComHorario(
                "Consultoria CCU", "Workshop", data,
                LocalTime.of(15, 0), LocalTime.of(17, 0),
                Categoria.TRABALHO, Prioridade.MEDIA
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
     * Testa se a funcionalidade de alterar o status booleano 'concluida' funciona pelo ID gerado.
     */
    @Test
    public void testMarcarComoConcluida() throws Exception {
        LocalDate data = LocalDate.now().plusDays(1);
        TarefaComPrazo tarefa = new TarefaComPrazo(
                "Comprar presente", "Shopping", data, Categoria.PESSOAL, Prioridade.MEDIA
        );
        
        gerenciador.cadastrarAtividade(tarefa);
        
        // Recupera o ID (UUID) que foi gerado automaticamente pela superclasse Atividade
        String idGerado = gerenciador.getListaAtividades().get(0).getId();
        
        // Invoca o método de conclusão da Controller
        gerenciador.marcarComoConcluida(idGerado);

        // Afirma que o status da atividade mudou de false para true
        assertTrue(gerenciador.getListaAtividades().get(0).isConcluida(), "A atividade deveria estar marcada como concluída (true).");
    }

    /**
     * Testa se a funcionalidade de exclusão remove corretamente o objeto da lista pelo ID.
     */
    @Test
    public void testExcluirAtividade() throws Exception {
        LocalDate data = LocalDate.now().plusDays(1);
        TarefaComPrazo tarefa = new TarefaComPrazo(
                "Estudar Microcontroladores", "Revisar STM32", data, Categoria.FACULDADE, Prioridade.ALTA
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
}