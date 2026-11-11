package organizaai.strategy;

import organizaai.modelo.Atividade;
import java.util.List;

/**
 * Interface que define o padrão Strategy para a ordenação da agenda.
 */
public interface EstrategiaOrdenacao {
    
    /**
     * Ordena uma lista de atividades 
     * 
     * @param atividades A lista de atividades que será ordenada.
     */
    void ordenar(List<Atividade> atividades);
}