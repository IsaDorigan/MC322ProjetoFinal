package organizaai.strategy;

import organizaai.modelo.Atividade;
import java.util.List;

/**
 * Estratégia concreta que ordena as atividades com base no nível de urgência.
 * Atividades com prioridade ALTA aparecerão no topo da lista.
 */
public class OrdenacaoPorPrioridade implements EstrategiaOrdenacao {

    @Override
    public void ordenar(List<Atividade> atividades) {
        // O método compareTo() inverte a1 e a2 para gerar ordem decrescente.
        // Como no Enum Prioridade a ordem é BAIXA(0), MEDIA(1), ALTA(2),
        // a ordem decrescente garante que ALTA fique no início da lista.
        atividades.sort((a1, a2) -> a2.getPrioridade().compareTo(a1.getPrioridade()));
    }
}