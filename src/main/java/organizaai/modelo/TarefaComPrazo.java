package src.main.java.organizaai.modelo;

import java.time.LocalDate;

/**
 * Representa uma tarefa que possui uma data limite para ser executada (deadline).
 * Herda as características de Atividade.
 */
public class TarefaComPrazo extends Atividade {

    /**
     * Construtor de TarefaComPrazo.
     * 
     * @param titulo     Título da tarefa.
     * @param descricao  Descrição da tarefa.
     * @param prazo      A data limite (prazo final) da tarefa.
     * @param categoria  A categoria relacionada.
     * @param prioridade O nível de prioridade.
     */
    public TarefaComPrazo(String titulo, String descricao, LocalDate prazo, Categoria categoria, Prioridade prioridade) {
        super(titulo, descricao, prazo, categoria, prioridade);
    }

    /**
     * Retorna os detalhes formatados especificamente para uma Tarefa.
     * 
     * @return String com os dados da Tarefa.
     */
    @Override
    public String exibirDetalhes() {
        return String.format("[TAREFA] %s - Prazo: %s | Prioridade: %s | Status: %s", 
                getTitulo(), getData().toString(), getPrioridade(), isConcluida() ? "Concluída" : "Pendente");
    }
}