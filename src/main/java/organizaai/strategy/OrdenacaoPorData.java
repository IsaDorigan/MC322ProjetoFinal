package organizaai.strategy;

import organizaai.modelo.Atividade;
import java.util.Comparator;
import java.util.List;

/**
 * Estratégia concreta que ordena as atividades de forma cronológica.
 * Os eventos e tarefas com datas mais antigas (ou próximas) aparecerão primeiro.
 */
public class OrdenacaoPorData implements EstrategiaOrdenacao {

    @Override
    public void ordenar(List<Atividade> atividades) {
        // Utiliza o Comparator comparando o atributo 'data' de cada Atividade
        atividades.sort(Comparator.comparing(Atividade::getData));
    }
}