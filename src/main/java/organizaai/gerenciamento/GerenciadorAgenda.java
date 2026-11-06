package src.main.java.organizaai.gerenciamento;

import src.main.java.organizaai.excecoes.ConflitoDeHorarioException;
import src.main.java.organizaai.excecoes.DataInvalidaException;
import src.main.java.organizaai.modelo.Atividade;
import src.main.java.organizaai.modelo.EventoComHorario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por gerenciar a lista de atividades da agenda.
 * Atua como a controladora principal, interligando a interface gráfica (futura) 
 * com as regras de negócio, exceções e o modelo de dados.
 */
public class GerenciadorAgenda {
    
    private List<Atividade> listaAtividades;

    /**
     * Inicializa o gerenciador com uma lista vazia de atividades prontas para uso.
     */
    public GerenciadorAgenda() {
        this.listaAtividades = new ArrayList<>();
    }

    /**
     * Cadastra uma nova atividade na agenda após passar pelas validações de dados e horários.
     * 
     * @param atividade O objeto TarefaComPrazo ou EventoComHorario a ser adicionado.
     * @throws DataInvalidaException Se a data fornecida for no passado.
     * @throws ConflitoDeHorarioException Se for um evento que choque com o horário de outro evento no mesmo dia.
     */
    public void cadastrarAtividade(Atividade atividade) throws DataInvalidaException, ConflitoDeHorarioException {
        validarData(atividade.getData());
        
        if (atividade instanceof EventoComHorario) {
            validarConflitoDeHorario((EventoComHorario) atividade);
        }
        
        listaAtividades.add(atividade);
    }

    /**
     * Remove uma atividade da agenda a partir do seu ID único gerado no momento da criação.
     * 
     * @param id O identificador único (UUID) da atividade.
     * @return true se a atividade foi encontrada e removida com sucesso, false caso contrário.
     */
    public boolean excluirAtividade(String id) {
        return listaAtividades.removeIf(atv -> atv.getId().equals(id));
    }

    /**
     * Altera o status de uma atividade específica para "concluída".
     * Facilita o filtro visual sem precisar apagar o histórico do usuário.
     * 
     * @param id O identificador único da atividade.
     */
    public void marcarComoConcluida(String id) {
        for (Atividade atv : listaAtividades) {
            if (atv.getId().equals(id)) {
                atv.setConcluida(true);
                break;
            }
        }
    }

    /**
     * Retorna a lista completa de atividades salvas atualmente na memória.
     * 
     * @return Lista contendo objetos do tipo Atividade (Tarefas e Eventos).
     */
    public List<Atividade> getListaAtividades() {
        return listaAtividades;
    }

    /**
     * Valida se a data fornecida é igual ou posterior à data atual do sistema.
     * 
     * @param data A data a ser verificada.
     * @throws DataInvalidaException Se a data for estritamente anterior ao dia de hoje.
     */
    private void validarData(LocalDate data) throws DataInvalidaException {
        if (data.isBefore(LocalDate.now())) {
            throw new DataInvalidaException("Não é possível agendar uma atividade em uma data no passado.");
        }
    }

    /**
     * Valida se o novo evento entra em conflito de horário com algum evento já existente na mesma data.
     * 
     * @param novoEvento O evento que o usuário está tentando agendar.
     * @throws ConflitoDeHorarioException Se o horário coincidir ou sobrepor outro evento do mesmo dia.
     */
    private void validarConflitoDeHorario(EventoComHorario novoEvento) throws ConflitoDeHorarioException {
        for (Atividade atv : listaAtividades) {
            if (atv instanceof EventoComHorario) {
                EventoComHorario eventoExistente = (EventoComHorario) atv;
                
                // Checa se os eventos estão marcados para o mesmo dia
                if (eventoExistente.getData().equals(novoEvento.getData())) {
                    
                    // Lógica de intersecção temporal: 
                    // Se o Início de A é antes do Fim de B, E o Fim de A é depois do Início de B = Conflito
                    boolean temConflito = novoEvento.getHoraInicio().isBefore(eventoExistente.getHoraFim()) &&
                                          novoEvento.getHoraFim().isAfter(eventoExistente.getHoraInicio());
                                          
                    if (temConflito) {
                        throw new ConflitoDeHorarioException(
                            "O horário escolhido entra em conflito com o evento já existente: " + eventoExistente.getTitulo()
                        );
                    }
                }
            }
        }
    }
}