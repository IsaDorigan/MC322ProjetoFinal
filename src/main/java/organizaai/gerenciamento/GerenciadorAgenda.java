package organizaai.gerenciamento;

import organizaai.excecoes.ConflitoDeHorarioException;
import organizaai.excecoes.DataInvalidaException;
import organizaai.modelo.Atividade;
import organizaai.modelo.EventoComHorario;
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
    }

    /**
     * Altera dinamicamente o algoritmo de ordenação (Padrão Strategy).
     * 
     * @param estrategia A nova estratégia escolhida pelo usuário na interface.
     */
    public void setEstrategiaOrdenacao(EstrategiaOrdenacao estrategia) {
        this.estrategiaOrdenacao = estrategia;
    }

    /**
     * Retorna uma cópia da lista de atividades, aplicando o algoritmo 
     * de ordenação selecionado atualmente na estratégia.
     * Retorna uma cópia para proteger a lista original de modificações externas indesejadas.
     * 
     * @return Lista ordenada de objetos do tipo Atividade.
     */
    public List<Atividade> obterAtividadesOrdenadas() {
        List<Atividade> copiaOrdenada = new ArrayList<>(this.listaAtividades);
        
        if (this.estrategiaOrdenacao != null) {
            this.estrategiaOrdenacao.ordenar(copiaOrdenada);
        }
        
        return copiaOrdenada;
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
     * Remove uma atividade da agenda a partir do seu ID único.
     * 
     * @param id O identificador único (UUID) da atividade.
     * @return true se a atividade foi removida com sucesso, false caso contrário.
     */
    public boolean excluirAtividade(String id) {
        return listaAtividades.removeIf(atv -> atv.getId().equals(id));
    }

    /**
     * Altera o status de uma atividade específica para "concluída".
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
     * Retorna a lista bruta de atividades sem ordenação forçada,
     * útil para processos internos como salvar os dados em arquivo XML.
     * 
     * @return Lista original contendo as atividades.
     */
    public List<Atividade> getListaAtividades() {
        return listaAtividades;
    }

    // Métodos privados de validação mantidos exatamente como antes...

    private void validarData(LocalDate data) throws DataInvalidaException {
        if (data.isBefore(LocalDate.now())) {
            throw new DataInvalidaException("Não é possível agendar uma atividade em uma data no passado.");
        }
    }

    private void validarConflitoDeHorario(EventoComHorario novoEvento) throws ConflitoDeHorarioException {
        for (Atividade atv : listaAtividades) {
            if (atv instanceof EventoComHorario) {
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