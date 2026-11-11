package organizaai.strategy;

import organizaai.modelo.Atividade;
import java.util.List;

/**
 * Interface que define o padrão Strategy para a ordenação da agenda.
 * Permite que diferentes algoritmos de ordenação sejam implementados 
 * sem alterar a classe principal de gerenciamento.
 */
public interface EstrategiaOrdenacao {
    
    /**
     * Ordena uma lista de atividades in-place (modificando a própria lista).
     * 
     * @param atividades A lista de atividades que será ordenada.
     */
    void ordenar(List<Atividade> atividades);
}