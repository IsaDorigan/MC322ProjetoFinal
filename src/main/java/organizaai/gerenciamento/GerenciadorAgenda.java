package organizaai.gerenciamento;

import organizaai.excecoes.ConflitoDeHorarioException;
import organizaai.excecoes.DataInvalidaException;
import organizaai.modelo.Atividade;
import organizaai.modelo.EventoComHorario;
import organizaai.modelo.Status;
import organizaai.strategy.EstrategiaOrdenacao;
import organizaai.strategy.OrdenacaoPorData;
import organizaai.persistencia.GerenciadorArquivoXML;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por gerenciar a lista de atividades da agenda.
 * Atua como a controladora principal, interligando a interface gráfica 
 * com as regras de negócio, exceções, modelo de dados e padrões de projeto (Strategy).
 */
public class GerenciadorAgenda {
    
    private List<Atividade> listaAtividades;
    private EstrategiaOrdenacao estrategiaOrdenacao;

    private GerenciadorArquivoXML gerenciadorArquivo;
    private final String CAMINHO_ARQUIVO = "meus_dados_agenda.xml";

    /**
     * Inicializa o gerenciador com uma lista vazia e define a ordenação 
     * por data como o comportamento padrão do sistema.
     */
    public GerenciadorAgenda() {
        this.gerenciadorArquivo = new GerenciadorArquivoXML();
        this.estrategiaOrdenacao = new OrdenacaoPorData(); 
        this.listaAtividades = gerenciadorArquivo.carregarAgenda(CAMINHO_ARQUIVO);
        // Atividades que venceram enquanto o programa estava fechado já abrem como atrasadas
        atualizarStatusAtrasados();
    }

    /**
     * Altera o algoritmo de ordenação
     * 
     * @param estrategia A nova estratégia escolhida pelo usuário na interface.
     */
    public void setEstrategiaOrdenacao(EstrategiaOrdenacao estrategia) {
        this.estrategiaOrdenacao = estrategia;
    }

    /**
     * Retorna uma cópia da lista de atividades, aplicando o algoritmo 
     * de ordenação selecionado atualmente na estratégia.
     * Retorna uma copia para proteger a lista original de modificações externas não desejadas.
     * 
     * @return Lista ordenada de objetos do tipo Atividade
     */
    public List<Atividade> obterAtividadesOrdenadas() {
        List<Atividade> copiaOrdenada = new ArrayList<>(this.listaAtividades);
        
        if (this.estrategiaOrdenacao != null) {
            this.estrategiaOrdenacao.ordenar(copiaOrdenada);
        }
        
        return copiaOrdenada;
    }

    /**
     * Cadastra uma nova atividade na agenda após passar pelas validações de dados e horarios.
     * 
     * @param atividade O objeto TarefaComPrazo ou EventoComHorario a ser adicionado.
     * @throws DataInvalidaException Se a data fornecida for no passado.
     * @throws ConflitoDeHorarioException Se for um evento que bate com o horário de outro evento no mesmo dia.
     */
    public void cadastrarAtividade(Atividade atividade) throws DataInvalidaException, ConflitoDeHorarioException {
        validarData(atividade.getData());
        
        if (atividade instanceof EventoComHorario) {
            validarConflitoDeHorario((EventoComHorario) atividade);
        }
        
        listaAtividades.add(atividade);
    }

    /**
     * Remove uma atividade da agenda a partir do seu ID único.
     * 
     * @param id O identificador único da atividade.
     * @return true se a atividade foi removida com sucesso, false caso contrário
     */
    public boolean excluirAtividade(String id) {
        return listaAtividades.removeIf(atv -> atv.getId().equals(id));
    }

    /**
     * Altera o status de uma atividade específica (pendente, em andamento ou concluída).
     * 
     * @param id         O identificador único da atividade.
     * @param novoStatus O novo status escolhido pelo usuário.
     * @return true se a atividade foi encontrada e atualizada, false caso contrário
     */
    public boolean atualizarStatus(String id, Status novoStatus) {
        for (Atividade atv : listaAtividades) {
            if (atv.getId().equals(id)) {
                atv.setStatus(novoStatus);
                return true;
            }
        }
        return false;
    }

    /**
     * Marca como EM_ATRASO toda atividade que não foi concluída e cuja data (prazo da tarefa
     * ou dia do evento) já passou. É chamado ao abrir o programa e a cada redesenho do calendário.
     * 
     * @return true se alguma atividade mudou de status, false caso contrário
     */
    public boolean atualizarStatusAtrasados() {
        boolean houveMudanca = false;
        for (Atividade atv : listaAtividades) {
            if (atv.estaEmAtraso() && atv.getStatus() != Status.EM_ATRASO) {
                atv.setStatus(Status.EM_ATRASO);
                houveMudanca = true;
            }
        }
        return houveMudanca;
    }

    /**
     * Substitui uma atividade existente pela versão atualizada (mesmo ID), mantendo a posição na lista.
     * Como o tipo (tarefa/evento) é a classe do objeto, mudar o tipo significa trocar o objeto inteiro.
     * A data não é revalidada, pois quem atualiza uma atividade antiga não deve ser barrado por ela já ter passado.
     * 
     * @param atualizada A atividade com os novos dados e o mesmo ID da original.
     * @throws ConflitoDeHorarioException Se a versão atualizada for um evento que bate com o horário de outro evento.
     * @throws IllegalArgumentException Se não existir atividade com esse ID.
     */
    public void atualizarAtividade(Atividade atualizada) throws ConflitoDeHorarioException {
        for (int i = 0; i < listaAtividades.size(); i++) {
            if (listaAtividades.get(i).getId().equals(atualizada.getId())) {
                if (atualizada instanceof EventoComHorario) {
                    validarConflitoDeHorario((EventoComHorario) atualizada);
                }
                listaAtividades.set(i, atualizada);
                return;
            }
        }
        throw new IllegalArgumentException("Atividade não encontrada: " + atualizada.getId());
    }

    /**
     * Retorna a lista de atividades sem ordenação,
     * útil para processos internos como salvar os dados em arquivo XML.
     * 
     * @return Lista original contendo as atividades.
     */
    public List<Atividade> getListaAtividades() {
        return listaAtividades;
    }

    private void validarData(LocalDate data) throws DataInvalidaException {
        if (data.isBefore(LocalDate.now())) {
            throw new DataInvalidaException("Não é possível agendar uma atividade em uma data no passado.");
        }
    }

    private void validarConflitoDeHorario(EventoComHorario novoEvento) throws ConflitoDeHorarioException {
        for (Atividade atv : listaAtividades) {
            // O próprio evento (mesmo ID) não conta como conflito: acontece ao atualizar um evento já cadastrado
            if (atv instanceof EventoComHorario && !atv.getId().equals(novoEvento.getId())) {
                EventoComHorario eventoExistente = (EventoComHorario) atv;
                
                if (eventoExistente.getData().equals(novoEvento.getData())) {
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

    /**
     * Aciona o módulo de persistência para varrer a lista atual 
     * e gravar as informações no arquivo XML local.
     */
    public void salvarDados() {
        gerenciadorArquivo.salvarAgenda(this.listaAtividades, CAMINHO_ARQUIVO);
    }
}
